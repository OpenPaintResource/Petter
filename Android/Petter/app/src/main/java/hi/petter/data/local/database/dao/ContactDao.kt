package hi.petter.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import hi.petter.data.local.database.entities.ContactEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {

    @Query("SELECT * FROM contacts ORDER BY added_date DESC")
    fun getAllContacts(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE user_id = :userId LIMIT 1")
    fun getContactById(userId: String): ContactEntity?

    @Query("SELECT COUNT(*) FROM contacts WHERE user_id = :userId")
    fun contactExists(userId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: ContactEntity)

    @Update
    suspend fun updateContact(contact: ContactEntity)

    @Query("DELETE FROM contacts WHERE user_id = :userId")
    suspend fun deleteContact(userId: String)

    @Query("UPDATE contacts SET unread_count = :count WHERE user_id = :userId")
    suspend fun updateUnreadCount(userId: String, count: Int)

    @Query("DELETE FROM contacts")
    suspend fun deleteAllContacts()
}