package hi.petter.domain.model

import kotlinx.serialization.Serializable
import java.util.Date

@Serializable
data class Message(
    val id: String = "",
    val type: MessageType = MessageType.TEXT,
    val from: String,
    val to: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: MessageStatus = MessageStatus.SENT,
    val metadata: Map<String, String> = emptyMap()
) {
    enum class MessageType {
        TEXT, IMAGE, FILE, SYSTEM
    }

    enum class MessageStatus {
        SENT, DELIVERED, READ, FAILED
    }

    fun getDate(): Date = Date(timestamp)
}

data class ChatMessage(
    val message: Message,
    val isFromMe: Boolean,
    val senderName: String,
    val avatarUrl: String? = null
)