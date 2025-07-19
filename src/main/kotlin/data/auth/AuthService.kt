package data.auth

import data.api.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import utils.I18nManager

/**
 * Authentication service for handling login, signup, and token management
 */
class AuthService(
    private val httpClient: HttpClient,
    private val tokenManager: TokenManager
) {
    private val _authState = MutableStateFlow(
        AuthState(
            isAuthenticated = tokenManager.isAuthenticated() && tokenManager.hasValidTokens(),
            user = tokenManager.getUser()
        )
    )
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    suspend fun login(username: String, password: String): NetworkResult<AuthResponse> {
        _authState.value = _authState.value.copy(isLoading = true, error = null)

        println("🔐 Attempting login for user: $username")
        println("📡 Login URL: ${ApiConfig.BASE_URL}${ApiConfig.Endpoints.AUTH_LOGIN}")

        return safeApiCall {
            val loginRequest = LoginRequest(username, password)
            println("📤 Login request: $loginRequest")

            val fullUrl = "${ApiConfig.BASE_URL}${ApiConfig.Endpoints.AUTH_LOGIN}"
            println("📡 Full URL: $fullUrl")

            val response = httpClient.post(fullUrl) {
                contentType(ContentType.Application.Json)
                setBody(loginRequest)
            }

            println("📥 Login response status: ${response.status}")

            val authResponse = response.body<AuthResponse>()
            println("✅ Login successful for user: ${authResponse.user?.username}")
            println("🔍 AuthResponse User Data: ${authResponse.user}")
            println("🔍 AuthResponse User Username: ${authResponse.user?.username}")
            println("🔍 AuthResponse User FirstName: ${authResponse.user?.firstName}")
            println("🔍 AuthResponse User LastName: ${authResponse.user?.lastName}")
            println("🔍 AuthResponse User Email: ${authResponse.user?.email}")
            println("🔍 AuthResponse User Role: ${authResponse.user?.role}")

            tokenManager.saveTokens(authResponse)
            println("🔍 AuthService - Tokens saved to TokenManager")

            _authState.value = AuthState(
                isAuthenticated = true,
                user = authResponse.user,
                accessToken = authResponse.accessToken,
                refreshToken = authResponse.refreshToken,
                isLoading = false
            )
            println("🔍 AuthService - Auth state updated with user: ${authResponse.user?.username}")

            authResponse
        }.also { result ->
            if (result.isError) {
                val error = (result as NetworkResult.Error).exception
                val errorMessage = when (error) {
                    is ApiException.NetworkError -> I18nManager.getString("auth.error.network")
                    is ApiException.HttpError -> {
                        when (error.statusCode) {
                            401 -> I18nManager.getString("auth.error.invalid_credentials")
                            403 -> I18nManager.getString("auth.error.access_forbidden")
                            else -> error.errorBody ?: I18nManager.getString("error.server")
                        }
                    }
                    is ApiException.AuthenticationError -> I18nManager.getString("auth.error.invalid_credentials")
                    else -> I18nManager.getString("error.unknown")
                }

                println("❌ Login failed: $errorMessage")

                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = errorMessage
                )
            }
        }
    }
    
    suspend fun signup(
        username: String,
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        role: String = "USER"
    ): NetworkResult<AuthResponse> {
        _authState.value = _authState.value.copy(isLoading = true, error = null)

        println("🔐 Starting signup process...")
        println("📤 Signup data: username=$username, email=$email, firstName=$firstName, lastName=$lastName")

        return safeApiCall {
            val signupRequest = SignupRequest(username, email, password, firstName, lastName, role)
            val fullUrl = "${ApiConfig.BASE_URL}${ApiConfig.Endpoints.AUTH_SIGNUP}"

            println("📡 Signup URL: $fullUrl")
            println("📦 Signup request: $signupRequest")

            // Debug: Show the actual JSON being sent
            try {
                val json = Json.encodeToString(SignupRequest.serializer(), signupRequest)
                println("📄 JSON payload: $json")
            } catch (e: Exception) {
                println("⚠️ Could not serialize to JSON: ${e.message}")
            }

            val response = httpClient.post(fullUrl) {
                contentType(ContentType.Application.Json)
                setBody(signupRequest)
            }

            println("📥 Signup response status: ${response.status}")

            val authResponse = response.body<AuthResponse>()
            println("✅ Signup successful for user: ${authResponse.user?.username}")
            println("🔑 Access Token received: ${authResponse.accessToken.take(30)}...")
            println("🔄 Refresh Token received: ${authResponse.refreshToken.take(30)}...")

            tokenManager.saveTokens(authResponse)
            println("💾 Tokens saved to TokenManager")

            // Verify tokens were saved
            val savedAccessToken = tokenManager.getAccessToken()
            val savedRefreshToken = tokenManager.getRefreshToken()
            println("🔍 Verification - Access Token saved: ${savedAccessToken?.take(30)}...")
            println("🔍 Verification - Refresh Token saved: ${savedRefreshToken?.take(30)}...")
            println("🔍 Verification - Is Authenticated: ${tokenManager.isAuthenticated()}")

            _authState.value = AuthState(
                isAuthenticated = true,
                user = authResponse.user,
                accessToken = authResponse.accessToken,
                refreshToken = authResponse.refreshToken,
                isLoading = false
            )

            println("🎯 Auth State updated - isAuthenticated: true")

            authResponse
        }.also { result ->
            when (result) {
                is NetworkResult.Error -> {
                    val error = result.exception
                    val errorMessage = when (error) {
                        is ApiException.NetworkError -> I18nManager.getString("auth.error.network")
                        is ApiException.HttpError -> {
                            when (error.statusCode) {
                                400 -> {
                                    // Parse validation errors from response body
                                    when {
                                        error.errorBody?.contains("username", ignoreCase = true) == true ->
                                            I18nManager.getString("auth.error.username_exists")
                                        error.errorBody?.contains("email", ignoreCase = true) == true ->
                                            I18nManager.getString("auth.error.email_exists")
                                        else -> I18nManager.getString("error.validation")
                                    }
                                }
                                401 -> I18nManager.getString("auth.error.invalid_credentials")
                                403 -> I18nManager.getString("auth.error.access_forbidden")
                                else -> error.errorBody ?: I18nManager.getString("error.server")
                            }
                        }
                        is ApiException.ValidationError -> I18nManager.getString("error.validation")
                        else -> I18nManager.getString("error.unknown")
                    }

                    println("❌ Signup failed: $errorMessage")
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = errorMessage
                    )
                }
                is NetworkResult.Success -> {
                    println("✅ Signup completed successfully")
                }
                is NetworkResult.Loading -> {
                    println("⏳ Signup still loading...")
                }
            }
        }
    }
    
    suspend fun refreshToken(): NetworkResult<TokenResponse> {
        val refreshToken = tokenManager.getRefreshToken()
            ?: return NetworkResult.Error(ApiException.AuthenticationError("No refresh token available"))
        
        return safeApiCall {
            val fullUrl = "${ApiConfig.BASE_URL}${ApiConfig.Endpoints.AUTH_REFRESH}"
            val response = httpClient.post(fullUrl) {
                contentType(ContentType.Application.Json)
                setBody(RefreshTokenRequest(refreshToken))
            }
            
            val tokenResponse = response.body<TokenResponse>()
            tokenManager.saveTokens(tokenResponse)
            
            _authState.value = _authState.value.copy(
                accessToken = tokenResponse.accessToken,
                refreshToken = tokenResponse.refreshToken
            )
            
            tokenResponse
        }
    }
    
    suspend fun logout() {
        tokenManager.clearTokens()
        _authState.value = AuthState(
            isAuthenticated = false,
            user = null,
            accessToken = null,
            refreshToken = null,
            isLoading = false,
            error = null
        )
    }
    
    fun getCurrentUser(): UserDTO? {
        val user = tokenManager.getUser()
        println("🔍 AuthService.getCurrentUser() - TokenManager user: $user")

        // If TokenManager doesn't have user, try from current auth state
        if (user == null) {
            val stateUser = _authState.value.user
            println("🔍 AuthService.getCurrentUser() - AuthState user: $stateUser")
            return stateUser
        }

        return user
    }

    fun isAuthenticated(): Boolean = tokenManager.isAuthenticated() && tokenManager.hasValidTokens()
    
    fun getUserRole(): UserRole {
        val user = getCurrentUser()
        return if (user != null) {
            UserRole.fromString(user.role)
        } else {
            UserRole.USER
        }
    }
    
    fun hasRole(requiredRole: UserRole): Boolean {
        val currentRole = getUserRole()
        return when (requiredRole) {
            UserRole.USER -> true // All roles can access USER level
            UserRole.MANAGER -> currentRole == UserRole.MANAGER || currentRole == UserRole.ADMIN
            UserRole.ADMIN -> currentRole == UserRole.ADMIN
        }
    }

    /**
     * Immediately set the loading state for immediate UI feedback
     * Used to provide instant loading state activation on button click
     */
    fun setLoadingState(isLoading: Boolean) {
        _authState.value = _authState.value.copy(
            isLoading = isLoading,
            error = if (isLoading) null else _authState.value.error
        )
    }
}
