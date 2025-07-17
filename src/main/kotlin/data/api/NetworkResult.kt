package data.api

import io.ktor.client.plugins.*
import io.ktor.client.call.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import utils.ErrorMessageTranslator
import kotlinx.coroutines.runBlocking

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
    val errors: Map<String, List<String>>? = null,
    val error: String? = null,
    val errorCode: String? = null,
    val suggestions: String? = null,
    val validationErrors: Map<String, List<String>>? = null,
    val details: ErrorDetails? = null
)

@Serializable
data class ErrorDetails(
    val resourceId: Long? = null,
    val dependentResource: String? = null,
    val resourceType: String? = null
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
                    val arabicMessage = ErrorMessageTranslator.translateToArabic("Authentication failed - Token invalid, expired, or missing. Please login again.")
                    ApiException.AuthenticationError(arabicMessage)
                }
                HttpStatusCode.Forbidden.value -> {
                    println("🚫 Authorization Error (403) - Access forbidden")
                    val arabicMessage = ErrorMessageTranslator.translateToArabic("Access forbidden - Insufficient permissions for this operation")
                    ApiException.AuthenticationError(arabicMessage)
                }
                HttpStatusCode.BadRequest.value -> {
                    println("⚠️ Validation Error (400) - Bad request")

                    // Try to get the response body for more detailed error information
                    val responseBody = try {
                        runBlocking { response.body<String>() }
                    } catch (e: Exception) {
                        println("⚠️ Could not get response body: ${e.message}")
                        message ?: ""
                    }

                    println("🔍 400 Error response body: $responseBody")

                    // Check for specific promotion endpoint routing issues
                    if (responseBody.contains("Invalid Parameter Type", ignoreCase = true) &&
                        responseBody.contains("Expected a valid long", ignoreCase = true) &&
                        (url.contains("/promotions/expired") || url.contains("/promotions/scheduled"))) {

                        println("🔍 Detected promotion endpoint routing issue")
                        val arabicMessage = "خطأ في توجيه نقطة النهاية للعروض الترويجية. الخادم يفسر 'expired' أو 'scheduled' كمعرف بدلاً من مسار نقطة النهاية. يرجى التحقق من إعدادات الخادم."
                        ApiException.HttpError(400, "Promotion Endpoint Routing Error", arabicMessage)
                    } else {
                        val arabicMessage = ErrorMessageTranslator.translateToArabic("Validation failed")
                        ApiException.ValidationError(emptyMap()) // TODO: Parse validation errors from response body
                    }
                }
                HttpStatusCode.Conflict.value -> {
                    println("⚠️ Conflict Error (409) - Data integrity violation")
                    val errorMessage = message ?: ""
                    println("🔍 409 Error message: $errorMessage")

                    // Try to get the response body for JSON parsing
                    val responseBody = try {
                        runBlocking { response.body<String>() }
                    } catch (e: Exception) {
                        println("⚠️ Could not get response body: ${e.message}")
                        errorMessage
                    }

                    println("🔍 Response body: $responseBody")

                    // Try to parse JSON error response
                    val errorResponse = try {
                        Json.decodeFromString<ErrorResponse>(responseBody)
                    } catch (e: Exception) {
                        println("⚠️ Could not parse JSON error response: ${e.message}")
                        null
                    }

                    if (errorResponse != null) {
                        println("✅ Parsed JSON error response")
                        println("🔍 Error code: ${errorResponse.errorCode}")
                        println("🔍 Error message: ${errorResponse.message}")
                        println("🔍 Suggestions: ${errorResponse.suggestions}")
                        println("🔍 Details: ${errorResponse.details}")

                        // Translate the error message to Arabic
                        val arabicMessage = ErrorMessageTranslator.translateToArabic(
                            errorMessage = errorResponse.message,
                            errorCode = errorResponse.errorCode,
                            suggestions = errorResponse.suggestions,
                            details = errorResponse.details
                        )

                        println("✅ Arabic message: $arabicMessage")

                        // Check if it's a foreign key constraint error or business logic error
                        if (errorResponse.errorCode?.contains("HAS_", ignoreCase = true) == true ||
                            errorResponse.errorCode == "CUSTOMER_HAS_SALES" ||
                            errorResponse.errorCode == "CUSTOMER_HAS_RETURNS" ||
                            errorResponse.errorCode == "BUSINESS_LOGIC_ERROR") {

                            val referencedTable = when {
                                errorResponse.errorCode.contains("SALES", ignoreCase = true) -> "sales"
                                errorResponse.errorCode.contains("RETURNS", ignoreCase = true) -> "returns"
                                errorResponse.errorCode.contains("PRODUCTS", ignoreCase = true) -> "products"
                                else -> "related records"
                            }

                            println("✅ Creating ForeignKeyConstraintError with Arabic message")
                            ApiException.ForeignKeyConstraintError(
                                constraintName = errorResponse.errorCode,
                                referencedTable = referencedTable,
                                originalMessage = arabicMessage
                            )
                        } else {
                            println("⚠️ Creating HttpError with Arabic message")
                            ApiException.HttpError(409, "Conflict", arabicMessage)
                        }
                    } else {
                        // Fallback to original logic if JSON parsing fails
                        // Check both the response body and the original error message
                        val fullErrorText = "$responseBody $errorMessage"

                        if (fullErrorText.contains("Cannot delete customer because they have", ignoreCase = true) ||
                            fullErrorText.contains("CUSTOMER_HAS_SALES", ignoreCase = true) ||
                            fullErrorText.contains("Data Integrity Violation", ignoreCase = true)) {

                            // Extract count from the error message
                            val count = extractNumberFromMessage(fullErrorText)
                            val arabicMessage = if (count != null) {
                                "لا يمكن حذف العميل لأنه مرتبط بـ $count من المبيعات. يرجى إكمال أو إلغاء جميع المبيعات المرتبطة بهذا العميل أولاً."
                            } else {
                                "لا يمكن حذف العميل لأنه مرتبط بمبيعات في النظام. يرجى إكمال أو إلغاء جميع المبيعات المرتبطة بهذا العميل أولاً."
                            }

                            val referencedTable = when {
                                fullErrorText.contains("sale", ignoreCase = true) -> "sales"
                                fullErrorText.contains("return", ignoreCase = true) -> "returns"
                                else -> "related records"
                            }

                            ApiException.ForeignKeyConstraintError(
                                constraintName = "CUSTOMER_HAS_SALES",
                                referencedTable = referencedTable,
                                originalMessage = arabicMessage
                            )
                        } else {
                            val arabicMessage = ErrorMessageTranslator.translateToArabic(fullErrorText)
                            ApiException.HttpError(409, "Conflict", arabicMessage)
                        }
                    }
                }
                HttpStatusCode.NotFound.value -> {
                    println("🔍 Not Found Error (404) - Endpoint not found: $url")
                    val arabicMessage = if (url.contains("/api/")) {
                        "نقطة النهاية '$url' غير موجودة. تأكد من أن الخادم يعمل وأن نقطة النهاية مُنفذة."
                    } else {
                        ErrorMessageTranslator.translateToArabic("Resource not found")
                    }
                    ApiException.HttpError(404, "Not Found", arabicMessage)
                }
                else -> {
                    println("⚠️ Client Error ($statusCode) - $statusText")
                    val arabicMessage = ErrorMessageTranslator.translateToArabic(message ?: statusText)
                    ApiException.HttpError(
                        statusCode = statusCode,
                        statusText = statusText,
                        errorBody = arabicMessage
                    )
                }
            }
        }
        is ServerResponseException -> {
            println("🔥 Server Error: ${response.status.value} ${response.status.description}")

            val errorMessage = message ?: ""

            // Try to parse JSON error response first
            val errorResponse = try {
                Json.decodeFromString<ErrorResponse>(errorMessage)
            } catch (e: Exception) {
                null
            }

            if (errorResponse != null) {
                val arabicMessage = ErrorMessageTranslator.translateToArabic(
                    errorMessage = errorResponse.message,
                    errorCode = errorResponse.errorCode,
                    suggestions = errorResponse.suggestions,
                    details = errorResponse.details
                )
                ApiException.ServerError(arabicMessage)
            } else {
                // Check if it's a foreign key constraint error
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
                    val arabicMessage = ErrorMessageTranslator.translateToArabic(errorMessage)

                    ApiException.ForeignKeyConstraintError(
                        constraintName = constraintName,
                        referencedTable = referencedTable,
                        originalMessage = arabicMessage
                    )
                } else {
                    val arabicMessage = ErrorMessageTranslator.translateToArabic("Server error: ${response.status.description}")
                    ApiException.ServerError(arabicMessage)
                }
            }
        }
        is RedirectResponseException -> {
            val arabicMessage = ErrorMessageTranslator.translateToArabic("Redirect: ${response.status.description}")
            ApiException.HttpError(
                statusCode = response.status.value,
                statusText = response.status.description,
                errorBody = arabicMessage
            )
        }
        is HttpRequestTimeoutException -> {
            println("⏰ Request timeout")
            val arabicMessage = ErrorMessageTranslator.translateToArabic("Request timeout - server may be down")
            ApiException.NetworkError(arabicMessage, this)
        }
        is java.net.ConnectException -> {
            println("🔌 Connection refused")
            val arabicMessage = ErrorMessageTranslator.translateToArabic("Cannot connect to server. Please check your internet connection and try again.")
            ApiException.NetworkError(arabicMessage, this)
        }
        is java.net.UnknownHostException -> {
            println("🌐 Unknown host")
            val arabicMessage = ErrorMessageTranslator.translateToArabic("Cannot resolve server address")
            ApiException.NetworkError(arabicMessage, this)
        }
        else -> {
            println("❓ Unknown error: ${this::class.simpleName}")
            val arabicMessage = ErrorMessageTranslator.translateToArabic(message ?: "Unknown error: ${this::class.simpleName}")
            ApiException.UnknownError(arabicMessage, this)
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
 * Helper function to extract numbers from error messages
 */
private fun extractNumberFromMessage(message: String): String? {
    val patterns = listOf(
        "they have (\\d+) associated".toRegex(),
        "because they have (\\d+)".toRegex(),
        "(\\d+) associated".toRegex(),
        "with (\\d+)".toRegex()
    )

    for (pattern in patterns) {
        val match = pattern.find(message)
        if (match != null) {
            return match.groupValues[1]
        }
    }
    return null
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
