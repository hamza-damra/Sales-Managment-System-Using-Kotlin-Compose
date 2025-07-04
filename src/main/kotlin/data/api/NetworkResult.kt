package data.api

import io.ktor.client.plugins.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

/**
 * Wrapper class for API responses that handles success, error, and loading states
 */
sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(val exception: ApiException) : NetworkResult<Nothing>()
    object Loading : NetworkResult<Nothing>()
    
    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
    val isLoading: Boolean get() = this is Loading
    
    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }
    
    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw exception
        is Loading -> throw IllegalStateException("Result is still loading")
    }
    
    inline fun onSuccess(action: (T) -> Unit): NetworkResult<T> {
        if (this is Success) action(data)
        return this
    }
    
    inline fun onError(action: (ApiException) -> Unit): NetworkResult<T> {
        if (this is Error) action(exception)
        return this
    }
    
    inline fun onLoading(action: () -> Unit): NetworkResult<T> {
        if (this is Loading) action()
        return this
    }
}

/**
 * Custom exception classes for API errors
 */
sealed class ApiException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause) {
    
    data class NetworkError(
        val originalMessage: String,
        val originalCause: Throwable? = null
    ) : ApiException("Network error: $originalMessage", originalCause)
    
    data class HttpError(
        val statusCode: Int,
        val statusText: String,
        val errorBody: String? = null
    ) : ApiException("HTTP $statusCode: $statusText")
    
    data class AuthenticationError(
        val originalMessage: String
    ) : ApiException("Authentication failed: $originalMessage")
    
    data class ValidationError(
        val errors: Map<String, List<String>>
    ) : ApiException("Validation failed: ${errors.values.flatten().joinToString(", ")}")
    
    data class ServerError(
        val originalMessage: String
    ) : ApiException("Server error: $originalMessage")
    
    data class UnknownError(
        val originalMessage: String,
        val originalCause: Throwable? = null
    ) : ApiException("Unknown error: $originalMessage", originalCause)
}

/**
 * Error response DTO for API error responses
 */
@Serializable
data class ErrorResponse(
    val message: String,
    val status: Int? = null,
    val timestamp: String? = null,
    val path: String? = null,
    val errors: Map<String, List<String>>? = null
)

/**
 * Extension functions to convert exceptions to ApiException
 */
fun Throwable.toApiException(): ApiException {
    println("🔍 Converting exception to ApiException: ${this::class.simpleName} - $message")

    return when (this) {
        is ClientRequestException -> {
            println("📡 HTTP Client Error: ${response.status.value} ${response.status.description}")
            when (response.status.value) {
                HttpStatusCode.Unauthorized.value ->
                    ApiException.AuthenticationError("Invalid username or password")
                HttpStatusCode.BadRequest.value ->
                    ApiException.ValidationError(emptyMap()) // TODO: Parse validation errors
                HttpStatusCode.NotFound.value ->
                    ApiException.HttpError(404, "API endpoint not found", "Check if backend is running and endpoints are correct")
                else ->
                    ApiException.HttpError(
                        statusCode = response.status.value,
                        statusText = response.status.description,
                        errorBody = message
                    )
            }
        }
        is ServerResponseException -> {
            println("🔥 Server Error: ${response.status.value} ${response.status.description}")
            ApiException.ServerError("Server error: ${response.status.description}")
        }
        is RedirectResponseException -> {
            ApiException.HttpError(
                statusCode = response.status.value,
                statusText = response.status.description
            )
        }
        is HttpRequestTimeoutException -> {
            println("⏰ Request timeout")
            ApiException.NetworkError("Request timeout - server may be down", this)
        }
        is java.net.ConnectException -> {
            println("🔌 Connection refused")
            ApiException.NetworkError("Cannot connect to server. Make sure backend is running on localhost:8081", this)
        }
        is java.net.UnknownHostException -> {
            println("🌐 Unknown host")
            ApiException.NetworkError("Cannot resolve server address", this)
        }
        else -> {
            println("❓ Unknown error: ${this::class.simpleName}")
            ApiException.UnknownError(message ?: "Unknown error: ${this::class.simpleName}", this)
        }
    }
}

/**
 * Safe API call wrapper that converts exceptions to NetworkResult
 */
suspend fun <T> safeApiCall(apiCall: suspend () -> T): NetworkResult<T> {
    return try {
        NetworkResult.Success(apiCall())
    } catch (e: Exception) {
        NetworkResult.Error(e.toApiException())
    }
}
