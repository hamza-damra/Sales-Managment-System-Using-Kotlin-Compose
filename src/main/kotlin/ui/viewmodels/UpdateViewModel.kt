package ui.viewmodels

import data.api.*
import data.repository.UpdateRepository
import services.UpdateService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import utils.Constants
import java.io.File

/**
 * ViewModel for managing update screen state and operations
 * Follows the established ViewModel pattern from the codebase
 */
class UpdateViewModel(
    private val updateRepository: UpdateRepository,
    private val updateService: UpdateService
) {
    private val viewModelScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // State flows from repository
    val updateState = updateRepository.updateState
    val updatePreferences = updateRepository.updatePreferences
    val updateHistory = updateRepository.updateHistory

    // Service state flows
    val isPollingActive = updateService.isPollingActive
    val lastUpdateCheck = updateService.lastUpdateCheck

    // UI state
    private val _uiState = MutableStateFlow(UpdateUiState())
    val uiState: StateFlow<UpdateUiState> = _uiState.asStateFlow()

    // Latest version info
    private val _latestVersionInfo = MutableStateFlow<ApplicationVersionDTO?>(null)
    val latestVersionInfo: StateFlow<ApplicationVersionDTO?> = _latestVersionInfo.asStateFlow()

    // Compatibility info
    private val _compatibilityInfo = MutableStateFlow<CompatibilityCheckDTO?>(null)
    val compatibilityInfo: StateFlow<CompatibilityCheckDTO?> = _compatibilityInfo.asStateFlow()

    // Download progress
    private val _downloadProgress = MutableStateFlow<DownloadProgress?>(null)
    val downloadProgress: StateFlow<DownloadProgress?> = _downloadProgress.asStateFlow()

    init {
        // Start automatic polling if enabled
        val preferences = updateRepository.updatePreferences.value
        if (preferences.autoCheckEnabled) {
            updateService.startPolling()
        }
    }

    /**
     * Perform manual update check
     */
    fun checkForUpdates() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                val result = updateService.performUpdateCheck()
                
                when (result) {
                    is NetworkResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            lastCheckResult = result.data
                        )
                        
                        // If update is available, get detailed version info
                        if (result.data.updateAvailable) {
                            getLatestVersionInfo()
                        }
                    }
                    is NetworkResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = result.exception.message
                        )
                    }
                    is NetworkResult.Loading -> {
                        // Already handled above
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    /**
     * Get latest version information
     */
    fun getLatestVersionInfo() {
        viewModelScope.launch {
            try {
                val result = updateRepository.getLatestVersion()
                
                when (result) {
                    is NetworkResult.Success -> {
                        _latestVersionInfo.value = result.data
                        
                        // Also check compatibility
                        checkCompatibility(result.data.versionNumber)
                    }
                    is NetworkResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            error = result.exception.message
                        )
                    }
                    is NetworkResult.Loading -> {
                        // Handled by loading state
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message
                )
            }
        }
    }

    /**
     * Check compatibility for a version
     */
    fun checkCompatibility(version: String) {
        viewModelScope.launch {
            try {
                val result = updateRepository.checkCompatibility(version)
                
                when (result) {
                    is NetworkResult.Success -> {
                        _compatibilityInfo.value = result.data
                    }
                    is NetworkResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            error = result.exception.message
                        )
                    }
                    is NetworkResult.Loading -> {
                        // Handled by loading state
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message
                )
            }
        }
    }

    /**
     * Start downloading update
     */
    fun downloadUpdate(version: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isDownloading = true,
                    error = null
                )
                
                updateService.startDownload(version).collect { result ->
                    when (result) {
                        is NetworkResult.Success -> {
                            _uiState.value = _uiState.value.copy(
                                isDownloading = false,
                                downloadedFile = saveDownloadedFile(result.data, version)
                            )
                        }
                        is NetworkResult.Error -> {
                            _uiState.value = _uiState.value.copy(
                                isDownloading = false,
                                error = result.exception.message
                            )
                        }
                        is NetworkResult.Loading -> {
                            // Progress is handled by download progress flow
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isDownloading = false,
                    error = e.message
                )
            }
        }
    }

    /**
     * Install downloaded update
     */
    fun installUpdate(updateFile: File, version: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isInstalling = true,
                    error = null
                )

                val result = updateService.installUpdate(updateFile, version)

                _uiState.value = _uiState.value.copy(
                    isInstalling = false,
                    installationResult = result
                )

                if (result.success) {
                    // Clear downloaded file after successful installation
                    _uiState.value = _uiState.value.copy(downloadedFile = null)
                }

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isInstalling = false,
                    error = e.message
                )
            }
        }
    }

    /**
     * Restart application to apply update
     */
    fun restartApplication() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isRestarting = true,
                    error = null
                )

                val success = updateService.restartApplication()

                if (!success) {
                    _uiState.value = _uiState.value.copy(
                        isRestarting = false,
                        error = "فشل في إعادة تشغيل التطبيق"
                    )
                }

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isRestarting = false,
                    error = e.message
                )
            }
        }
    }

    /**
     * Cancel current download
     */
    fun cancelDownload() {
        updateService.cancelDownload()
        _uiState.value = _uiState.value.copy(
            isDownloading = false,
            error = null
        )
        _downloadProgress.value = null
    }

    /**
     * Update preferences
     */
    fun updatePreferences(preferences: UpdatePreferences) {
        updateRepository.updatePreferences(preferences)
        
        // Start/stop polling based on auto-check setting
        if (preferences.autoCheckEnabled && !updateService.isPollingActive.value) {
            updateService.startPolling()
        } else if (!preferences.autoCheckEnabled && updateService.isPollingActive.value) {
            updateService.stopPolling()
        }
    }

    /**
     * Clear error state
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Clear update state
     */
    fun clearUpdateState() {
        updateRepository.clearUpdateState()
        _uiState.value = UpdateUiState()
        _latestVersionInfo.value = null
        _compatibilityInfo.value = null
        _downloadProgress.value = null
    }

    /**
     * Get system information
     */
    fun getSystemInfo(): SystemInfo {
        return updateRepository.getSystemInfo()
    }

    /**
     * Cleanup resources
     */
    fun cleanup() {
        updateService.cleanup()
        viewModelScope.cancel()
    }

    // Private helper methods
    private fun saveDownloadedFile(data: ByteArray, version: String): File? {
        return try {
            val downloadsDir = File(Constants.Files.TEMP_DIRECTORY, "updates")
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }
            
            val fileName = "sales-management-$version.jar"
            val file = File(downloadsDir, fileName)
            
            file.writeBytes(data)
            file
        } catch (e: Exception) {
            println("❌ UpdateViewModel - Failed to save downloaded file: ${e.message}")
            null
        }
    }
}

/**
 * UI state for the update screen
 */
data class UpdateUiState(
    val isLoading: Boolean = false,
    val isDownloading: Boolean = false,
    val isInstalling: Boolean = false,
    val isRestarting: Boolean = false,
    val error: String? = null,
    val lastCheckResult: UpdateCheckResponseDTO? = null,
    val downloadedFile: File? = null,
    val installationResult: UpdateInstallationResult? = null
)
