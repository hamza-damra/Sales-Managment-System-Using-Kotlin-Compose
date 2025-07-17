package data.repository

import data.api.*
import data.api.services.UpdateApiService
import data.auth.TokenManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import utils.Constants
import java.security.MessageDigest
import java.util.prefs.Preferences

/**
 * Repository for managing update operations and state
 * Follows the established repository pattern from the codebase
 */
class UpdateRepository(
    private val updateApiService: UpdateApiService,
    private val tokenManager: TokenManager
) {
    private val prefs = Preferences.userNodeForPackage(UpdateRepository::class.java)
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        encodeDefaults = true
    }

    // State management
    private val _updateState = MutableStateFlow(UpdateState(currentVersion = Constants.App.VERSION))
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    private val _updatePreferences = MutableStateFlow(loadPreferences())
    val updatePreferences: StateFlow<UpdatePreferences> = _updatePreferences.asStateFlow()

    private val _updateHistory = MutableStateFlow(loadUpdateHistory())
    val updateHistory: StateFlow<List<UpdateHistoryEntry>> = _updateHistory.asStateFlow()

    companion object {
        private const val PREFERENCES_KEY = "update_preferences"
        private const val HISTORY_KEY = "update_history"
        private const val LAST_CHECK_KEY = "last_update_check"
    }

    /**
     * Check for available updates
     */
    suspend fun checkForUpdates(): NetworkResult<UpdateCheckResponseDTO> {
        println("🔄 UpdateRepository - Checking for updates")
        
        updateState(isChecking = true, error = null)
        
        val result = updateApiService.checkForUpdates(Constants.App.VERSION)
        
        when (result) {
            is NetworkResult.Success -> {
                val updateData = result.data
                updateState(
                    isChecking = false,
                    updateAvailable = updateData.updateAvailable,
                    latestVersion = updateData.latestVersion,
                    isMandatory = updateData.isMandatory,
                    lastCheckTime = getCurrentTimestamp()
                )
                
                // Save last check time
                prefs.put(LAST_CHECK_KEY, getCurrentTimestamp())
                
                println("✅ Update check completed: updateAvailable=${updateData.updateAvailable}")
            }
            is NetworkResult.Error -> {
                updateState(
                    isChecking = false,
                    error = result.exception.message
                )
                println("❌ Update check failed: ${result.exception.message}")
            }
            is NetworkResult.Loading -> {
                // Already handled above
            }
        }
        
        return result
    }

    /**
     * Get latest version information
     */
    suspend fun getLatestVersion(): NetworkResult<ApplicationVersionDTO> {
        println("🔄 UpdateRepository - Getting latest version")
        return updateApiService.getLatestVersion()
    }

    /**
     * Check system compatibility for a version
     */
    suspend fun checkCompatibility(version: String): NetworkResult<CompatibilityCheckDTO> {
        println("🔄 UpdateRepository - Checking compatibility for version: $version")
        val systemInfo = SystemInfo()
        return updateApiService.checkCompatibility(version, systemInfo)
    }

    /**
     * Get differential update information
     */
    suspend fun getDifferentialUpdate(
        fromVersion: String,
        toVersion: String
    ): NetworkResult<DifferentialUpdateDTO> {
        println("🔄 UpdateRepository - Getting differential update: $fromVersion -> $toVersion")
        return updateApiService.getDifferentialUpdate(fromVersion, toVersion)
    }

    /**
     * Download update with progress tracking
     */
    suspend fun downloadUpdate(
        version: String,
        onProgress: (DownloadProgress) -> Unit
    ): Flow<NetworkResult<ByteArray>> {
        println("🔄 UpdateRepository - Starting download for version: $version")
        
        updateState(isDownloading = true, error = null)
        
        return updateApiService.downloadUpdate(version) { progress ->
            updateState(
                downloadProgress = progress.percentage / 100f,
                downloadedBytes = progress.downloadedBytes,
                totalBytes = progress.totalBytes
            )
            onProgress(progress)
            
            if (progress.isComplete) {
                updateState(isDownloading = false)
            }
            
            if (progress.error != null) {
                updateState(isDownloading = false, error = progress.error)
            }
        }
    }

    /**
     * Download differential update
     */
    suspend fun downloadDifferentialUpdate(
        fromVersion: String,
        toVersion: String,
        onProgress: (DownloadProgress) -> Unit
    ): Flow<NetworkResult<ByteArray>> {
        println("🔄 UpdateRepository - Starting differential download: $fromVersion -> $toVersion")
        
        updateState(isDownloading = true, error = null)
        
        return updateApiService.downloadDifferentialUpdate(fromVersion, toVersion) { progress ->
            updateState(
                downloadProgress = progress.percentage / 100f,
                downloadedBytes = progress.downloadedBytes,
                totalBytes = progress.totalBytes
            )
            onProgress(progress)
            
            if (progress.isComplete) {
                updateState(isDownloading = false)
            }
            
            if (progress.error != null) {
                updateState(isDownloading = false, error = progress.error)
            }
        }
    }

    /**
     * Verify file integrity using checksum
     */
    fun verifyFileIntegrity(fileData: ByteArray, expectedChecksum: String): Boolean {
        return try {
            val digest = MessageDigest.getInstance(Constants.Updates.CHECKSUM_ALGORITHM)
            val hash = digest.digest(fileData)
            val calculatedChecksum = hash.joinToString("") { "%02x".format(it) }
            
            val isValid = calculatedChecksum.equals(expectedChecksum, ignoreCase = true)
            println("🔐 File integrity check: ${if (isValid) "PASSED" else "FAILED"}")
            println("   Expected: $expectedChecksum")
            println("   Calculated: $calculatedChecksum")
            
            isValid
        } catch (e: Exception) {
            println("❌ File integrity check failed: ${e.message}")
            false
        }
    }

    /**
     * Update preferences
     */
    fun updatePreferences(preferences: UpdatePreferences) {
        _updatePreferences.value = preferences
        savePreferences(preferences)
        println("💾 Update preferences saved")
    }

    /**
     * Add entry to update history
     */
    fun addToHistory(entry: UpdateHistoryEntry) {
        val currentHistory = _updateHistory.value.toMutableList()
        currentHistory.add(0, entry) // Add to beginning
        
        // Keep only last 50 entries
        if (currentHistory.size > 50) {
            currentHistory.removeAt(currentHistory.size - 1)
        }
        
        _updateHistory.value = currentHistory
        saveUpdateHistory(currentHistory)
        println("📝 Update history entry added: ${entry.version}")
    }

    /**
     * Clear update state
     */
    fun clearUpdateState() {
        _updateState.value = UpdateState(currentVersion = Constants.App.VERSION)
        println("🧹 Update state cleared")
    }

    /**
     * Get system information for compatibility checks
     */
    fun getSystemInfo(): SystemInfo {
        return SystemInfo()
    }

    // Private helper methods
    private fun updateState(
        isChecking: Boolean? = null,
        isDownloading: Boolean? = null,
        isInstalling: Boolean? = null,
        updateAvailable: Boolean? = null,
        latestVersion: String? = null,
        isMandatory: Boolean? = null,
        downloadProgress: Float? = null,
        downloadedBytes: Long? = null,
        totalBytes: Long? = null,
        error: String? = null,
        lastCheckTime: String? = null,
        nextCheckTime: String? = null
    ) {
        val current = _updateState.value
        _updateState.value = current.copy(
            isChecking = isChecking ?: current.isChecking,
            isDownloading = isDownloading ?: current.isDownloading,
            isInstalling = isInstalling ?: current.isInstalling,
            updateAvailable = updateAvailable ?: current.updateAvailable,
            latestVersion = latestVersion ?: current.latestVersion,
            isMandatory = isMandatory ?: current.isMandatory,
            downloadProgress = downloadProgress ?: current.downloadProgress,
            downloadedBytes = downloadedBytes ?: current.downloadedBytes,
            totalBytes = totalBytes ?: current.totalBytes,
            error = error ?: current.error,
            lastCheckTime = lastCheckTime ?: current.lastCheckTime,
            nextCheckTime = nextCheckTime ?: current.nextCheckTime
        )
    }

    private fun loadPreferences(): UpdatePreferences {
        return try {
            val prefsJson = prefs.get(PREFERENCES_KEY, null)
            if (prefsJson != null) {
                json.decodeFromString<UpdatePreferences>(prefsJson)
            } else {
                UpdatePreferences()
            }
        } catch (e: Exception) {
            println("⚠️ Failed to load update preferences: ${e.message}")
            UpdatePreferences()
        }
    }

    private fun savePreferences(preferences: UpdatePreferences) {
        try {
            val prefsJson = json.encodeToString(preferences)
            prefs.put(PREFERENCES_KEY, prefsJson)
        } catch (e: Exception) {
            println("❌ Failed to save update preferences: ${e.message}")
        }
    }

    private fun loadUpdateHistory(): List<UpdateHistoryEntry> {
        return try {
            val historyJson = prefs.get(HISTORY_KEY, null)
            if (historyJson != null) {
                json.decodeFromString<List<UpdateHistoryEntry>>(historyJson)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            println("⚠️ Failed to load update history: ${e.message}")
            emptyList()
        }
    }

    private fun saveUpdateHistory(history: List<UpdateHistoryEntry>) {
        try {
            val historyJson = json.encodeToString(history)
            prefs.put(HISTORY_KEY, historyJson)
        } catch (e: Exception) {
            println("❌ Failed to save update history: ${e.message}")
        }
    }

    private fun getCurrentTimestamp(): String {
        return kotlinx.datetime.Clock.System.now().toString()
    }
}
