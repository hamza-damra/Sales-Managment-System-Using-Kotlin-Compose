package data.api.services

import data.api.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import utils.Constants

/**
 * API service for update system operations
 * Implements HTTP-based communication following the Update System API documentation
 */
class UpdateApiService(private val httpClient: HttpClient) {
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        encodeDefaults = true
    }

    /**
     * Check for available updates
     */
    suspend fun checkForUpdates(currentVersion: String): NetworkResult<UpdateCheckResponseDTO> {
        println("🔄 UpdateApiService - Checking for updates, current version: $currentVersion")
        
        return safeApiCall {
            val response = httpClient.get(ApiConfig.Endpoints.UPDATES_CHECK) {
                parameter("currentVersion", currentVersion)
            }
            
            println("✅ Update check response status: ${response.status}")
            
            val responseText = response.body<String>()
            println("📄 Update check response: $responseText")
            
            val apiResponse = json.decodeFromString<UpdateApiResponse<UpdateCheckResponseDTO>>(responseText)
            
            if (apiResponse.success && apiResponse.data != null) {
                println("✅ Update check successful: updateAvailable=${apiResponse.data.updateAvailable}")
                apiResponse.data
            } else {
                throw Exception("Update check failed: ${apiResponse.message}")
            }
        }
    }

    /**
     * Get latest version information
     */
    suspend fun getLatestVersion(): NetworkResult<ApplicationVersionDTO> {
        println("🔄 UpdateApiService - Getting latest version info")
        
        return safeApiCall {
            val response = httpClient.get(ApiConfig.Endpoints.UPDATES_LATEST)
            
            println("✅ Latest version response status: ${response.status}")
            
            val responseText = response.body<String>()
            val apiResponse = json.decodeFromString<UpdateApiResponse<ApplicationVersionDTO>>(responseText)
            
            if (apiResponse.success && apiResponse.data != null) {
                println("✅ Latest version retrieved: ${apiResponse.data.versionNumber}")
                apiResponse.data
            } else {
                throw Exception("Failed to get latest version: ${apiResponse.message}")
            }
        }
    }

    /**
     * Get version metadata
     */
    suspend fun getVersionMetadata(version: String): NetworkResult<ApplicationVersionDTO> {
        println("🔄 UpdateApiService - Getting version metadata for: $version")
        
        return safeApiCall {
            val response = httpClient.get(ApiConfig.Endpoints.updateVersion(version))
            
            val responseText = response.body<String>()
            val apiResponse = json.decodeFromString<UpdateApiResponse<ApplicationVersionDTO>>(responseText)
            
            if (apiResponse.success && apiResponse.data != null) {
                println("✅ Version metadata retrieved for: $version")
                apiResponse.data
            } else {
                throw Exception("Failed to get version metadata: ${apiResponse.message}")
            }
        }
    }

    /**
     * Check system compatibility
     */
    suspend fun checkCompatibility(
        version: String,
        systemInfo: SystemInfo
    ): NetworkResult<CompatibilityCheckDTO> {
        println("🔄 UpdateApiService - Checking compatibility for version: $version")
        
        return safeApiCall {
            val response = httpClient.get(ApiConfig.Endpoints.updateCompatibility(version)) {
                parameter("clientVersion", Constants.App.VERSION)
                parameter("os", systemInfo.osName)
                parameter("arch", systemInfo.osArch)
                parameter("javaVersion", systemInfo.javaVersion)
            }
            
            val responseText = response.body<String>()
            val apiResponse = json.decodeFromString<UpdateApiResponse<CompatibilityCheckDTO>>(responseText)
            
            if (apiResponse.success && apiResponse.data != null) {
                println("✅ Compatibility check completed: compatible=${apiResponse.data.isCompatible}")
                apiResponse.data
            } else {
                throw Exception("Compatibility check failed: ${apiResponse.message}")
            }
        }
    }

    /**
     * Get differential update information
     */
    suspend fun getDifferentialUpdate(
        fromVersion: String,
        toVersion: String
    ): NetworkResult<DifferentialUpdateDTO> {
        println("🔄 UpdateApiService - Getting differential update: $fromVersion -> $toVersion")
        
        return safeApiCall {
            val response = httpClient.get(ApiConfig.Endpoints.updateDelta(fromVersion, toVersion))
            
            val responseText = response.body<String>()
            val apiResponse = json.decodeFromString<UpdateApiResponse<DifferentialUpdateDTO>>(responseText)
            
            if (apiResponse.success && apiResponse.data != null) {
                println("✅ Differential update info retrieved: deltaAvailable=${apiResponse.data.deltaAvailable}")
                apiResponse.data
            } else {
                throw Exception("Failed to get differential update info: ${apiResponse.message}")
            }
        }
    }

    /**
     * Download update file with progress tracking
     */
    suspend fun downloadUpdate(
        version: String,
        onProgress: (DownloadProgress) -> Unit
    ): Flow<NetworkResult<ByteArray>> = flow {
        emit(NetworkResult.Loading)
        
        try {
            println("🔄 UpdateApiService - Starting download for version: $version")
            
            val response = httpClient.get(ApiConfig.Endpoints.updateDownload(version))
            
            if (response.status != HttpStatusCode.OK) {
                emit(NetworkResult.Error(ApiException.HttpError(
                    statusCode = response.status.value,
                    statusText = response.status.description,
                    errorBody = "Download failed"
                )))
                return@flow
            }
            
            val contentLength = response.headers[HttpHeaders.ContentLength]?.toLongOrNull() ?: 0L
            println("📦 Download content length: $contentLength bytes")
            
            val chunks = mutableListOf<ByteArray>()
            var downloadedBytes = 0L
            val startTime = System.currentTimeMillis()
            
            response.bodyAsChannel().let { channel ->
                val buffer = ByteArray(Constants.Updates.DOWNLOAD_CHUNK_SIZE)
                
                while (!channel.isClosedForRead) {
                    val bytesRead = channel.readAvailable(buffer, 0, buffer.size)
                    if (bytesRead <= 0) break
                    
                    val chunk = buffer.copyOf(bytesRead)
                    chunks.add(chunk)
                    downloadedBytes += bytesRead
                    
                    // Calculate progress and speed
                    val percentage = if (contentLength > 0) {
                        (downloadedBytes.toFloat() / contentLength.toFloat()) * 100f
                    } else 0f
                    
                    val elapsedTime = System.currentTimeMillis() - startTime
                    val speedBytesPerSecond = if (elapsedTime > 0) {
                        (downloadedBytes * 1000L) / elapsedTime
                    } else 0L
                    
                    val estimatedTimeRemaining = if (speedBytesPerSecond > 0 && contentLength > downloadedBytes) {
                        (contentLength - downloadedBytes) / speedBytesPerSecond
                    } else 0L
                    
                    // Report progress
                    onProgress(DownloadProgress(
                        downloadedBytes = downloadedBytes,
                        totalBytes = contentLength,
                        percentage = percentage,
                        speedBytesPerSecond = speedBytesPerSecond,
                        estimatedTimeRemainingSeconds = estimatedTimeRemaining,
                        isComplete = false
                    ))
                }
            }
            
            // Combine all chunks
            val totalSize = chunks.sumOf { it.size }
            val result = ByteArray(totalSize)
            var offset = 0
            
            chunks.forEach { chunk ->
                chunk.copyInto(result, offset)
                offset += chunk.size
            }
            
            // Final progress update
            onProgress(DownloadProgress(
                downloadedBytes = downloadedBytes,
                totalBytes = contentLength,
                percentage = 100f,
                speedBytesPerSecond = 0L,
                estimatedTimeRemainingSeconds = 0L,
                isComplete = true
            ))
            
            println("✅ Download completed: $downloadedBytes bytes")
            emit(NetworkResult.Success(result))
            
        } catch (e: Exception) {
            println("❌ Download failed: ${e.message}")
            onProgress(DownloadProgress(
                downloadedBytes = 0L,
                totalBytes = 0L,
                percentage = 0f,
                isComplete = false,
                error = e.message
            ))
            emit(NetworkResult.Error(e.toApiException()))
        }
    }

    /**
     * Download differential update
     */
    suspend fun downloadDifferentialUpdate(
        fromVersion: String,
        toVersion: String,
        onProgress: (DownloadProgress) -> Unit
    ): Flow<NetworkResult<ByteArray>> = flow {
        emit(NetworkResult.Loading)
        
        try {
            println("🔄 UpdateApiService - Starting differential download: $fromVersion -> $toVersion")
            
            val response = httpClient.get(ApiConfig.Endpoints.updateDeltaDownload(fromVersion, toVersion))
            
            if (response.status != HttpStatusCode.OK) {
                emit(NetworkResult.Error(ApiException.HttpError(
                    statusCode = response.status.value,
                    statusText = response.status.description,
                    errorBody = "Differential download failed"
                )))
                return@flow
            }
            
            // Use the same download logic as regular updates
            val contentLength = response.headers[HttpHeaders.ContentLength]?.toLongOrNull() ?: 0L
            val result = response.body<ByteArray>()
            
            onProgress(DownloadProgress(
                downloadedBytes = result.size.toLong(),
                totalBytes = contentLength,
                percentage = 100f,
                isComplete = true
            ))
            
            println("✅ Differential download completed: ${result.size} bytes")
            emit(NetworkResult.Success(result))
            
        } catch (e: Exception) {
            println("❌ Differential download failed: ${e.message}")
            onProgress(DownloadProgress(
                downloadedBytes = 0L,
                totalBytes = 0L,
                percentage = 0f,
                isComplete = false,
                error = e.message
            ))
            emit(NetworkResult.Error(e.toApiException()))
        }
    }
}
