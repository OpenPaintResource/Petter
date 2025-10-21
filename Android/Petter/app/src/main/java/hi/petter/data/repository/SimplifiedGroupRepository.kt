package hi.petter.data.repository

import hi.petter.data.remote.decentralized.SimplifiedMqttManager
import hi.petter.domain.model.Group
import hi.petter.domain.model.GroupMember
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map

/**
 * 殖化的群组Repository
 * 只实现基本的群组管理功能，确保编译通过
 */
class SimplifiedGroupRepository(
    private val mqttManager: SimplifiedMqttManager
) {

    // 群组数据流
    private val _myGroups = MutableStateFlow<List<Group>>(emptyList())
    val myGroups: StateFlow<List<Group>> = _myGroups

    // 群组成员流
    private val _groupMembers = MutableStateFlow<Map<String, List<GroupMember>>>(emptyMap())
    val groupMembers: StateFlow<Map<String, List<GroupMember>>> = _groupMembers

    private val _events = MutableSharedFlow<GroupEvent>()
    val events: Flow<GroupEvent> = _events

    /**
     * 群组事件
     */
    sealed class GroupEvent {
        data class GroupCreated(val group: Group) : GroupEvent()
        data class GroupJoined(val groupId: String, val member: GroupMember) : GroupEvent()
        data class GroupLeft(val groupId: String, val userId: String) : GroupEvent()
        data class MemberJoined(val groupId: String, val member: GroupMember) : GroupEvent()
        data class MemberLeft(val groupId: String, val userId: String) : GroupEvent()
        data class MemberOnline(val groupId: String, val userId: String) : GroupEvent()
        data class MemberOffline(val groupId: String, val userId: String) : GroupEvent()
        data class GroupUpdated(val groupId: String, val group: Group) : GroupEvent()
        data class Error(val message: String) : GroupEvent()
    }

    /**
     * 创建群组
     */
    suspend fun createGroup(name: String, description: String = ""): String {
        return try {
            val group = Group(
                id = "group_${System.currentTimeMillis()}",
                name = name,
                description = description,
                creatorId = "current_user", // 简化版
                createdTime = System.currentTimeMillis(),
                memberCount = 1,
                isPublic = true,
                isEncrypted = false
            )

            val groupId = mqttManager.createGroup(group.name)

            // 模拟添加到本地数据
            _myGroups.value = _myGroups.value + group

            _events.tryEmit(GroupEvent.GroupCreated(group))
            groupId
        } catch (e: Exception) {
            _events.tryEmit(GroupEvent.Error("创建群组失败: ${e.message}"))
            ""
        }
    }

    /**
     * 加入群组
     */
    suspend fun joinGroup(groupId: String, nickname: String = ""): Boolean {
        return try {
            val member = GroupMember(
                userId = "current_user", // 简化版
                groupId = groupId,
                nickname = nickname,
                role = hi.petter.domain.model.MemberRole.MEMBER,
                joinedTime = System.currentTimeMillis(),
                isOnline = true
            )

            val success = mqttManager.joinGroup(groupId)

            if (success) {
                _events.tryEmit(GroupEvent.MemberJoined(groupId, member))
            } else {
                _events.tryEmit(GroupEvent.Error("加入群组失败"))
            }

            success
        } catch (e: Exception) {
            _events.tryEmit(GroupEvent.Error("加入群组失败: ${e.message}"))
            false
        }
    }

    /**
     * 离开群组
     */
    suspend fun leaveGroup(groupId: String): Boolean {
        return try {
            val success = mqttManager.leaveGroup(groupId)

            if (success) {
                _events.tryEmit(GroupEvent.GroupLeft(groupId, "current_user"))
                // 从本地数据移除
                _groupMembers.value = _groupMembers.value.filterNot { it.key == groupId }
            } else {
                _events.tryEmit(GroupEvent.Error("离开群组失败"))
            }

            success
        } catch (e: Exception) {
            _events.tryEmit(GroupEvent.Error("离开群组失败: ${e.message}"))
            false
        }
    }

    /**
     * 发送群组消息
     */
    suspend fun sendGroupMessage(groupId: String, content: String): Boolean {
        return try {
            val message = """
                {
                    "type": "text",
                    "content": "$content",
                    "fromUserId": "current_user",
                    "groupId": "$groupId",
                    "timestamp": "${System.currentTimeMillis()}"
                }
            """

            mqttManager.sendGroupMessage(groupId, message)
        } catch (e: Exception) {
            _events.tryEmit(GroupEvent.Error("发送群组消息失败: ${e.message}"))
            false
        }
    }

    /**
     * 发送私人消息
     */
    suspend fun sendDirectMessage(receiverId: String, content: String): Boolean {
        return try {
            val message = """
                {
                    "type": "text",
                    "content": "$content",
                    "fromUserId": "current_user",
                    "toUserId": "$receiverId",
                    "timestamp": "${System.currentTimeMillis()}"
                }
            """

            mqttManager.sendDirectMessage(receiverId, message)
        } catch (e: Exception) {
            _events.tryEmit(GroupEvent.Error("发送私人消息失败: ${e.message}"))
            false
        }
    }

    /**
     * 获取我的群组列表
     */
    fun getMyGroups(): Flow<List<Group>> = _myGroups

    /**
     * 获取群组成员
     */
    fun getGroupMembers(groupId: String): Flow<List<GroupMember>> {
        return _groupMembers.map { members -> members.getOrDefault(groupId, emptyList()) }
    }

    
    /**
     * 发现公开群组
     */
    suspend fun discoverPublicGroups(): List<Group> {
        return try {
            // 模拟发现一些公开群组
            listOf(
                Group(
                    id = "public_group_1",
                    name = "公开群组1",
                    description = "这是一个公开的群组",
                    creatorId = "user1",
                    createdTime = System.currentTimeMillis() - 3600000,
                    memberCount = 5,
                    isPublic = true,
                    isEncrypted = false
                ),
                Group(
                    id = "public_group_2",
                    name = "公开群组2",
                    description = "另一个公开的群组",
                    creatorId = "user2",
                    createdTime = System.currentTimeMillis() - 7200000,
                    memberCount = 10,
                    isPublic = true,
                    isEncrypted = false
                )
            )
        } catch (e: Exception) {
            _events.tryEmit(GroupEvent.Error("发现群组失败: ${e.message}"))
            emptyList()
        }
    }
}