package hi.petter.data.remote.model

/**
 * 用户响应数据模型
 */
data class UserResponse(
    val id: String,
    val name: String,
    val email: String?,
    val avatar: String?,
    val status: String,
    val createdAt: String,
    val updatedAt: String
)