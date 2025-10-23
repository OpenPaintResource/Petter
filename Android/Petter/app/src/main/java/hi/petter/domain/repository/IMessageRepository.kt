package hi.petter.domain.repository

import hi.petter.domain.model.Message
import kotlinx.coroutines.flow.Flow

interface IMessageRepository {
    suspend fun sendMessage(message: Message): Result<Message>
    suspend fun markAsRead(messageId: String): Result<Unit>
    suspend fun deleteMessage(messageId: String): Result<Unit>
    suspend fun getMessages(userId: String, isGroup: Boolean): Result<List<Message>>
    suspend fun getUnreadCount(userId: String, isGroup: Boolean): Result<Int>
    fun observeMessages(userId: String, isGroup: Boolean): Flow<List<Message>>
    suspend fun getAllMessages(): Result<List<Message>>
    suspend fun searchMessages(query: String): Result<List<Message>>
    suspend fun deleteAllMessages(): Result<Unit>
}