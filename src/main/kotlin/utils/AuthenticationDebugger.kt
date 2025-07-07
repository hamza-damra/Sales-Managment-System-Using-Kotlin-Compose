package utils

import data.auth.TokenManager
import data.auth.AuthService
import data.di.AppDependencies
import kotlinx.coroutines.flow.first

/**
 * Utility class for debugging authentication issues
 */
object AuthenticationDebugger {
    
    /**
     * Comprehensive authentication status check
     */
    suspend fun checkAuthenticationStatus(): AuthenticationStatus {
        val authService = AppDependencies.container.authService
        val tokenManager = AppDependencies.container.tokenManager
        val authState = authService.authState.first()
        
        return AuthenticationStatus(
            isAuthenticated = authState.isAuthenticated,
            hasAccessToken = tokenManager.getAccessToken() != null,
            hasRefreshToken = tokenManager.getRefreshToken() != null,
            isTokenExpired = tokenManager.isTokenExpired(),
            hasValidTokens = tokenManager.hasValidTokens(),
            user = authState.user,
            accessTokenPreview = tokenManager.getAccessToken()?.take(30),
            refreshTokenPreview = tokenManager.getRefreshToken()?.take(30),
            authStateError = authState.error
        )
    }
    
    /**
     * Print detailed authentication debug information
     */
    suspend fun printAuthenticationDebug() {
        val status = checkAuthenticationStatus()
        
        println("=== AUTHENTICATION DEBUG REPORT ===")
        println("🔍 Authentication Status:")
        println("  - Is Authenticated: ${status.isAuthenticated}")
        println("  - Has Access Token: ${status.hasAccessToken}")
        println("  - Has Refresh Token: ${status.hasRefreshToken}")
        println("  - Is Token Expired: ${status.isTokenExpired}")
        println("  - Has Valid Tokens: ${status.hasValidTokens}")
        println("  - Auth State Error: ${status.authStateError ?: "None"}")
        println()
        
        println("🔍 User Information:")
        if (status.user != null) {
            println("  - ID: ${status.user.id}")
            println("  - Username: ${status.user.username}")
            println("  - Email: ${status.user.email}")
            println("  - Name: ${status.user.firstName} ${status.user.lastName}")
            println("  - Role: ${status.user.role}")
        } else {
            println("  - No user data available")
        }
        println()
        
        println("🔍 Token Information:")
        println("  - Access Token Preview: ${status.accessTokenPreview ?: "None"}...")
        println("  - Refresh Token Preview: ${status.refreshTokenPreview ?: "None"}...")
        println()
        
        println("🔍 Recommendations:")
        when {
            !status.isAuthenticated -> {
                println("  ❌ User is not authenticated - Need to login")
            }
            !status.hasAccessToken -> {
                println("  ❌ No access token - Need to login")
            }
            status.isTokenExpired && status.hasRefreshToken -> {
                println("  ⏰ Token expired but refresh token available - Should auto-refresh")
            }
            status.isTokenExpired && !status.hasRefreshToken -> {
                println("  ❌ Token expired and no refresh token - Need to login")
            }
            status.hasValidTokens -> {
                println("  ✅ Authentication looks good - Check backend connectivity")
            }
            else -> {
                println("  ⚠️ Unknown authentication state - Check token manager")
            }
        }
        println("=====================================")
    }
    
    /**
     * Test authentication by attempting a simple API call
     */
    suspend fun testAuthentication(): AuthenticationTestResult {
        return try {
            val status = checkAuthenticationStatus()
            
            if (!status.isAuthenticated) {
                return AuthenticationTestResult(
                    success = false,
                    error = "User is not authenticated",
                    recommendation = "Please login first"
                )
            }
            
            if (!status.hasValidTokens) {
                return AuthenticationTestResult(
                    success = false,
                    error = "No valid tokens available",
                    recommendation = "Please login again"
                )
            }
            
            // Try to make a simple authenticated API call
            val dashboardService = AppDependencies.container.dashboardApiService
            val result = dashboardService.getDashboardSummary()
            
            if (result.isSuccess) {
                AuthenticationTestResult(
                    success = true,
                    error = null,
                    recommendation = "Authentication is working correctly"
                )
            } else {
                val error = (result as data.api.NetworkResult.Error).exception
                when (error) {
                    is data.api.ApiException.AuthenticationError -> {
                        AuthenticationTestResult(
                            success = false,
                            error = "Authentication failed: ${error.message}",
                            recommendation = "Please login again - tokens may be invalid"
                        )
                    }
                    is data.api.ApiException.HttpError -> {
                        if (error.statusCode == 401) {
                            AuthenticationTestResult(
                                success = false,
                                error = "HTTP 401 Unauthorized: ${error.statusText}",
                                recommendation = "Authentication tokens are invalid - please login again"
                            )
                        } else if (error.statusCode == 404) {
                            AuthenticationTestResult(
                                success = false,
                                error = "HTTP 404 Not Found: ${error.statusText}",
                                recommendation = "Backend endpoint not found - check if backend is running"
                            )
                        } else {
                            AuthenticationTestResult(
                                success = false,
                                error = "HTTP ${error.statusCode}: ${error.statusText}",
                                recommendation = "Check backend connectivity and API endpoints"
                            )
                        }
                    }
                    else -> {
                        AuthenticationTestResult(
                            success = false,
                            error = "API call failed: ${error.message}",
                            recommendation = "Check backend connectivity"
                        )
                    }
                }
            }
        } catch (e: Exception) {
            AuthenticationTestResult(
                success = false,
                error = "Test failed with exception: ${e.message}",
                recommendation = "Check application configuration and backend connectivity"
            )
        }
    }
    
    /**
     * Force clear all authentication data
     */
    suspend fun clearAuthentication() {
        println("🔄 Clearing all authentication data...")
        val authService = AppDependencies.container.authService
        authService.logout()
        println("✅ Authentication data cleared - user should see login screen")
    }
}

/**
 * Data class representing authentication status
 */
data class AuthenticationStatus(
    val isAuthenticated: Boolean,
    val hasAccessToken: Boolean,
    val hasRefreshToken: Boolean,
    val isTokenExpired: Boolean,
    val hasValidTokens: Boolean,
    val user: data.auth.UserDTO?,
    val accessTokenPreview: String?,
    val refreshTokenPreview: String?,
    val authStateError: String?
)

/**
 * Data class representing authentication test result
 */
data class AuthenticationTestResult(
    val success: Boolean,
    val error: String?,
    val recommendation: String
)
