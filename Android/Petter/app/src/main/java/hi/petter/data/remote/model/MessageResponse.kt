package hi.petter.data.remote.model

/**
 * 消息响应数据模型
 */
data class MessageResponse(
    val id: String,
    val type: String,
    val content: String,
    val senderId: String,
    val senderName: String,
    val groupId: String?,
    val receiverId: String?,
    val timestamp: String,
    val status: String
)