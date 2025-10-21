package hi.petter.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import hi.petter.data.local.database.entities.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Query("SELECT * FROM messages WHERE (from_user = :userId1 AND to_user = :userId2) OR (from_user = :userId2 AND to_user = :userId1) ORDER BY timestamp ASC")
    fun getChatMessages(userId1: String, userId2: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE to_user = :userId AND status != 'READ' ORDER BY timestamp DESC")
    fun getUnreadMessages(userId: String): List<MessageEntity>

    @Query("SELECT COUNT(*) FROM messages WHERE to_user = :userId AND status != 'READ'")
    fun getUnreadCount(userId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)

    @Update
    suspend fun updateMessageStatus(messageId: String, status: String)

    @Query("DELETE FROM messages WHERE id = :messageId")
    suspend fun deleteMessage(messageId: String)

    @Query("DELETE FROM messages")
    suspend fun deleteAllMessages()

    @Query("SELECT * FROM messages ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentMessages(limit: Int): Flow<List<MessageEntity>>
}