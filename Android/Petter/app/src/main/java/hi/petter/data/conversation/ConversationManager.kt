package hi.petter.data.conversation

import hi.petter.data.local.database.MessageDatabase
import hi.petter.data.repository.GroupRepositoryImpl
import hi.petter.data.repository.MessageRepositoryImpl
import hi.petter.domain.model.Group
import hi.petter.domain.model.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 会话管理器 - 统一处理群组和私聊会话
 * 参考微信的聊天列表逻辑
 */
@Singleton
class ConversationManager @Inject constructor(
    private val messageDatabase: MessageDatabase,
    private val groupRepository: GroupRepositoryImpl,
    private val messageRepository: MessageRepositoryImpl
) {

    private var currentUserId: String = ""

    fun setCurrentUserId(userId: String) {
        currentUserId = userId
    }

    /**
     * 获取所有会话列表（群组 + 私聊）
     * 按最后消息时间排序
     */
    fun getAllConversations(): Flow<List<Conversation>> {
        // 获取群组列表
        val groupFlow = groupRepository.observeUserGroups()

        // 获取所有最近的私聊会话
        val privateChatFlow = getRecentPrivateChats()

        return combine(groupFlow, privateChatFlow) { groups, privateChats ->
            val conversations = mutableListOf<Conversation>()

            // 添加群组会话
            groups.forEach { group ->
                val lastMessage = messageRepository.getLastMessage(group.id, true)
                val unreadCount = getUnreadCount(group.id, true)

                conversations.add(
                    Conversation(
                        id = group.id,
                        name = group.name,
                        lastMessage = lastMessage?.content ?: "暂无消息",
                        lastMessageTime = lastMessage?.timestamp ?: group.createdTime,
                        unreadCount = unreadCount,
                        isGroup = true,
                        avatarUrl = group.avatarUrl,
                        isMuted = false, // TODO: 实现免打扰功能
                        isActive = true
                    )
                )
            }

            // 添加私聊会话
            privateChats.forEach { chat ->
                val unreadCount = getUnreadCount(chat.userId, false)

                conversations.add(
                    Conversation(
                        id = chat.userId,
                        name = chat.userName,
                        lastMessage = chat.lastMessage,
                        lastMessageTime = chat.lastMessageTime,
                        unreadCount = unreadCount,
                        isGroup = false,
                        avatarUrl = null,
                        isMuted = false,
                        isActive = true
                    )
                )
            }

            // 按最后消息时间排序
            conversations.sortedByDescending { it.lastMessageTime }
        }
    }

    /**
     * 获取最近的私聊会话
     */
    private fun getRecentPrivateChats(): Flow<List<PrivateChat>> {
        return messageDatabase.messages.map { messages ->
            // 筛选出私聊消息（非群组消息）
            val privateMessages = messages.filter { message ->
                !message.metadata.containsKey("groupId") &&
                (message.from == currentUserId || message.to == currentUserId)
            }

            // 按对话者分组
            val chatMap = mutableMapOf<String, PrivateChat>()

            privateMessages.forEach { message ->
                val otherUserId = if (message.from == currentUserId) {
                    message.to
                } else {
                    message.from
                }

                if (otherUserId.isNotEmpty()) {
                    val existing = chatMap[otherUserId]
                    if (existing == null || message.timestamp > existing.lastMessageTime) {
                        chatMap[otherUserId] = PrivateChat(
                            userId = otherUserId,
                            userName = otherUserId, // TODO: 获取用户昵称
                            lastMessage = message.content,
                            lastMessageTime = message.timestamp
                        )
                    }
                }
            }

            chatMap.values.toList()
        }
    }

    /**
     * 获取未读消息数量
     */
    private fun getUnreadCount(chatId: String, isGroup: Boolean): Int {
        return try {
            val messages = messageDatabase.getMessagesForChat(chatId, isGroup)
            messages.count { message ->
                message.from != currentUserId && message.status != Message.MessageStatus.READ
            }
        } catch (e: Exception) {
            0
        }
    }

    /**
     * 标记会话为已读
     */
    suspend fun markConversationAsRead(chatId: String, isGroup: Boolean) {
        val messages = messageDatabase.getMessagesForChat(chatId, isGroup)
        messages.filter {
            it.from != currentUserId && it.status != Message.MessageStatus.READ
        }.forEach { message ->
            messageRepository.markAsRead(message.id)
        }
    }

    /**
     * 删除会话
     */
    suspend fun deleteConversation(chatId: String, isGroup: Boolean) {
        if (isGroup) {
            // 退出群组
            groupRepository.leaveGroup(chatId)
        } else {
            // 删除私聊记录
            val messages = messageDatabase.getMessagesForChat(chatId, false)
            messages.forEach { message ->
                messageRepository.deleteMessage(message.id)
            }
        }
    }
}

/**
 * 会话数据类
 */
data class Conversation(
    val id: String,
    val name: String,
    val lastMessage: String,
    val lastMessageTime: Long,
    val unreadCount: Int = 0,
    val isGroup: Boolean = false,
    val avatarUrl: String? = null,
    val isMuted: Boolean = false,
    val isActive: Boolean = true
)

/**
 * 私聊数据类
 */
data class PrivateChat(
    val userId: String,
    val userName: String,
    val lastMessage: String,
    val lastMessageTime: Long
)