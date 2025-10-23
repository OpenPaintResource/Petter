package hi.petter.data.repository

import hi.petter.data.local.database.MessageDatabase
import hi.petter.data.remote.decentralized.SimplifiedMqttManager
import hi.petter.domain.model.Contact
import hi.petter.domain.model.User
import hi.petter.domain.repository.IContactRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 联系人相关的Repository实现
 */
@Singleton
class ContactRepositoryImpl @Inject constructor(
    private val messageDatabase: MessageDatabase,
    private val mqttManager: SimplifiedMqttManager
) : IContactRepository, BaseRepository() {

    override suspend fun addContact(userId: String, nickname: String): Result<Contact> {
        return safeApiCall {
            val user = User(
                id = userId,
                username = nickname,
                email = "$nickname@petter.local",
                nickname = nickname,
                avatarUrl = null,
                status = User.UserStatus.OFFLINE,
                lastSeen = 0L
            )

            val contact = Contact(
                user = user,
                addedDate = System.currentTimeMillis()
            )

            messageDatabase.insertContact(contact)
            contact
        }
    }

    override suspend fun removeContact(userId: String): Result<Unit> {
        return safeApiCall {
            messageDatabase.deleteContact(userId)
            Unit
        }
    }

    override suspend fun updateContact(contact: Contact): Result<Contact> {
        return safeApiCall {
            messageDatabase.updateContact(contact)
            contact
        }
    }

    override suspend fun getContact(userId: String): Result<Contact?> {
        return safeApiCall {
            messageDatabase.getContactById(userId)
        }
    }

    override suspend fun getAllContacts(): Result<List<Contact>> {
        return safeApiCall {
            messageDatabase.getAllContacts()
        }
    }

    override fun observeContacts(): Flow<List<Contact>> {
        return messageDatabase.contacts
    }

    override suspend fun searchContacts(query: String): Result<List<Contact>> {
        return safeApiCall {
            messageDatabase.getAllContacts().filter { contact ->
                contact.getDisplayName().contains(query, ignoreCase = true) ||
                contact.user.id.contains(query, ignoreCase = true)
            }
        }
    }

    /**
     * 根据用户状态更新联系人信息
     */
    suspend fun updateUserOnlineStatus(userId: String, isOnline: Boolean): Result<Unit> {
        return safeApiCall {
            val contact = messageDatabase.getContactById(userId)
            if (contact != null) {
                val updatedUser = contact.user.copy(
                    status = if (isOnline) User.UserStatus.ONLINE else User.UserStatus.OFFLINE,
                    lastSeen = if (isOnline) System.currentTimeMillis() else contact.user.lastSeen
                )
                val updatedContact = contact.copy(user = updatedUser)
                messageDatabase.updateContact(updatedContact)
            }
            Unit
        }
    }

    /**
     * 从用户信息创建联系人
     */
    suspend fun createContactFromUser(user: User): Result<Contact> {
        return safeApiCall {
            val contact = Contact(
                user = user,
                addedDate = System.currentTimeMillis()
            )

            messageDatabase.insertContact(contact)
            contact
        }
    }

    /**
     * 获取在线联系人数量
     */
    suspend fun getOnlineContactsCount(): Result<Int> {
        return safeApiCall {
            messageDatabase.getAllContacts().count { it.user.status == User.UserStatus.ONLINE }
        }
    }
}