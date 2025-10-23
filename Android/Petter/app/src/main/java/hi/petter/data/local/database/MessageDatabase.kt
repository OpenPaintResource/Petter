package hi.petter.data.local.database

import android.content.Context
import android.util.Log
import hi.petter.domain.model.Message
import hi.petter.domain.model.Group
import hi.petter.domain.model.Contact
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 简化的消息数据库，暂时避免序列化问题
 */
@Singleton
class MessageDatabase @Inject constructor(
    private val context: Context
) {
    private val mutex = Mutex()

    // 内存缓存
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    private val _groups = MutableStateFlow<List<hi.petter.domain.model.Group>>(emptyList())
    val groups: StateFlow<List<hi.petter.domain.model.Group>> = _groups

    private val _contacts = MutableStateFlow<List<hi.petter.domain.model.Contact>>(emptyList())
    val contacts: StateFlow<List<hi.petter.domain.model.Contact>> = _contacts

    init {
        // 暂时使用内存存储，不进行文件序列化
    }

    // ==================== 消息相关操作 ====================

    suspend fun insertMessage(message: Message) {
        mutex.withLock {
            val currentList = _messages.value.toMutableList()
            currentList.add(message)
            _messages.value = currentList
        }
    }

    suspend fun updateMessage(message: Message) {
        mutex.withLock {
            val currentList = _messages.value.toMutableList()
            val index = currentList.indexOfFirst { it.id == message.id }
            if (index >= 0) {
                currentList[index] = message
                _messages.value = currentList
            }
        }
    }

    suspend fun deleteMessage(messageId: String) {
        mutex.withLock {
            val currentList = _messages.value.toMutableList()
            currentList.removeAll { it.id == messageId }
            _messages.value = currentList
        }
    }

    fun getMessagesForChat(chatId: String, isGroup: Boolean = false): List<Message> {
        return _messages.value.filter { message ->
            if (isGroup) {
                message.metadata["groupId"] == chatId
            } else {
                (message.from == chatId || message.to == chatId) && message.metadata["groupId"] == null
            }
        }.sortedBy { it.timestamp }
    }

    // ==================== 群组相关操作 ====================

    suspend fun insertGroup(group: hi.petter.domain.model.Group) {
        mutex.withLock {
            val currentList = _groups.value.toMutableList()
            currentList.add(group)
            _groups.value = currentList
        }
    }

    suspend fun updateGroup(group: hi.petter.domain.model.Group) {
        mutex.withLock {
            val currentList = _groups.value.toMutableList()
            val index = currentList.indexOfFirst { it.id == group.id }
            if (index >= 0) {
                currentList[index] = group
                _groups.value = currentList
            }
        }
    }

    suspend fun deleteGroup(groupId: String) {
        mutex.withLock {
            val currentList = _groups.value.toMutableList()
            currentList.removeAll { it.id == groupId }
            _groups.value = currentList
        }
    }

    fun getGroupById(groupId: String): hi.petter.domain.model.Group? {
        return _groups.value.find { it.id == groupId }
    }

    fun getGroupsForUser(userId: String): List<hi.petter.domain.model.Group> {
        return _groups.value.filter { group ->
            group.creatorId == userId // 简化处理：只显示创建的群组
        }
    }

    // ==================== 联系人相关操作 ====================

    suspend fun insertContact(contact: hi.petter.domain.model.Contact) {
        mutex.withLock {
            val currentList = _contacts.value.toMutableList()
            // 避免重复添加
            if (currentList.none { it.user.id == contact.user.id }) {
                currentList.add(contact)
                _contacts.value = currentList
            }
        }
    }

    suspend fun updateContact(contact: hi.petter.domain.model.Contact) {
        mutex.withLock {
            val currentList = _contacts.value.toMutableList()
            val index = currentList.indexOfFirst { it.user.id == contact.user.id }
            if (index >= 0) {
                currentList[index] = contact
                _contacts.value = currentList
            }
        }
    }

    suspend fun deleteContact(userId: String) {
        mutex.withLock {
            val currentList = _contacts.value.toMutableList()
            currentList.removeAll { it.user.id == userId }
            _contacts.value = currentList
        }
    }

    fun getContactById(userId: String): hi.petter.domain.model.Contact? {
        return _contacts.value.find { it.user.id == userId }
    }

    fun getAllContacts(): List<hi.petter.domain.model.Contact> {
        return _contacts.value.sortedBy { it.getDisplayName() }
    }

    // ==================== 清理和维护 ====================

    /**
     * 清理过期数据
     */
    suspend fun cleanupOldData() {
        mutex.withLock {
            val currentTime = System.currentTimeMillis()
            val thirtyDaysAgo = currentTime - (30 * 24 * 60 * 60 * 1000L)

            // 清理30天前的消息
            val filteredMessages = _messages.value.filter { it.timestamp > thirtyDaysAgo }
            if (filteredMessages.size != _messages.value.size) {
                _messages.value = filteredMessages
            }
        }
    }

    /**
     * 获取数据库统计信息
     */
    fun getDatabaseStats(): Map<String, Int> {
        return mapOf(
            "messages" to _messages.value.size,
            "groups" to _groups.value.size,
            "contacts" to _contacts.value.size
        )
    }
}