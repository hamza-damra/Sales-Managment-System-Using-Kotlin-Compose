package data.auth

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.prefs.Preferences

/**
 * Manages authentication tokens with secure storage and automatic refresh
 */
class TokenManager {
    private val prefs = Preferences.userNodeForPackage(TokenManager::class.java)
    private val mutex = Mutex()
    
    companion object {
        private const val ACCESS_TOKEN_KEY = "access_token"
        private const val REFRESH_TOKEN_KEY = "refresh_token"
        private const val USER_DATA_KEY = "user_data"
        private const val TOKEN_EXPIRY_KEY = "token_expiry"
    }
    
    private var _accessToken: String? = null
    private var _refreshToken: String? = null
    private var _user: UserDTO? = null
    
    init {
        loadTokensFromStorage()
    }
    
    suspend fun saveTokens(authResponse: AuthResponse) = mutex.withLock {
        _accessToken = authResponse.accessToken
        _refreshToken = authResponse.refreshToken
        _user = authResponse.user
        
        // Save to persistent storage
        prefs.put(ACCESS_TOKEN_KEY, authResponse.accessToken)
        prefs.put(REFRESH_TOKEN_KEY, authResponse.refreshToken)
        
        // Save user data if available
        authResponse.user?.let { user ->
            val userData = "${user.id}|${user.username}|${user.email}|${user.firstName}|${user.lastName}|${user.role}|${user.createdAt}"
            prefs.put(USER_DATA_KEY, userData)
        }
        
        // Save token expiry time (estimate 1 hour for access token)
        val expiryTime = System.currentTimeMillis() + (60 * 60 * 1000) // 1 hour
        prefs.putLong(TOKEN_EXPIRY_KEY, expiryTime)
    }
    
    suspend fun saveTokens(tokenResponse: TokenResponse) = mutex.withLock {
        _accessToken = tokenResponse.accessToken
        _refreshToken = tokenResponse.refreshToken
        
        // Save to persistent storage
        prefs.put(ACCESS_TOKEN_KEY, tokenResponse.accessToken)
        prefs.put(REFRESH_TOKEN_KEY, tokenResponse.refreshToken)
        
        // Update token expiry time
        val expiryTime = System.currentTimeMillis() + (60 * 60 * 1000) // 1 hour
        prefs.putLong(TOKEN_EXPIRY_KEY, expiryTime)
    }
    
    fun getAccessToken(): String? = _accessToken
    
    fun getRefreshToken(): String? = _refreshToken
    
    fun getUser(): UserDTO? = _user
    
    fun isAuthenticated(): Boolean = !_accessToken.isNullOrBlank()
    
    fun isTokenExpired(): Boolean {
        val expiryTime = prefs.getLong(TOKEN_EXPIRY_KEY, 0)
        return System.currentTimeMillis() >= expiryTime
    }
    
    suspend fun refreshToken(refreshToken: String): TokenResponse? {
        // This will be called by the HTTP client when token refresh is needed
        // The actual refresh logic will be handled by AuthService
        // For now, return null to indicate refresh failed
        // The AuthService will handle the actual refresh logic
        return null
    }
    
    suspend fun clearTokens() = mutex.withLock {
        _accessToken = null
        _refreshToken = null
        _user = null
        
        // Clear from persistent storage
        prefs.remove(ACCESS_TOKEN_KEY)
        prefs.remove(REFRESH_TOKEN_KEY)
        prefs.remove(USER_DATA_KEY)
        prefs.remove(TOKEN_EXPIRY_KEY)
    }
    
    private fun loadTokensFromStorage() {
        _accessToken = prefs.get(ACCESS_TOKEN_KEY, null)
        _refreshToken = prefs.get(REFRESH_TOKEN_KEY, null)
        
        // Load user data
        val userData = prefs.get(USER_DATA_KEY, null)
        if (userData != null) {
            try {
                val parts = userData.split("|")
                if (parts.size >= 7) {
                    _user = UserDTO(
                        id = parts[0].toLong(),
                        username = parts[1],
                        email = parts[2],
                        firstName = parts[3],
                        lastName = parts[4],
                        role = parts[5],
                        createdAt = parts[6]
                    )
                }
            } catch (e: Exception) {
                // Invalid user data, clear it
                prefs.remove(USER_DATA_KEY)
            }
        }
    }
    
    fun hasValidTokens(): Boolean {
        return !_accessToken.isNullOrBlank() && !isTokenExpired()
    }
}
