package data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data models for the Update System API
 * Following the API documentation specifications exactly
 */

// Core Update DTOs
@Serializable
data class UpdateCheckResponseDTO(
    val updateAvailable: Boolean,
    val latestVersion: String,
    val currentVersion: String,
    val isMandatory: Boolean,
    val releaseNotes: String,
    val downloadUrl: String,
    val fileSize: Long,
    val formattedFileSize: String,
    val checksum: String,
    val minimumClientVersion: String? = null
)

@Serializable
data class ApplicationVersionDTO(
    val id: Long? = null,
    val versionNumber: String,
    val releaseDate: String, // ISO 8601 format
    val isMandatory: Boolean,
    val isActive: Boolean,
    val releaseNotes: String,
    val minimumClientVersion: String? = null,
    val fileName: String,
    val fileSize: Long,
    val formattedFileSize: String,
    val checksum: String,
    val checksumAlgorithm: String = "SHA-256",
    val downloadUrl: String,
    val releaseChannel: String = "STABLE",
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val createdBy: String? = null,
    val downloadCount: Long? = null,
    val successfulDownloads: Long? = null,
    val failedDownloads: Long? = null
)

@Serializable
data class DifferentialUpdateDTO(
    val fromVersion: String,
    val toVersion: String,
    val deltaAvailable: Boolean,
    val deltaSize: Long,
    val formattedDeltaSize: String,
    val fullUpdateSize: Long,
    val formattedFullUpdateSize: String,
    val compressionRatio: Double, // Percentage
    val deltaChecksum: String,
    val deltaDownloadUrl: String,
    val fullDownloadUrl: String,
    val changedFiles: List<ChangedFileDTO>,
    val patchInstructions: List<PatchInstructionDTO> = emptyList(),
    val fallbackToFull: Boolean,
    val fallbackReason: String? = null,
    val estimatedApplyTimeSeconds: Long,
    val createdAt: String,
    val expiresAt: String
)

@Serializable
data class ChangedFileDTO(
    val path: String,
    val operation: String, // "ADDED", "MODIFIED", "DELETED", "MOVED", "RENAMED"
    val size: Long,
    val checksum: String
)

@Serializable
data class PatchInstructionDTO(
    val order: Int,
    val operation: String, // "COPY", "EXTRACT", "DELETE", "MOVE", "VERIFY"
    val target: String,
    val source: String? = null,
    val checksum: String
)

@Serializable
data class CompatibilityCheckDTO(
    val isCompatible: Boolean,
    val targetVersion: String,
    val clientVersion: String,
    val minimumRequiredVersion: String,
    val javaVersion: JavaVersionInfoDTO,
    val operatingSystem: OperatingSystemInfoDTO,
    val systemRequirements: SystemRequirementsDTO,
    val compatibilityIssues: List<CompatibilityIssueDTO>,
    val recommendations: List<String>,
    val canProceed: Boolean,
    val warningLevel: String // "NONE", "INFO", "WARNING", "CRITICAL"
)

@Serializable
data class JavaVersionInfoDTO(
    val required: String,
    val detected: String? = null,
    val isCompatible: Boolean,
    val vendor: String? = null
)

@Serializable
data class OperatingSystemInfoDTO(
    val name: String? = null,
    val version: String? = null,
    val architecture: String? = null,
    val isSupported: Boolean
)

@Serializable
data class SystemRequirementsDTO(
    val minimumMemoryMB: Long,
    val availableMemoryMB: Long? = null,
    val minimumDiskSpaceMB: Long,
    val availableDiskSpaceMB: Long? = null,
    val additionalRequirements: Map<String, String> = emptyMap()
)

@Serializable
data class CompatibilityIssueDTO(
    val type: String, // "JAVA_VERSION", "OPERATING_SYSTEM", "MEMORY", "DISK_SPACE", "ARCHITECTURE", "DEPENDENCY", "CONFIGURATION"
    val severity: String, // "CRITICAL", "WARNING", "INFO"
    val description: String,
    val resolution: String,
    val component: String
)

// API Response Wrappers
@Serializable
data class UpdateApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String,
    val timestamp: String? = null
)

@Serializable
data class UpdateErrorResponse(
    val success: Boolean = false,
    val error: UpdateErrorDetails
)

@Serializable
data class UpdateErrorDetails(
    val status: Int,
    val error: String,
    val message: String,
    val errorCode: String? = null,
    val timestamp: String,
    val suggestions: String? = null
)

// Local Update State Models
@Serializable
data class UpdateState(
    val isChecking: Boolean = false,
    val isDownloading: Boolean = false,
    val isInstalling: Boolean = false,
    val updateAvailable: Boolean = false,
    val currentVersion: String,
    val latestVersion: String? = null,
    val isMandatory: Boolean = false,
    val downloadProgress: Float = 0f,
    val downloadedBytes: Long = 0L,
    val totalBytes: Long = 0L,
    val error: String? = null,
    val lastCheckTime: String? = null,
    val nextCheckTime: String? = null
)

@Serializable
data class UpdatePreferences(
    val autoCheckEnabled: Boolean = true,
    val checkIntervalMinutes: Long = 30L,
    val autoDownloadEnabled: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val allowMeteredConnection: Boolean = false,
    val preferDifferentialUpdates: Boolean = true
)

// Download Progress Models
data class DownloadProgress(
    val downloadedBytes: Long,
    val totalBytes: Long,
    val percentage: Float,
    val speedBytesPerSecond: Long = 0L,
    val estimatedTimeRemainingSeconds: Long = 0L,
    val isComplete: Boolean = false,
    val error: String? = null
)

// Update History Models
@Serializable
data class UpdateHistoryEntry(
    val version: String,
    val updateDate: String,
    val updateType: String, // "FULL", "DIFFERENTIAL", "ROLLBACK"
    val downloadSize: Long,
    val installationTime: Long, // milliseconds
    val success: Boolean,
    val errorMessage: String? = null,
    val releaseNotes: String? = null
)

// System Information for Compatibility Checks
data class SystemInfo(
    val osName: String = System.getProperty("os.name"),
    val osVersion: String = System.getProperty("os.version"),
    val osArch: String = System.getProperty("os.arch"),
    val javaVersion: String = System.getProperty("java.version"),
    val javaVendor: String = System.getProperty("java.vendor"),
    val availableMemoryMB: Long = Runtime.getRuntime().maxMemory() / (1024 * 1024),
    val freeMemoryMB: Long = Runtime.getRuntime().freeMemory() / (1024 * 1024),
    val totalMemoryMB: Long = Runtime.getRuntime().totalMemory() / (1024 * 1024)
)

// Update Installation Result
data class UpdateInstallationResult(
    val success: Boolean,
    val version: String,
    val installationTimeMs: Long,
    val errorMessage: String? = null,
    val requiresRestart: Boolean = true,
    val backupCreated: Boolean = false,
    val backupPath: String? = null
)
