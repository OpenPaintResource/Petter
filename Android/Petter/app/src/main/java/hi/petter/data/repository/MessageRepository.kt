package hi.petter.data.repository

import hi.petter.data.local.database.AppDatabase
import hi.petter.data.local.database.dao.MessageDao
import hi.petter.data.local.database.entities.MessageEntity
import hi.petter.data.remote.mqtt.MqttClientManager
import hi.petter.domain.model.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MessageRepository(
    private val database: AppDatabase,
    private val mqttClientManager: MqttClientManager
) : hi.petter.domain.repository.IMessageRepository {

    private val messageDao: MessageDao by lazy { database.messageDao() }

    override suspend fun sendMessage(message: Message): Boolean {
        return try {
            // Save to local database first
            val messageEntity = MessageEntity(
                id = message.id,
                type = message.type.name,
                from = message.from,
                to = message.to,
                content = message.content,
                timestamp = message.timestamp,
                status = message.status.name,
                metadata = "{}"
            )
            messageDao.insertMessage(messageEntity)

            // Send via MQTT
            mqttClientManager.sendChatMessage(message.to, message.content)
            true
        } catch (e: Exception) {
            // Update message status to failed
            messageDao.updateMessageStatus(message.id, "FAILED")
            false
        }
    }

    override fun getChatMessages(userId: String, otherUserId: String): Flow<List<Message>> {
        return messageDao.getChatMessages(userId, otherUserId).map { entities ->
            entities.map { entity ->
                Message(
                    id = entity.id,
                    type = Message.MessageType.valueOf(entity.type),
                    from = entity.from,
                    to = entity.to,
                    content = entity.content,
                    timestamp = entity.timestamp,
                    status = Message.MessageStatus.valueOf(entity.status),
                    metadata = emptyMap()
                )
            }
        }
    }

    override suspend fun markAsRead(messageId: String) {
        messageDao.updateMessageStatus(messageId, "READ")
    }

    override suspend fun getUnreadCount(userId: String): Int {
        return messageDao.getUnreadCount(userId)
    }

    override suspend fun deleteMessage(messageId: String) {
        messageDao.deleteMessage(messageId)
    }

    override fun getAllMessages(): Flow<List<Message>> {
        return messageDao.getRecentMessages(100).map { entities ->
            entities.map { entity ->
                Message(
                    id = entity.id,
                    type = Message.MessageType.valueOf(entity.type),
                    from = entity.from,
                    to = entity.to,
                    content = entity.content,
                    timestamp = entity.timestamp,
                    status = Message.MessageStatus.valueOf(entity.status),
                    metadata = emptyMap()
                )
            }
        }
    }
}