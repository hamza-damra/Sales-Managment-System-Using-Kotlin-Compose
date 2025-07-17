package services

import data.api.*
import data.repository.UpdateRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import utils.Constants
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * Core update service for managing update operations
 * Handles polling, downloading, installation, and lifecycle management
 */
class UpdateService(
    private val updateRepository: UpdateRepository,
    private val notificationService: NotificationService
) {
    private var pollingJob: Job? = null
    private var downloadJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    // State flows
    private val _isPollingActive = MutableStateFlow(false)
    val isPollingActive: StateFlow<Boolean> = _isPollingActive.asStateFlow()
    
    private val _lastUpdateCheck = MutableStateFlow<String?>(null)
    val lastUpdateCheck: StateFlow<String?> = _lastUpdateCheck.asStateFlow()

    /**
     * Start automatic update polling
     */
    fun startPolling() {
        if (pollingJob?.isActive == true) {
            println("⚠️ UpdateService - Polling already active")
            return
        }
        
        println("🔄 UpdateService - Starting update polling")
        _isPollingActive.value = true
        
        pollingJob = serviceScope.launch {
            while (isActive) {
                try {
                    val preferences = updateRepository.updatePreferences.value
                    
                    if (preferences.autoCheckEnabled) {
                        println("🔍 UpdateService - Performing scheduled update check")
                        performUpdateCheck()
                    }
                    
                    // Wait for the configured interval
                    val intervalMinutes = updateRepository.updatePreferences.value.checkIntervalMinutes
                    delay(intervalMinutes.minutes)
                    
                } catch (e: CancellationException) {
                    println("🛑 UpdateService - Polling cancelled")
                    break
                } catch (e: Exception) {
                    println("❌ UpdateService - Error during polling: ${e.message}")
                    // Continue polling even if one check fails
                    delay(Constants.Updates.RETRY_DELAY_SECONDS.seconds)
                }
            }
        }
    }

    /**
     * Stop automatic update polling
     */
    fun stopPolling() {
        println("🛑 UpdateService - Stopping update polling")
        pollingJob?.cancel()
        pollingJob = null
        _isPollingActive.value = false
    }

    /**
     * Perform manual update check
     */
    suspend fun performUpdateCheck(): NetworkResult<UpdateCheckResponseDTO> {
        println("🔍 UpdateService - Performing manual update check")
        _lastUpdateCheck.value = getCurrentTimestamp()
        
        val result = updateRepository.checkForUpdates()
        
        when (result) {
            is NetworkResult.Success -> {
                val updateData = result.data
                
                if (updateData.updateAvailable) {
                    println("📢 UpdateService - Update available: ${updateData.latestVersion}")
                    
                    // Show notification if enabled
                    val preferences = updateRepository.updatePreferences.value
                    if (preferences.notificationsEnabled) {
                        showUpdateNotification(updateData)
                    }
                    
                    // Auto-download if enabled and not mandatory (mandatory updates should be user-initiated)
                    if (preferences.autoDownloadEnabled && !updateData.isMandatory) {
                        println("⬇️ UpdateService - Starting auto-download")
                        startDownload(updateData.latestVersion)
                    }
                } else {
                    println("✅ UpdateService - No updates available")
                }
            }
            is NetworkResult.Error -> {
                println("❌ UpdateService - Update check failed: ${result.exception.message}")
                
                // Show error notification if it's a user-initiated check
                if (updateRepository.updateState.value.isChecking) {
                    notificationService.showError(
                        title = "فشل في فحص التحديثات",
                        message = result.exception.message ?: "حدث خطأ أثناء فحص التحديثات"
                    )
                }
            }
            is NetworkResult.Loading -> {
                // Handled by repository state
            }
        }
        
        return result
    }

    /**
     * Start downloading an update
     */
    suspend fun startDownload(version: String): Flow<NetworkResult<ByteArray>> {
        println("⬇️ UpdateService - Starting download for version: $version")
        
        // Cancel any existing download
        downloadJob?.cancel()
        
        return flow {
            try {
                // Check if differential update is available
                val preferences = updateRepository.updatePreferences.value
                var downloadFlow: Flow<NetworkResult<ByteArray>>? = null
                
                if (preferences.preferDifferentialUpdates) {
                    println("🔍 UpdateService - Checking for differential update")
                    val deltaResult = updateRepository.getDifferentialUpdate(Constants.App.VERSION, version)
                    
                    if (deltaResult is NetworkResult.Success && deltaResult.data.deltaAvailable) {
                        println("📦 UpdateService - Using differential update (${deltaResult.data.formattedDeltaSize})")
                        downloadFlow = updateRepository.downloadDifferentialUpdate(
                            Constants.App.VERSION,
                            version
                        ) { progress ->
                            // Progress is handled by repository
                        }
                    }
                }
                
                // Fall back to full download if differential not available
                if (downloadFlow == null) {
                    println("📦 UpdateService - Using full update download")
                    downloadFlow = updateRepository.downloadUpdate(version) { progress ->
                        // Progress is handled by repository
                    }
                }
                
                // Collect and emit the download results
                downloadFlow.collect { result ->
                    emit(result)
                    
                    when (result) {
                        is NetworkResult.Success -> {
                            println("✅ UpdateService - Download completed successfully")
                            
                            // Verify file integrity
                            val versionResult = updateRepository.getLatestVersion()
                            if (versionResult is NetworkResult.Success) {
                                val isValid = updateRepository.verifyFileIntegrity(
                                    result.data,
                                    versionResult.data.checksum
                                )
                                
                                if (isValid) {
                                    println("✅ UpdateService - File integrity verified")
                                    
                                    // Save the downloaded file
                                    val savedFile = saveDownloadedFile(result.data, version)
                                    if (savedFile != null) {
                                        println("💾 UpdateService - File saved: ${savedFile.absolutePath}")
                                        
                                        // Add to history
                                        updateRepository.addToHistory(
                                            UpdateHistoryEntry(
                                                version = version,
                                                updateDate = getCurrentTimestamp(),
                                                updateType = "FULL",
                                                downloadSize = result.data.size.toLong(),
                                                installationTime = 0L,
                                                success = true,
                                                releaseNotes = versionResult.data.releaseNotes
                                            )
                                        )
                                        
                                        // Show completion notification
                                        notificationService.showSuccess(
                                            title = "تم تحميل التحديث",
                                            message = "تم تحميل الإصدار $version بنجاح. يمكنك الآن تثبيته."
                                        )
                                    }
                                } else {
                                    println("❌ UpdateService - File integrity check failed")
                                    emit(NetworkResult.Error(ApiException.ValidationError(
                                        mapOf("checksum" to listOf("File integrity verification failed"))
                                    )))
                                }
                            }
                        }
                        is NetworkResult.Error -> {
                            println("❌ UpdateService - Download failed: ${result.exception.message}")
                            
                            // Add failed entry to history
                            updateRepository.addToHistory(
                                UpdateHistoryEntry(
                                    version = version,
                                    updateDate = getCurrentTimestamp(),
                                    updateType = "FULL",
                                    downloadSize = 0L,
                                    installationTime = 0L,
                                    success = false,
                                    errorMessage = result.exception.message
                                )
                            )
                            
                            // Show error notification
                            notificationService.showError(
                                title = "فشل في تحميل التحديث",
                                message = result.exception.message ?: "حدث خطأ أثناء تحميل التحديث"
                            )
                        }
                        is NetworkResult.Loading -> {
                            // Handled by repository state
                        }
                    }
                }
                
            } catch (e: Exception) {
                println("❌ UpdateService - Download error: ${e.message}")
                emit(NetworkResult.Error(e.toApiException()))
            }
        }
    }

    /**
     * Install downloaded update
     */
    suspend fun installUpdate(updateFile: File, version: String): UpdateInstallationResult {
        println("🔧 UpdateService - Installing update: $version")

        return try {
            updateRepository.updateState.value.copy(isInstalling = true)

            val startTime = System.currentTimeMillis()

            // Validate update file integrity first
            val versionResult = updateRepository.getLatestVersion()
            if (versionResult is NetworkResult.Success) {
                val isValid = updateRepository.verifyFileIntegrity(
                    updateFile.readBytes(),
                    versionResult.data.checksum
                )

                if (!isValid) {
                    throw Exception("فشل في التحقق من سلامة ملف التحديث")
                }
            }

            // Create backup of current application
            val backupFile = createBackup()
            if (backupFile == null) {
                throw Exception("فشل في إنشاء نسخة احتياطية من التطبيق الحالي")
            }

            // Perform the actual JAR file replacement
            val replacementResult = replaceApplicationJar(updateFile)
            if (!replacementResult.success) {
                // Restore from backup if replacement failed
                restoreFromBackup(backupFile)
                throw Exception(replacementResult.errorMessage ?: "فشل في استبدال ملف التطبيق")
            }

            val installationTime = System.currentTimeMillis() - startTime

            // Add successful installation to history
            updateRepository.addToHistory(
                UpdateHistoryEntry(
                    version = version,
                    updateDate = getCurrentTimestamp(),
                    updateType = "INSTALLATION",
                    downloadSize = updateFile.length(),
                    installationTime = installationTime,
                    success = true
                )
            )

            println("✅ UpdateService - Update installed successfully")

            // Show success notification
            notificationService.showSuccess(
                title = "تم تثبيت التحديث",
                message = "تم تثبيت الإصدار $version بنجاح. سيتم إعادة تشغيل التطبيق."
            )

            UpdateInstallationResult(
                success = true,
                version = version,
                installationTimeMs = installationTime,
                requiresRestart = true,
                backupCreated = true,
                backupPath = backupFile.absolutePath
            )

        } catch (e: Exception) {
            println("❌ UpdateService - Installation failed: ${e.message}")

            // Add failed installation to history
            updateRepository.addToHistory(
                UpdateHistoryEntry(
                    version = version,
                    updateDate = getCurrentTimestamp(),
                    updateType = "INSTALLATION",
                    downloadSize = updateFile.length(),
                    installationTime = 0L,
                    success = false,
                    errorMessage = e.message
                )
            )

            // Show error notification
            notificationService.showError(
                title = "فشل في تثبيت التحديث",
                message = e.message ?: "حدث خطأ أثناء تثبيت التحديث"
            )

            UpdateInstallationResult(
                success = false,
                version = version,
                installationTimeMs = 0L,
                errorMessage = e.message,
                requiresRestart = false
            )
        } finally {
            updateRepository.updateState.value.copy(isInstalling = false)
        }
    }

    /**
     * Restart application to apply update
     */
    suspend fun restartApplication(): Boolean {
        return try {
            println("🔄 UpdateService - Initiating application restart")

            // Give a brief delay to allow UI to update
            delay(1000)

            // In development mode, show a message instead of actually restarting
            if (isDevelopmentMode()) {
                println("⚠️ UpdateService - Development mode: restart simulation")
                notificationService.showInfo(
                    title = "وضع التطوير",
                    message = "تم محاكاة إعادة التشغيل. في الإنتاج، سيتم إعادة تشغيل التطبيق تلقائياً."
                )
                return true
            }

            // Get current JAR path
            val currentJarPath = getCurrentJarPath()
            if (currentJarPath == null) {
                println("❌ UpdateService - Could not determine current JAR path for restart")
                notificationService.showError(
                    title = "فشل في إعادة التشغيل",
                    message = "لا يمكن تحديد مسار ملف التطبيق. يرجى إعادة التشغيل يدوياً."
                )
                return false
            }

            // Prepare restart command with additional JVM options
            val javaExecutable = System.getProperty("java.home") + "/bin/java"
            val restartCommand = mutableListOf(
                javaExecutable,
                "-Xmx2g", // Increase memory for better performance
                "-Dfile.encoding=UTF-8", // Ensure UTF-8 encoding for Arabic text
                "-jar",
                currentJarPath.absolutePath
            )

            println("🚀 UpdateService - Executing restart command: ${restartCommand.joinToString(" ")}")

            // Start new process
            val processBuilder = ProcessBuilder(restartCommand)
            processBuilder.directory(currentJarPath.parentFile ?: File(System.getProperty("user.dir")))

            // Redirect output to avoid hanging
            processBuilder.redirectOutput(ProcessBuilder.Redirect.DISCARD)
            processBuilder.redirectError(ProcessBuilder.Redirect.DISCARD)

            val newProcess = processBuilder.start()

            // Verify the new process started successfully
            delay(2000) // Wait a bit longer to ensure startup

            if (newProcess.isAlive) {
                println("✅ UpdateService - New application instance started successfully")

                // Exit current application
                delay(500)
                kotlin.system.exitProcess(0)
            } else {
                println("❌ UpdateService - New application instance failed to start")
                notificationService.showError(
                    title = "فشل في إعادة التشغيل",
                    message = "فشل في بدء تشغيل التطبيق الجديد. يرجى إعادة التشغيل يدوياً."
                )
                return false
            }

        } catch (e: Exception) {
            println("❌ UpdateService - Failed to restart application: ${e.message}")
            e.printStackTrace()
            notificationService.showError(
                title = "فشل في إعادة التشغيل",
                message = "فشل في إعادة تشغيل التطبيق: ${e.message ?: "خطأ غير معروف"}"
            )
            false
        }
    }

    /**
     * Cancel current download
     */
    fun cancelDownload() {
        println("🛑 UpdateService - Cancelling download")
        downloadJob?.cancel()
        downloadJob = null
        updateRepository.clearUpdateState()
    }

    /**
     * Cleanup service resources
     */
    fun cleanup() {
        println("🧹 UpdateService - Cleaning up resources")
        stopPolling()
        cancelDownload()
        serviceScope.cancel()
    }

    // Private helper methods
    private fun showUpdateNotification(updateData: UpdateCheckResponseDTO) {
        val title = if (updateData.isMandatory) {
            "تحديث إجباري متاح"
        } else {
            "تحديث جديد متاح"
        }
        
        val message = "الإصدار ${updateData.latestVersion} متاح للتحميل (${updateData.formattedFileSize})"
        
        if (updateData.isMandatory) {
            notificationService.showWarning(title, message)
        } else {
            notificationService.showInfo(title, message)
        }
    }

    private fun saveDownloadedFile(data: ByteArray, version: String): File? {
        return try {
            val downloadsDir = File(Constants.Files.TEMP_DIRECTORY, "updates")
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }
            
            val fileName = "sales-management-$version.jar"
            val file = File(downloadsDir, fileName)
            
            Files.write(file.toPath(), data)
            file
        } catch (e: Exception) {
            println("❌ UpdateService - Failed to save downloaded file: ${e.message}")
            null
        }
    }

    private fun createBackup(): File? {
        return try {
            val backupDir = File(Constants.Files.BACKUPS_DIRECTORY)
            if (!backupDir.exists()) {
                backupDir.mkdirs()
            }

            // Get current JAR file path
            val currentJar = getCurrentJarPath()
            val timestamp = System.currentTimeMillis()
            val backupFileName = "backup-${Constants.App.VERSION}-$timestamp.jar"
            val backupFile = File(backupDir, backupFileName)

            if (currentJar?.exists() == true) {
                if (isDevelopmentMode()) {
                    println("⚠️ UpdateService - Development mode: creating enhanced mock backup")
                    val mockBackupContent = """
                        |Development Mode Backup
                        |Created: $timestamp
                        |Original Version: ${Constants.App.VERSION}
                        |Original Path: ${currentJar.absolutePath}
                        |Original Size: ${currentJar.length()} bytes
                        |Purpose: Testing backup functionality in development environment
                    """.trimMargin()

                    backupFile.writeText(mockBackupContent)
                    println("✅ UpdateService - Enhanced mock backup created: ${backupFile.absolutePath}")
                } else {
                    // Production mode: Create actual backup
                    Files.copy(currentJar.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
                    println("✅ UpdateService - Production backup created: ${backupFile.absolutePath}")
                }

                backupFile
            } else {
                println("❌ UpdateService - Current JAR file not found")

                // Even if JAR not found, create a placeholder backup in development mode
                if (isDevelopmentMode()) {
                    println("⚠️ UpdateService - Development mode: creating placeholder backup")
                    val placeholderContent = """
                        |Development Mode Placeholder Backup
                        |Created: $timestamp
                        |Note: Original JAR not found, this is a placeholder for testing
                    """.trimMargin()

                    backupFile.writeText(placeholderContent)
                    println("✅ UpdateService - Placeholder backup created: ${backupFile.absolutePath}")
                    return backupFile
                }

                null
            }
        } catch (e: Exception) {
            println("❌ UpdateService - Failed to create backup: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    private fun getCurrentJarPath(): File? {
        return try {
            println("🔍 UpdateService - Attempting to find current JAR path")

            // Method 1: Check if running from a standalone JAR (production mode)
            val codeSource = UpdateService::class.java.protectionDomain.codeSource
            if (codeSource != null) {
                val jarPath = File(codeSource.location.toURI())
                println("🔍 UpdateService - Code source path: ${jarPath.absolutePath}")

                if (jarPath.exists() && jarPath.name.endsWith(".jar") && !jarPath.absolutePath.contains(".gradle")) {
                    println("✅ UpdateService - Found application JAR via code source: ${jarPath.absolutePath}")
                    return jarPath
                }
            }

            // Method 2: Look for application JAR in common locations
            val possibleLocations = listOf(
                // Current working directory
                File(System.getProperty("user.dir")),
                // Parent directory
                File(System.getProperty("user.dir")).parentFile,
                // Application directory (if installed)
                File(System.getProperty("user.home"), "AppData/Local/SalesManagementSystem"),
                File(System.getProperty("user.home"), ".local/share/SalesManagementSystem"),
                File("/opt/SalesManagementSystem"),
                File("C:/Program Files/SalesManagementSystem")
            )

            for (location in possibleLocations) {
                if (location?.exists() == true) {
                    val jarFiles = location.listFiles { file ->
                        file.name.endsWith(".jar") &&
                        (file.name.contains("sales-management") || file.name.contains("SalesManagementSystem"))
                    }

                    if (!jarFiles.isNullOrEmpty()) {
                        val appJar = jarFiles.first()
                        println("✅ UpdateService - Found application JAR: ${appJar.absolutePath}")
                        return appJar
                    }
                }
            }

            // Method 3: Development mode fallback
            if (isDevelopmentMode()) {
                println("⚠️ UpdateService - Running in development mode, creating mock JAR for testing")
                return createMockJarForDevelopment()
            }

            println("❌ UpdateService - Could not determine application JAR path")
            null

        } catch (e: Exception) {
            println("❌ UpdateService - Failed to determine JAR path: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    private fun isDevelopmentMode(): Boolean {
        return try {
            // Check if we're running from IDE or build directory
            val classPath = System.getProperty("java.class.path")
            val codeSource = UpdateService::class.java.protectionDomain.codeSource

            // Multiple indicators of development mode
            val isDevClassPath = classPath.contains("build/classes") ||
                               classPath.contains("target/classes") ||
                               classPath.contains("out/production") ||
                               classPath.contains(".gradle/caches")

            val isDevCodeSource = codeSource?.location?.path?.let { path ->
                path.contains("build/classes") ||
                path.contains("target/classes") ||
                path.contains("out/production")
            } ?: false

            val hasNoMainJar = !classPath.split(System.getProperty("path.separator"))
                .any { it.endsWith(".jar") && !it.contains(".gradle") && !it.contains("/.m2/") }

            isDevClassPath || isDevCodeSource || hasNoMainJar
        } catch (e: Exception) {
            println("⚠️ UpdateService - Error detecting development mode: ${e.message}")
            false
        }
    }

    private fun createMockJarForDevelopment(): File? {
        return try {
            val tempDir = File(Constants.Files.TEMP_DIRECTORY, "development")
            if (!tempDir.exists()) {
                tempDir.mkdirs()
            }

            val mockJar = File(tempDir, "sales-management-dev-${Constants.App.VERSION}.jar")
            if (!mockJar.exists()) {
                // Create a more realistic mock JAR file for development testing
                val mockContent = """
                    |Development Mode Mock JAR
                    |Created: ${System.currentTimeMillis()}
                    |Version: ${Constants.App.VERSION}
                    |Purpose: Testing update functionality in development environment
                    |Note: This is not a real JAR file, just a placeholder for testing
                """.trimMargin()

                mockJar.writeText(mockContent)
                println("✅ UpdateService - Created mock JAR for development: ${mockJar.absolutePath}")
            } else {
                println("✅ UpdateService - Using existing mock JAR: ${mockJar.absolutePath}")
            }

            mockJar
        } catch (e: Exception) {
            println("❌ UpdateService - Failed to create mock JAR: ${e.message}")
            null
        }
    }

    private fun replaceApplicationJar(newJarFile: File): JarReplacementResult {
        return try {
            val currentJar = getCurrentJarPath()
            if (currentJar == null) {
                return JarReplacementResult(false, "لا يمكن تحديد مسار ملف التطبيق الحالي")
            }

            println("🔄 UpdateService - Replacing JAR: ${currentJar.absolutePath}")

            // Verify new JAR file exists and is valid
            if (!newJarFile.exists()) {
                return JarReplacementResult(false, "ملف التحديث غير موجود")
            }

            if (newJarFile.length() == 0L) {
                return JarReplacementResult(false, "ملف التحديث فارغ")
            }

            // In development mode, simulate the replacement
            if (isDevelopmentMode()) {
                println("⚠️ UpdateService - Development mode: simulating JAR replacement")

                // Update the mock JAR with new content to simulate the update
                val mockContent = """
                    |Development Mode Updated JAR
                    |Updated: ${System.currentTimeMillis()}
                    |New Version: Simulated Update
                    |Original Size: ${newJarFile.length()} bytes
                    |Purpose: Testing update functionality in development environment
                """.trimMargin()

                currentJar.writeText(mockContent)
                Thread.sleep(1000) // Simulate replacement time
                println("✅ UpdateService - JAR replacement simulated successfully")
                return JarReplacementResult(true, null)
            }

            // Production mode: Perform actual JAR replacement
            println("🔧 UpdateService - Production mode: performing actual JAR replacement")

            // Ensure parent directory exists
            currentJar.parentFile?.mkdirs()

            // Create a temporary file for atomic replacement
            val tempFile = File(currentJar.parentFile, "${currentJar.name}.tmp")

            // Copy new JAR to temporary location
            Files.copy(newJarFile.toPath(), tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING)

            // Verify the temporary file
            if (!tempFile.exists() || tempFile.length() != newJarFile.length()) {
                tempFile.delete()
                return JarReplacementResult(false, "فشل في نسخ ملف التحديث")
            }

            // Atomic replacement: move temp file to replace current JAR
            Files.move(tempFile.toPath(), currentJar.toPath(), StandardCopyOption.REPLACE_EXISTING)

            println("✅ UpdateService - JAR replacement completed successfully")
            JarReplacementResult(true, null)

        } catch (e: Exception) {
            println("❌ UpdateService - JAR replacement failed: ${e.message}")
            e.printStackTrace()
            JarReplacementResult(false, e.message)
        }
    }

    private fun restoreFromBackup(backupFile: File): Boolean {
        return try {
            val currentJar = getCurrentJarPath()
            if (currentJar == null) {
                println("❌ UpdateService - Cannot restore: current JAR path unknown")
                return false
            }

            if (!backupFile.exists()) {
                println("❌ UpdateService - Cannot restore: backup file does not exist")
                return false
            }

            println("🔄 UpdateService - Restoring from backup: ${backupFile.absolutePath}")

            // In development mode, simulate restoration
            if (isDevelopmentMode()) {
                println("⚠️ UpdateService - Development mode: simulating backup restoration")
                val restoredContent = """
                    |Development Mode Restored JAR
                    |Restored: ${System.currentTimeMillis()}
                    |Backup Source: ${backupFile.name}
                    |Purpose: Testing backup restoration in development environment
                """.trimMargin()

                currentJar.writeText(restoredContent)
                println("✅ UpdateService - Backup restoration simulated successfully")
                return true
            }

            // Production mode: Perform actual restoration
            Files.copy(backupFile.toPath(), currentJar.toPath(), StandardCopyOption.REPLACE_EXISTING)

            println("✅ UpdateService - Successfully restored from backup")
            true

        } catch (e: Exception) {
            println("❌ UpdateService - Failed to restore from backup: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    private fun getCurrentTimestamp(): String {
        return kotlinx.datetime.Clock.System.now().toString()
    }

    // Data class for JAR replacement result
    private data class JarReplacementResult(
        val success: Boolean,
        val errorMessage: String?
    )
}
