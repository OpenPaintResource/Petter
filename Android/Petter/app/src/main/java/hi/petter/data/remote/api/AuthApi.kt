package hi.petter.data.remote.api

import retrofit2.http.Body
import retrofit2.http.POST

/**
 * 认证相关的API接口
 * 简化实现，主要用于保持架构完整性
 */
interface AuthApi {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse
}

data class LoginRequest(
    val username: String,
    val password: String
)

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)

data class AuthResponse(
    val success: Boolean,
    val message: String,
    val user: hi.petter.domain.model.User? = null
)