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

    data class ForeignKeyConstraintError(
        val constraintName: String,
        val referencedTable: String,
        val originalMessage: String
    ) : ApiException("Cannot delete: Record has related data in $referencedTable")

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
            val statusCode = response.status.value
            val statusText = response.status.description
            val url = response.call.request.url.toString()

            println("📡 HTTP Client Error: $statusCode $statusText for URL: $url")

            when (statusCode) {
                HttpStatusCode.Unauthorized.value -> {
                    println("🔐 Authentication Error (401) - Token invalid, expired, or missing")
                    ApiException.AuthenticationError("Authentication failed - Token invalid, expired, or missing. Please login again.")
                }
                HttpStatusCode.Forbidden.value -> {
                    println("🚫 Authorization Error (403) - Access forbidden")
                    ApiException.AuthenticationError("Access forbidden - Insufficient permissions for this operation")
                }
                HttpStatusCode.BadRequest.value -> {
                    println("⚠️ Validation Error (400) - Bad request")
                    ApiException.ValidationError(emptyMap()) // TODO: Parse validation errors from response body
                }
                HttpStatusCode.Conflict.value -> {
                    println("⚠️ Conflict Error (409) - Data integrity violation")
                    val errorMessage = message ?: ""
                    println("🔍 409 Error message: $errorMessage")

                    // Check if it's a foreign key constraint error
                    if (errorMessage.contains("Cannot delete customer because they have", ignoreCase = true) ||
                        errorMessage.contains("CUSTOMER_HAS_SALES", ignoreCase = true) ||
                        errorMessage.contains("Data Integrity Violation", ignoreCase = true)) {

                        println("✅ Detected foreign key constraint violation")

                        val referencedTable = when {
                            errorMessage.contains("sale", ignoreCase = true) -> "sales"
                            errorMessage.contains("return", ignoreCase = true) -> "returns"
                            else -> "related records"
                        }

                        println("🔍 Referenced table: $referencedTable")

                        ApiException.ForeignKeyConstraintError(
                            constraintName = "CUSTOMER_HAS_SALES",
                            referencedTable = referencedTable,
                            originalMessage = errorMessage
                        )
                    } else {
                        println("❌ Not a recognized foreign key constraint pattern")
                        ApiException.HttpError(409, "Conflict", errorMessage)
                    }
                }
                HttpStatusCode.NotFound.value -> {
                    println("🔍 Not Found Error (404) - Endpoint not found: $url")
                    if (url.contains("/api/")) {
                        ApiException.HttpError(404, "API endpoint not found", "The endpoint '$url' does not exist. Check if the backend is running and the endpoint is implemented.")
                    } else {
                        ApiException.HttpError(404, "Resource not found", "The requested resource was not found")
                    }
                }
                else -> {
                    println("⚠️ Client Error ($statusCode) - $statusText")
                    ApiException.HttpError(
                        statusCode = statusCode,
                        statusText = statusText,
                        errorBody = message
                    )
                }
            }
        }
        is ServerResponseException -> {
            println("🔥 Server Error: ${response.status.value} ${response.status.description}")

            // Check if it's a foreign key constraint error
            val errorMessage = message ?: ""
            if (errorMessage.contains("foreign key constraint", ignoreCase = true) ||
                errorMessage.contains("constraint", ignoreCase = true) &&
                (errorMessage.contains("returns", ignoreCase = true) ||
                 errorMessage.contains("sales", ignoreCase = true))) {

                val referencedTable = when {
                    errorMessage.contains("returns", ignoreCase = true) -> "returns"
                    errorMessage.contains("sales", ignoreCase = true) -> "sales"
                    else -> "related records"
                }

                val constraintName = extractConstraintName(errorMessage)
                ApiException.ForeignKeyConstraintError(
                    constraintName = constraintName,
                    referencedTable = referencedTable,
                    originalMessage = errorMessage
                )
            } else {
                ApiException.ServerError("Server error: ${response.status.description}")
            }
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
 * Helper function to extract constraint name from error message
 */
private fun extractConstraintName(errorMessage: String): String {
    // Try to extract constraint name from common patterns
    val patterns = listOf(
        "constraint `([^`]+)`".toRegex(),
        "constraint \"([^\"]+)\"".toRegex(),
        "constraint '([^']+)'".toRegex(),
        "constraint ([a-zA-Z0-9_]+)".toRegex()
    )

    for (pattern in patterns) {
        val match = pattern.find(errorMessage)
        if (match != null) {
            return match.groupValues[1]
        }
    }

    return "unknown_constraint"
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
