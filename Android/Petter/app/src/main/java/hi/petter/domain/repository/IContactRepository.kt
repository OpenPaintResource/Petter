package hi.petter.domain.repository

import hi.petter.domain.model.Contact
import hi.petter.domain.model.User

interface IContactRepository {
    suspend fun getContacts(): List<Contact>
    suspend fun addContact(userId: String)
    suspend fun removeContact(userId: String)
    suspend fun searchUsers(query: String): List<User>
    suspend fun updateContactStatus(userId: String, isOnline: Boolean)
}