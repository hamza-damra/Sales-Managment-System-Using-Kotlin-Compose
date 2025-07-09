package data.api

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import data.auth.TokenManager

/**
 * HTTP Client provider with configuration for API communication
 */
object HttpClientProvider {
    
    private var _client: HttpClient? = null
    
    fun getClient(tokenManager: TokenManager): HttpClient {
        if (_client == null) {
            _client = createHttpClient(tokenManager)
        }
        return _client!!
    }

    /**
     * Create a simple HTTP client without authentication for testing
     */
    fun create(): HttpClient {
        return HttpClient(CIO) {
            // JSON Configuration
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    encodeDefaults = true
                    prettyPrint = true
                    coerceInputValues = true
                })
            }

            // Logging
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.ALL
                filter { request ->
                    request.url.host.contains("localhost")
                }
            }

            // Timeout Configuration
            install(HttpTimeout) {
                requestTimeoutMillis = ApiConfig.Http.REQUEST_TIMEOUT
                connectTimeoutMillis = ApiConfig.Http.CONNECT_TIMEOUT
                socketTimeoutMillis = ApiConfig.Http.SOCKET_TIMEOUT
            }

            // Default Headers and Base URL
            defaultRequest {
                url(ApiConfig.BASE_URL)
                header(HttpHeaders.ContentType, ApiConfig.Http.CONTENT_TYPE)
                header(HttpHeaders.Accept, ApiConfig.Http.ACCEPT)
            }
        }
    }
    
    private fun createHttpClient(tokenManager: TokenManager): HttpClient {
        return HttpClient(CIO) {
            // JSON Configuration
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    encodeDefaults = true  // Changed to true to include all fields
                    prettyPrint = true
                    coerceInputValues = true
                })
            }
            
            // Logging
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.ALL
                filter { request ->
                    request.url.host.contains("localhost")
                }
            }
            
            // Timeout Configuration
            install(HttpTimeout) {
                requestTimeoutMillis = ApiConfig.Http.REQUEST_TIMEOUT
                connectTimeoutMillis = ApiConfig.Http.CONNECT_TIMEOUT
                socketTimeoutMillis = ApiConfig.Http.SOCKET_TIMEOUT
            }
            
            // Authentication
            install(Auth) {
                bearer {
                    loadTokens {
                        val accessToken = tokenManager.getAccessToken()
                        val refreshToken = tokenManager.getRefreshToken()
                        println("🔐 HTTP Client - Loading tokens...")
                        println("🔐 HTTP Client - Access Token: ${accessToken?.take(30)}...")
                        println("🔐 HTTP Client - Refresh Token: ${refreshToken?.take(30)}...")
                        println("🔐 HTTP Client - Is Authenticated: ${tokenManager.isAuthenticated()}")
                        println("🔐 HTTP Client - Is Token Expired: ${tokenManager.isTokenExpired()}")

                        // Check if tokens are expired and try to refresh
                        if (tokenManager.isTokenExpired() && refreshToken != null) {
                            println("⏰ HTTP Client - Access token is expired, attempting refresh...")
                            try {
                                // Use runBlocking for token refresh in loadTokens
                                val newTokens = kotlinx.coroutines.runBlocking {
                                    tokenManager.refreshToken(refreshToken)
                                }
                                if (newTokens != null) {
                                    println("✅ HTTP Client - Token refreshed successfully in loadTokens")
                                    return@loadTokens BearerTokens(newTokens.accessToken, newTokens.refreshToken)
                                } else {
                                    println("❌ HTTP Client - Token refresh failed in loadTokens")
                                }
                            } catch (e: Exception) {
                                println("❌ HTTP Client - Token refresh exception in loadTokens: ${e.message}")
                            }
                        }

                        if (accessToken != null) {
                            println("✅ HTTP Client - Using Bearer tokens for request")
                            BearerTokens(accessToken, refreshToken ?: "")
                        } else {
                            println("❌ HTTP Client - No access token available - User needs to login")
                            null
                        }
                    }
                    
                    refreshTokens {
                        println("🔄 HTTP Client - Automatic token refresh triggered...")
                        val currentRefreshToken = tokenManager.getRefreshToken()
                        println("🔄 HTTP Client - Refresh token: ${currentRefreshToken?.take(30)}...")

                        if (currentRefreshToken != null) {
                            try {
                                // Use runBlocking for now - this is a limitation of Ktor's auth plugin
                                val newTokens = kotlinx.coroutines.runBlocking {
                                    tokenManager.refreshToken(currentRefreshToken)
                                }
                                if (newTokens != null) {
                                    println("✅ HTTP Client - Automatic token refresh successful")
                                    BearerTokens(newTokens.accessToken, newTokens.refreshToken)
                                } else {
                                    println("❌ HTTP Client - Automatic token refresh failed - User needs to re-login")
                                    null
                                }
                            } catch (e: Exception) {
                                println("❌ HTTP Client - Automatic token refresh exception: ${e.message}")
                                null
                            }
                        } else {
                            println("❌ HTTP Client - No refresh token available for automatic refresh")
                            null
                        }
                    }
                }
            }
            
            // Default Headers and Base URL
            defaultRequest {
                url(ApiConfig.BASE_URL)
                header(HttpHeaders.ContentType, ApiConfig.Http.CONTENT_TYPE)
                header(HttpHeaders.Accept, ApiConfig.Http.ACCEPT)
            }
            
            // Enhanced Error Handling with Authentication Detection
            HttpResponseValidator {
                validateResponse { response ->
                    val statusCode = response.status.value
                    val statusText = response.status.description
                    val url = response.call.request.url.toString()

                    println("🔍 HTTP Response - Status: $statusCode, URL: $url")

                    when (statusCode) {
                        in 300..399 -> {
                            println("🔄 Redirect ($statusCode) - $statusText")
                            throw RedirectResponseException(response, "Redirect: $statusText")
                        }
                        401 -> {
                            println("🔐 Authentication Error (401) - Token invalid or expired for: $url")
                            throw ClientRequestException(response, "Authentication required - Token invalid or expired")
                        }
                        403 -> {
                            println("🚫 Authorization Error (403) - Access forbidden for: $url")
                            throw ClientRequestException(response, "Access forbidden - Insufficient permissions")
                        }
                        404 -> {
                            println("🔍 Not Found Error (404) - Endpoint not found: $url")
                            throw ClientRequestException(response, "API endpoint not found: $url")
                        }
                        in 400..499 -> {
                            println("⚠️ Client Error ($statusCode) - $statusText for: $url")
                            throw ClientRequestException(response, "Client error: $statusText")
                        }
                        in 500..599 -> {
                            println("🔥 Server Error ($statusCode) - $statusText for: $url")
                            throw ServerResponseException(response, "Server error: $statusText")
                        }
                    }
                }
            }
        }
    }
    
    fun closeClient() {
        _client?.close()
        _client = null
    }
}
