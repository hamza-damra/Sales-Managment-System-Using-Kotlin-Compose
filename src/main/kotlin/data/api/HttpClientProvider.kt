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
                level = LogLevel.INFO
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

                        if (accessToken != null) {
                            println("✅ HTTP Client - Using Bearer tokens for request")
                            BearerTokens(accessToken, refreshToken ?: "")
                        } else {
                            println("❌ HTTP Client - No access token available")
                            null
                        }
                    }
                    
                    refreshTokens {
                        val refreshToken = tokenManager.getRefreshToken()
                        if (refreshToken != null) {
                            try {
                                val newTokens = tokenManager.refreshToken(refreshToken)
                                if (newTokens != null) {
                                    BearerTokens(newTokens.accessToken, newTokens.refreshToken)
                                } else {
                                    null
                                }
                            } catch (e: Exception) {
                                null
                            }
                        } else {
                            null
                        }
                    }
                }
            }
            
            // Default Headers
            defaultRequest {
                header(HttpHeaders.ContentType, ApiConfig.Http.CONTENT_TYPE)
                header(HttpHeaders.Accept, ApiConfig.Http.ACCEPT)
            }
            
            // Error Handling
            HttpResponseValidator {
                validateResponse { response ->
                    when (response.status.value) {
                        in 300..399 -> throw RedirectResponseException(response, "Redirect")
                        in 400..499 -> throw ClientRequestException(response, "Client error")
                        in 500..599 -> throw ServerResponseException(response, "Server error")
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
