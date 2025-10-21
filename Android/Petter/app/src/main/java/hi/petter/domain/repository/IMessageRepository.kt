package hi.petter.domain.repository

import hi.petter.domain.model.Message
import kotlinx.coroutines.flow.Flow

interface IMessageRepository {
    suspend fun sendMessage(message: Message): Boolean
    fun getChatMessages(userId: String, otherUserId: String): Flow<List<Message>>
    suspend fun markAsRead(messageId: String)
    suspend fun getUnreadCount(userId: String): Int
    suspend fun deleteMessage(messageId: String)
    fun getAllMessages(): Flow<List<Message>>
}