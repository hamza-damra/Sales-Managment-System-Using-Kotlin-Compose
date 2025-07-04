package data.auth

import kotlinx.serialization.Serializable

// Authentication Request DTOs
@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class SignupRequest(
    val username: String,
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val role: String = "USER" // USER, ADMIN, MANAGER
)

@Serializable
data class RefreshTokenRequest(
    val refreshToken: String
)

// Authentication Response DTOs
@Serializable
data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val user: UserDTO? = null
)

@Serializable
data class TokenResponse(
    val accessToken: String,
    val refreshToken: String
)

@Serializable
data class UserDTO(
    val id: Long,
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val role: String,
    val createdAt: String
)

// User roles enum
enum class UserRole(val value: String) {
    USER("USER"),
    ADMIN("ADMIN"),
    MANAGER("MANAGER");
    
    companion object {
        fun fromString(value: String): UserRole {
            return values().find { it.value == value } ?: USER
        }
    }
}

// Authentication state
data class AuthState(
    val isAuthenticated: Boolean = false,
    val user: UserDTO? = null,
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
