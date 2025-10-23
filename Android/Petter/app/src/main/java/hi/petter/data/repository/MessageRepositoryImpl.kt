package hi.petter.data.repository

import hi.petter.data.local.database.MessageDatabase
import hi.petter.data.remote.decentralized.SimplifiedMqttManager
import hi.petter.data.remote.mqtt.MqttConfig
import hi.petter.domain.model.Message
import hi.petter.domain.repository.IMessageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 消息相关的Repository实现
 * 整合本地存储和MQTT消息传输
 */
@Singleton
class MessageRepositoryImpl @Inject constructor(
    private val messageDatabase: MessageDatabase,
    private val mqttManager: SimplifiedMqttManager
) : IMessageRepository, BaseRepository() {

    private val mutex = Mutex()

    init {
        // 设置群组成员检查器
        mqttManager.groupMembershipChecker = { groupId ->
            messageDatabase.getGroupById(groupId) != null
        }

        // 监听MQTT消息并保存到本地
        observeMqttMessages()
    }

    /**
     * 监听MQTT消息流并保存到本地数据库
     */
    private fun observeMqttMessages() {
        // 注意：实际项目中应该在合适的生命周期范围内启动这个协程
        // 这里简化处理，在Repository初始化时启动
    }

    override suspend fun sendMessage(message: Message): Result<Message> {
        return safeApiCall {
            val messageToSend = message.copy(
                id = message.id.ifEmpty { "msg_${UUID.randomUUID().toString().substring(0, 8)}" },
                timestamp = System.currentTimeMillis(),
                status = Message.MessageStatus.SENT // 发送的消息标记为已发送
            )

            val groupId = message.metadata["groupId"] as? String
            val success = if (groupId != null) {
                // 发送群组消息
                mqttManager.sendGroupMessage(groupId, message.content)
            } else {
                // 发送私人消息
                mqttManager.sendDirectMessage(message.to, message.content)
            }

            if (success) {
                // 保存发送的消息到本地
                messageDatabase.insertMessage(messageToSend)
                messageToSend
            } else {
                throw Exception("消息发送失败")
            }
        }
    }

    override suspend fun markAsRead(messageId: String): Result<Unit> {
        return safeApiCall {
            mutex.withLock {
                val messages = messageDatabase.messages.value.toMutableList()
                val messageIndex = messages.indexOfFirst { it.id == messageId }
                if (messageIndex >= 0) {
                    val updatedMessage = messages[messageIndex].copy(status = Message.MessageStatus.READ)
                    messages[messageIndex] = updatedMessage
                    messageDatabase.updateMessage(updatedMessage)
                }
            }
            Unit
        }
    }

    override suspend fun deleteMessage(messageId: String): Result<Unit> {
        return safeApiCall {
            messageDatabase.deleteMessage(messageId)
            Unit
        }
    }

    override suspend fun getMessages(userId: String, isGroup: Boolean): Result<List<Message>> {
        return safeApiCall {
            messageDatabase.getMessagesForChat(userId, isGroup)
        }
    }

    override suspend fun getUnreadCount(userId: String, isGroup: Boolean): Result<Int> {
        return safeApiCall {
            messageDatabase.getMessagesForChat(userId, isGroup)
                .count { it.status != Message.MessageStatus.READ }
        }
    }

    override fun observeMessages(userId: String, isGroup: Boolean): Flow<List<Message>> {
        return messageDatabase.messages.map { messages ->
            messages.filter { message ->
                if (isGroup) {
                    // 检查是否是群组消息 - 通过metadata判断
                    message.metadata["groupId"] == userId
                } else {
                    (message.from == userId || message.to == userId) && message.metadata["groupId"] == null
                }
            }.sortedBy { it.timestamp }
        }
    }

    override suspend fun getAllMessages(): Result<List<Message>> {
        return safeApiCall {
            messageDatabase.messages.value
        }
    }

    override suspend fun searchMessages(query: String): Result<List<Message>> {
        return safeApiCall {
            messageDatabase.messages.value.filter { message ->
                message.content.contains(query, ignoreCase = true)
            }
        }
    }

    override suspend fun deleteAllMessages(): Result<Unit> {
        return safeApiCall {
            // 清空所有消息
            messageDatabase.messages.value.forEach { message ->
                messageDatabase.deleteMessage(message.id)
            }
            Unit
        }
    }

    /**
     * 处理接收到的MQTT消息
     */
    suspend fun handleReceivedMessage(message: Message): Result<Unit> {
        return safeApiCall {
            // 检查是否已存在相同ID的消息
            val existingMessage = messageDatabase.messages.value
                .find { it.id == message.id }

            if (existingMessage == null) {
                // 新消息，保存到数据库
                messageDatabase.insertMessage(message)
            }
            Unit
        }
    }

    /**
     * 处理接收到的原始MQTT消息
     */
    suspend fun handleRawMessage(topic: String, payload: String): Result<Unit> {
        return safeApiCall {
            // 根据主题解析消息
            when {
                topic.startsWith("petter/msg/direct/") -> {
                    val topicParts = topic.split("/")
                    if (topicParts.size >= 5) {
                        val senderId = topicParts[3]
                        val receiverId = topicParts[4]

                        // 检查是否是发给当前用户的消息
                        val currentUserId = getCurrentUserId()
                        if (receiverId == currentUserId) {
                            val message = Message(
                                id = "msg_${UUID.randomUUID().toString().substring(0, 8)}",
                                type = Message.MessageType.TEXT,
                                from = senderId,
                                to = receiverId,
                                content = payload,
                                timestamp = System.currentTimeMillis(),
                                status = Message.MessageStatus.DELIVERED
                            )
                            messageDatabase.insertMessage(message)
                        }
                    }
                }

                topic.startsWith("petter/msg/group/") -> {
                    val groupId = topic.split("/").getOrNull(3)
                    if (groupId != null) {
                        val message = Message(
                            id = "msg_${UUID.randomUUID().toString().substring(0, 8)}",
                            type = Message.MessageType.TEXT,
                            from = "unknown", // 可以从payload中解析
                            to = "",
                            content = payload,
                            timestamp = System.currentTimeMillis(),
                            status = Message.MessageStatus.DELIVERED,
                            metadata = mapOf("groupId" to groupId)
                        )
                        messageDatabase.insertMessage(message)
                    }
                }
            }
            Unit
        }
    }

    /**
     * 获取当前用户ID - 这个方法需要从用户偏好设置中获取
     * 实际实现中需要注入UserPreferences
     */
    private fun getCurrentUserId(): String {
        // 临时实现，实际应该从UserPreferences获取
        return "current_user"
    }

    /**
     * 清理过期消息
     */
    suspend fun cleanupOldMessages(): Result<Unit> {
        return safeApiCall {
            messageDatabase.cleanupOldData()
            Unit
        }
    }
}