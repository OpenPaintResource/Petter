package hi.petter.domain.model

/**
 * 消息类型枚举
 */
enum class MessageType {
    TEXT, IMAGE, FILE, SYSTEM
}

/**
 * 消息状态枚举
 */
enum class MessageStatus {
    PENDING, SENDING, SENT, DELIVERED, READ, FAILED, SENDING_FAILED
}

/**
 * 消息投递状态
 */
enum class MessageDeliveryStatus {
    PENDING, SENDING, SENT, DELIVERED, FAILED, READ
}

/**
 * 消息附件模型
 */
data class MessageAttachment(
    val url: String,
    val fileName: String? = null,
    val fileSize: Long = 0L,
    val type: String = "unknown"
) {
    companion object {
        fun createImageAttachment(url: String): MessageAttachment {
            return MessageAttachment(
                url = url,
                type = "image"
            )
        }

        fun createFileAttachment(url: String, fileName: String, fileSize: Long): MessageAttachment {
            return MessageAttachment(
                url = url,
                fileName = fileName,
                fileSize = fileSize,
                type = "file"
            )
        }
    }
}

/**
 * 消息反应模型
 */
data class MessageReaction(
    val userId: String,
    val emoji: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    companion object {
        val EMOJI_LIST = listOf("👍", "👎", "❤", "😊", "😂", "😄", "😁", "😆", "😅", "😶", "🤣", "🤗", "🤗", "🤠", "💪", "💥", "💦", "🧐", "🐒", "🚶")
    }
}