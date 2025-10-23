package hi.petter.domain.repository

import hi.petter.domain.model.Contact
import kotlinx.coroutines.flow.Flow

interface IContactRepository {
    suspend fun addContact(userId: String, nickname: String): Result<Contact>
    suspend fun removeContact(userId: String): Result<Unit>
    suspend fun updateContact(contact: Contact): Result<Contact>
    suspend fun getContact(userId: String): Result<Contact?>
    suspend fun getAllContacts(): Result<List<Contact>>
    fun observeContacts(): Flow<List<Contact>>
    suspend fun searchContacts(query: String): Result<List<Contact>>
}