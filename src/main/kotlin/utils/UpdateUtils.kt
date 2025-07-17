package utils

import data.api.*
import kotlinx.datetime.*
import java.text.DecimalFormat
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Utility functions for update operations
 * Following the established utility pattern from the codebase
 */
object UpdateUtils {

    /**
     * Format file size in human-readable format
     */
    fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
        
        val size = bytes / Math.pow(1024.0, digitGroups.toDouble())
        val formatter = DecimalFormat("#,##0.#")
        
        return "${formatter.format(size)} ${units[digitGroups]}"
    }

    /**
     * Format download speed in human-readable format
     */
    fun formatSpeed(bytesPerSecond: Long): String {
        return "${formatFileSize(bytesPerSecond)}/s"
    }

    /**
     * Format time duration in human-readable format
     */
    fun formatDuration(seconds: Long): String {
        if (seconds <= 0) return "غير معروف"
        
        val duration = seconds.seconds
        
        return when {
            duration.inWholeHours > 0 -> {
                val hours = duration.inWholeHours
                val minutes = (duration.inWholeMinutes % 60)
                if (minutes > 0) "${hours}س ${minutes}د" else "${hours}س"
            }
            duration.inWholeMinutes > 0 -> {
                val minutes = duration.inWholeMinutes
                val secs = (duration.inWholeSeconds % 60)
                if (secs > 0) "${minutes}د ${secs}ث" else "${minutes}د"
            }
            else -> "${duration.inWholeSeconds}ث"
        }
    }

    /**
     * Compare version strings using semantic versioning
     */
    fun compareVersions(version1: String, version2: String): Int {
        val v1Parts = version1.split(".").map { it.toIntOrNull() ?: 0 }
        val v2Parts = version2.split(".").map { it.toIntOrNull() ?: 0 }
        
        val maxLength = maxOf(v1Parts.size, v2Parts.size)
        
        for (i in 0 until maxLength) {
            val v1Part = v1Parts.getOrNull(i) ?: 0
            val v2Part = v2Parts.getOrNull(i) ?: 0
            
            when {
                v1Part < v2Part -> return -1
                v1Part > v2Part -> return 1
            }
        }
        
        return 0
    }

    /**
     * Check if a version is newer than another
     */
    fun isNewerVersion(currentVersion: String, newVersion: String): Boolean {
        return compareVersions(currentVersion, newVersion) < 0
    }

    /**
     * Validate version string format
     */
    fun isValidVersionFormat(version: String): Boolean {
        val versionRegex = Regex("""^\d+\.\d+\.\d+(-[a-zA-Z0-9]+)?$""")
        return versionRegex.matches(version)
    }

    /**
     * Format timestamp for display
     */
    fun formatTimestamp(timestamp: String): String {
        return try {
            val instant = Instant.parse(timestamp)
            val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
            
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val today = now.date
            val yesterday = today.minus(1, DateTimeUnit.DAY)
            
            when (localDateTime.date) {
                today -> "اليوم ${localDateTime.time.toString().substring(0, 5)}"
                yesterday -> "أمس ${localDateTime.time.toString().substring(0, 5)}"
                else -> "${localDateTime.date} ${localDateTime.time.toString().substring(0, 5)}"
            }
        } catch (e: Exception) {
            timestamp
        }
    }

    /**
     * Calculate estimated time remaining for download
     */
    fun calculateETA(downloadedBytes: Long, totalBytes: Long, speedBytesPerSecond: Long): Long {
        if (speedBytesPerSecond <= 0 || totalBytes <= downloadedBytes) return 0
        
        val remainingBytes = totalBytes - downloadedBytes
        return remainingBytes / speedBytesPerSecond
    }

    /**
     * Get update type display name in Arabic
     */
    fun getUpdateTypeDisplayName(updateType: String): String {
        return when (updateType.uppercase()) {
            "FULL" -> "تحديث كامل"
            "DIFFERENTIAL" -> "تحديث تفاضلي"
            "INSTALLATION" -> "تثبيت"
            "ROLLBACK" -> "استرجاع"
            else -> updateType
        }
    }

    /**
     * Get warning level display name in Arabic
     */
    fun getWarningLevelDisplayName(warningLevel: String): String {
        return when (warningLevel.uppercase()) {
            "NONE" -> "لا توجد تحذيرات"
            "INFO" -> "معلومات"
            "WARNING" -> "تحذير"
            "CRITICAL" -> "حرج"
            else -> warningLevel
        }
    }

    /**
     * Get compatibility issue severity display name in Arabic
     */
    fun getSeverityDisplayName(severity: String): String {
        return when (severity.uppercase()) {
            "INFO" -> "معلومات"
            "WARNING" -> "تحذير"
            "CRITICAL" -> "حرج"
            else -> severity
        }
    }

    /**
     * Get release channel display name in Arabic
     */
    fun getReleaseChannelDisplayName(channel: String): String {
        return when (channel.uppercase()) {
            "STABLE" -> "مستقر"
            "BETA" -> "تجريبي"
            "NIGHTLY" -> "ليلي"
            "ALPHA" -> "ألفا"
            else -> channel
        }
    }

    /**
     * Generate update notification message
     */
    fun generateUpdateNotificationMessage(updateData: UpdateCheckResponseDTO): String {
        val versionText = "الإصدار ${updateData.latestVersion}"
        val sizeText = "(${updateData.formattedFileSize})"
        val typeText = if (updateData.isMandatory) "تحديث إجباري" else "تحديث اختياري"
        
        return "$typeText متاح: $versionText $sizeText"
    }

    /**
     * Check if update is compatible with current system
     */
    fun isUpdateCompatible(compatibilityInfo: CompatibilityCheckDTO): Boolean {
        return compatibilityInfo.isCompatible && 
               compatibilityInfo.canProceed && 
               compatibilityInfo.warningLevel != "CRITICAL"
    }

    /**
     * Get compatibility issues summary
     */
    fun getCompatibilityIssuesSummary(issues: List<CompatibilityIssueDTO>): String {
        if (issues.isEmpty()) return "لا توجد مشاكل في التوافق"
        
        val criticalCount = issues.count { it.severity == "CRITICAL" }
        val warningCount = issues.count { it.severity == "WARNING" }
        val infoCount = issues.count { it.severity == "INFO" }
        
        val parts = mutableListOf<String>()
        
        if (criticalCount > 0) {
            parts.add("$criticalCount مشكلة حرجة")
        }
        if (warningCount > 0) {
            parts.add("$warningCount تحذير")
        }
        if (infoCount > 0) {
            parts.add("$infoCount معلومة")
        }
        
        return parts.joinToString("، ")
    }

    /**
     * Validate checksum format
     */
    fun isValidChecksum(checksum: String): Boolean {
        val checksumRegex = Regex("""^sha256:[a-fA-F0-9]{64}$""")
        return checksumRegex.matches(checksum)
    }

    /**
     * Extract version from filename
     */
    fun extractVersionFromFilename(filename: String): String? {
        val versionRegex = Regex("""(\d+\.\d+\.\d+)""")
        return versionRegex.find(filename)?.value
    }

    /**
     * Generate backup filename
     */
    fun generateBackupFilename(version: String): String {
        val timestamp = Clock.System.now().epochSeconds
        return "backup-$version-$timestamp.jar"
    }

    /**
     * Check if file exists and is readable
     */
    fun isFileAccessible(filePath: String): Boolean {
        return try {
            val file = java.io.File(filePath)
            file.exists() && file.canRead()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get available disk space in bytes
     */
    fun getAvailableDiskSpace(path: String = "."): Long {
        return try {
            val file = java.io.File(path)
            file.usableSpace
        } catch (e: Exception) {
            0L
        }
    }

    /**
     * Check if there's enough disk space for download
     */
    fun hasEnoughDiskSpace(requiredBytes: Long, path: String = "."): Boolean {
        val availableSpace = getAvailableDiskSpace(path)
        // Add 10% buffer for safety
        val requiredWithBuffer = (requiredBytes * 1.1).toLong()
        return availableSpace >= requiredWithBuffer
    }

    /**
     * Sanitize filename for safe file operations
     */
    fun sanitizeFilename(filename: String): String {
        return filename.replace(Regex("[^a-zA-Z0-9.-]"), "_")
    }

    /**
     * Generate download progress message
     */
    fun generateProgressMessage(progress: DownloadProgress): String {
        val percentage = "%.1f%%".format(progress.percentage)
        val downloaded = formatFileSize(progress.downloadedBytes)
        val total = formatFileSize(progress.totalBytes)
        val speed = if (progress.speedBytesPerSecond > 0) {
            " - ${formatSpeed(progress.speedBytesPerSecond)}"
        } else ""
        val eta = if (progress.estimatedTimeRemainingSeconds > 0) {
            " - متبقي ${formatDuration(progress.estimatedTimeRemainingSeconds)}"
        } else ""
        
        return "تحميل $percentage ($downloaded من $total)$speed$eta"
    }

    /**
     * Validate JAR file integrity
     */
    fun validateJarFile(filePath: String): Boolean {
        return try {
            val file = java.io.File(filePath)
            if (!file.exists() || file.length() == 0L) {
                return false
            }

            // Try to open as ZIP/JAR file
            java.util.zip.ZipFile(file).use { zipFile ->
                // Check for essential JAR components
                val hasManifest = zipFile.getEntry("META-INF/MANIFEST.MF") != null
                val hasClasses = zipFile.entries().asSequence().any {
                    it.name.endsWith(".class")
                }
                hasManifest && hasClasses
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get current application JAR path
     */
    fun getCurrentJarPath(): String? {
        return try {
            // Method 1: Try to get JAR path from class path
            val classPath = System.getProperty("java.class.path")

            // Split class path and look for JAR files
            val classPathEntries = classPath.split(System.getProperty("path.separator"))
            val jarEntry = classPathEntries.find { it.endsWith(".jar") }

            if (jarEntry != null) {
                val jarFile = java.io.File(jarEntry)
                if (jarFile.exists()) {
                    return jarFile.absolutePath
                }
            }

            // Method 2: Try to get from code source
            val codeSource = UpdateUtils::class.java.protectionDomain.codeSource
            if (codeSource != null) {
                val jarPath = java.io.File(codeSource.location.toURI())
                if (jarPath.exists() && jarPath.name.endsWith(".jar")) {
                    return jarPath.absolutePath
                }
            }

            // Method 3: Development mode - return null to indicate dev mode
            if (isDevelopmentMode()) {
                return null // Indicates development mode
            }

            null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Check if running in development mode
     */
    fun isDevelopmentMode(): Boolean {
        return try {
            val classPath = System.getProperty("java.class.path")
            classPath.contains("build/classes") ||
            classPath.contains("target/classes") ||
            classPath.contains("out/production") ||
            !classPath.contains(".jar")
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Create safe temporary file for update operations
     */
    fun createTempUpdateFile(version: String): java.io.File? {
        return try {
            val tempDir = java.io.File(System.getProperty("java.io.tmpdir"), "sales-management-updates")
            if (!tempDir.exists()) {
                tempDir.mkdirs()
            }

            val tempFile = java.io.File(tempDir, "update-$version-${System.currentTimeMillis()}.jar")
            tempFile.createNewFile()
            tempFile
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Clean up temporary update files
     */
    fun cleanupTempFiles() {
        try {
            val tempDir = java.io.File(System.getProperty("java.io.tmpdir"), "sales-management-updates")
            if (tempDir.exists() && tempDir.isDirectory) {
                tempDir.listFiles()?.forEach { file ->
                    try {
                        // Delete files older than 24 hours
                        val ageHours = (System.currentTimeMillis() - file.lastModified()) / (1000 * 60 * 60)
                        if (ageHours > 24) {
                            file.delete()
                        }
                    } catch (e: Exception) {
                        // Ignore individual file deletion errors
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore cleanup errors
        }
    }

    /**
     * Generate restart command for current platform
     */
    fun generateRestartCommand(): List<String>? {
        return try {
            val currentJar = getCurrentJarPath() ?: return null
            val javaExecutable = System.getProperty("java.home") +
                if (System.getProperty("os.name").lowercase().contains("windows")) {
                    "/bin/java.exe"
                } else {
                    "/bin/java"
                }

            listOf(javaExecutable, "-jar", currentJar)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Check if application restart is supported on current platform
     */
    fun isRestartSupported(): Boolean {
        return try {
            // In development mode, restart is "supported" but will be simulated
            if (isDevelopmentMode()) {
                return true
            }

            getCurrentJarPath() != null &&
            System.getProperty("java.home") != null &&
            java.io.File(System.getProperty("java.home")).exists()
        } catch (e: Exception) {
            false
        }
    }
}
