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
 * 基于文件序列化的本地数据库
 * 支持消息、群组、联系人的持久化存储
 */
@Singleton
class MessageDatabase @Inject constructor(
    private val context: Context
) {
    private val mutex = Mutex()
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    // 数据文件路径
    private val dataDir = File(context.filesDir, "petter_data")
    private val messagesFile = File(dataDir, "messages.json")
    private val groupsFile = File(dataDir, "groups.json")
    private val contactsFile = File(dataDir, "contacts.json")

    // 内存缓存
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    private val _groups = MutableStateFlow<List<Group>>(emptyList())
    val groups: StateFlow<List<Group>> = _groups

    private val _contacts = MutableStateFlow<List<Contact>>(emptyList())
    val contacts: StateFlow<List<Contact>> = _contacts

    init {
        // 确保数据目录存在
        if (!dataDir.exists()) {
            dataDir.mkdirs()
        }

        // 加载持久化数据
        loadDataFromDisk()
    }

    // ==================== 消息相关操作 ====================

    suspend fun insertMessage(message: Message) {
        mutex.withLock {
            val currentList = _messages.value.toMutableList()
            currentList.add(message)
            _messages.value = currentList
            saveMessagesToDisk()
        }
    }

    suspend fun updateMessage(message: Message) {
        mutex.withLock {
            val currentList = _messages.value.toMutableList()
            val index = currentList.indexOfFirst { it.id == message.id }
            if (index >= 0) {
                currentList[index] = message
                _messages.value = currentList
                saveMessagesToDisk()
            }
        }
    }

    suspend fun deleteMessage(messageId: String) {
        mutex.withLock {
            val currentList = _messages.value.toMutableList()
            currentList.removeAll { it.id == messageId }
            _messages.value = currentList
            saveMessagesToDisk()
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

    suspend fun insertGroup(group: Group) {
        mutex.withLock {
            val currentList = _groups.value.toMutableList()
            // 避免重复添加
            if (currentList.none { it.id == group.id }) {
                currentList.add(group)
                _groups.value = currentList
                saveGroupsToDisk()
                Log.d("MessageDatabase", "Inserted group: ${group.name} (${group.id})")
            }
        }
    }

    suspend fun updateGroup(group: Group) {
        mutex.withLock {
            val currentList = _groups.value.toMutableList()
            val index = currentList.indexOfFirst { it.id == group.id }
            if (index >= 0) {
                currentList[index] = group
                _groups.value = currentList
                saveGroupsToDisk()
                Log.d("MessageDatabase", "Updated group: ${group.name} (${group.id})")
            }
        }
    }

    suspend fun deleteGroup(groupId: String) {
        mutex.withLock {
            val currentList = _groups.value.toMutableList()
            val removed = currentList.removeAll { it.id == groupId }
            _groups.value = currentList
            if (removed) {
                saveGroupsToDisk()
                Log.d("MessageDatabase", "Deleted group: $groupId")
            }
        }
    }

    fun getGroupById(groupId: String): Group? {
        return _groups.value.find { it.id == groupId }
    }

    fun getGroupsForUser(userId: String): List<Group> {
        return _groups.value.filter { group ->
            group.creatorId == userId // 简化处理：只显示创建的群组
        }
    }

    // ==================== 联系人相关操作 ====================

    suspend fun insertContact(contact: Contact) {
        mutex.withLock {
            val currentList = _contacts.value.toMutableList()
            // 避免重复添加
            if (currentList.none { it.user.id == contact.user.id }) {
                currentList.add(contact)
                _contacts.value = currentList
                saveContactsToDisk()
                Log.d("MessageDatabase", "Inserted contact: ${contact.getDisplayName()}")
            }
        }
    }

    suspend fun updateContact(contact: Contact) {
        mutex.withLock {
            val currentList = _contacts.value.toMutableList()
            val index = currentList.indexOfFirst { it.user.id == contact.user.id }
            if (index >= 0) {
                currentList[index] = contact
                _contacts.value = currentList
                saveContactsToDisk()
                Log.d("MessageDatabase", "Updated contact: ${contact.getDisplayName()}")
            }
        }
    }

    suspend fun deleteContact(userId: String) {
        mutex.withLock {
            val currentList = _contacts.value.toMutableList()
            val removed = currentList.removeAll { it.user.id == userId }
            _contacts.value = currentList
            if (removed) {
                saveContactsToDisk()
                Log.d("MessageDatabase", "Deleted contact: $userId")
            }
        }
    }

    fun getContactById(userId: String): Contact? {
        return _contacts.value.find { it.user.id == userId }
    }

    fun getAllContacts(): List<Contact> {
        return _contacts.value.sortedBy { it.getDisplayName() }
    }

    // ==================== 数据持久化操作 ====================

    /**
     * 从磁盘加载所有数据
     */
    private fun loadDataFromDisk() {
        try {
            // 加载消息
            if (messagesFile.exists()) {
                val messagesJson = messagesFile.readText()
                val messagesList = json.decodeFromString<List<Message>>(messagesJson)
                _messages.value = messagesList
                Log.d("MessageDatabase", "Loaded ${messagesList.size} messages")
            }

            // 加载群组
            if (groupsFile.exists()) {
                val groupsJson = groupsFile.readText()
                val groupsList = json.decodeFromString<List<Group>>(groupsJson)
                _groups.value = groupsList
                Log.d("MessageDatabase", "Loaded ${groupsList.size} groups")
            }

            // 加载联系人
            if (contactsFile.exists()) {
                val contactsJson = contactsFile.readText()
                val contactsList = json.decodeFromString<List<Contact>>(contactsJson)
                _contacts.value = contactsList
                Log.d("MessageDatabase", "Loaded ${contactsList.size} contacts")
            }
        } catch (e: Exception) {
            Log.e("MessageDatabase", "Error loading data from disk", e)
        }
    }

    /**
     * 保存消息数据到磁盘
     */
    private suspend fun saveMessagesToDisk() {
        try {
            val messagesJson = json.encodeToString(_messages.value)
            messagesFile.writeText(messagesJson)
        } catch (e: Exception) {
            Log.e("MessageDatabase", "Error saving messages to disk", e)
        }
    }

    /**
     * 保存群组数据到磁盘
     */
    private suspend fun saveGroupsToDisk() {
        try {
            val groupsJson = json.encodeToString(_groups.value)
            groupsFile.writeText(groupsJson)
        } catch (e: Exception) {
            Log.e("MessageDatabase", "Error saving groups to disk", e)
        }
    }

    /**
     * 保存联系人数据到磁盘
     */
    private suspend fun saveContactsToDisk() {
        try {
            val contactsJson = json.encodeToString(_contacts.value)
            contactsFile.writeText(contactsJson)
        } catch (e: Exception) {
            Log.e("MessageDatabase", "Error saving contacts to disk", e)
        }
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