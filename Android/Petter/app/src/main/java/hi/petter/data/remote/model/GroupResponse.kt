package hi.petter.data.remote.model

/**
 * 群组响应数据模型
 */
data class GroupResponse(
    val id: String,
    val name: String,
    val description: String?,
    val createdBy: String,
    val memberCount: Int,
    val isPublic: Boolean,
    val createdAt: String,
    val updatedAt: String
)