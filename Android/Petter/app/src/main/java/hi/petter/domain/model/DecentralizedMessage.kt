package hi.petter.domain.model

import java.util.Date

data class DecentralizedMessage(
    val id: String,
    val type: DecentralizedMessageType = DecentralizedMessageType.TEXT,
    val content: String,
    val senderId: String,
    val senderName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val groupId: String? = null,
    val receiverId: String? = null,
    val status: Message.MessageStatus = Message.MessageStatus.SENT,
    val metadata: MessageMetadata = MessageMetadata(),
    val encryptedContent: String? = null, // 端到端加密内容
    val signature: String? = null,      // 消息签名
    val replyTo: String? = null,     // 回复消息ID
    val reactions: List<MessageReaction> = emptyList(),
    val attachments: List<MessageAttachment> = emptyList()
)

data class MessageMetadata(
    val deviceId: String = "",
    val messageId: String = "",
    val parentMessageId: String? = null,
    val threadId: String? = null,
    val priority: MessagePriority = MessagePriority.NORMAL,
    val tags: Set<String> = emptySet(),
    val location: LocationInfo? = null,
    val deliveryConfirmation: Boolean = false
)

// 临时禁用这些类以避免与CommonTypes.kt冲突
/*
data class MessageReaction(
    val userId: String,
    val emoji: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class MessageAttachment(
    val id: String,
    val type: AttachmentType,
    val name: String,
    val url: String,
    val size: Long = 0L,
    val thumbnailUrl: String? = null,
    val duration: Long? = null, // for audio/video
    val metadata: Map<String, String> = emptyMap()
)
*/

data class LocationInfo(
    val latitude: Double,
    val longitude: Double,
    val address: String? = null,
    val accuracy: Float? = null
)

enum class MessagePriority {
    LOW, NORMAL, HIGH, URGENT
}

enum class AttachmentType {
    IMAGE, VIDEO, AUDIO, FILE, VOICE_NOTE, LOCATION, CONTACT
}

// 扩展MessageType - 重命名以避免冲突
enum class DecentralizedMessageType {
    TEXT, IMAGE, VIDEO, AUDIO, FILE, LOCATION, CONTACT, SYSTEM, REACTION, TYPING
}