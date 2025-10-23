package hi.petter.data.repository

import hi.petter.data.local.database.MessageDatabase
import hi.petter.data.remote.decentralized.SimplifiedMqttManager
import hi.petter.domain.model.Group
import hi.petter.domain.repository.IGroupRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 群组相关的Repository实现
 * 整合本地存储和MQTT群组管理
 */
@Singleton
class GroupRepositoryImpl @Inject constructor(
    private val messageDatabase: MessageDatabase,
    private val mqttManager: SimplifiedMqttManager
) : IGroupRepository, BaseRepository() {

    private var currentUserId: String = ""

    /**
     * 设置当前用户ID
     */
    fun setCurrentUserId(userId: String) {
        currentUserId = userId
    }

    override suspend fun createGroup(name: String, description: String): Result<Group> {
        return safeApiCall {
            // 生成群组ID
            val groupId = "group_${UUID.randomUUID().toString().substring(0, 8)}"

            // 创建群组对象
            val group = Group(
                id = groupId,
                name = name,
                description = description,
                creatorId = currentUserId,
                createdTime = System.currentTimeMillis(),
                memberCount = 1,
                isPublic = true,
                avatarUrl = null
            )

            // 保存到本地数据库
            messageDatabase.insertGroup(group)

            // 通过MQTT广播群组创建
            val mqttGroupId = mqttManager.createGroup(name)
            if (mqttGroupId.isNotEmpty()) {
                group
            } else {
                // MQTT创建失败，但本地已保存，仍然返回成功
                group
            }
        }
    }

    override suspend fun joinGroup(groupId: String): Result<Group> {
        return safeApiCall {
            // 检查群组是否存在
            val existingGroup = messageDatabase.getGroupById(groupId)
            if (existingGroup != null) {
                // 用户已在群组中
                // 简化处理：假设用户不在群组中
                // TODO: 实际应该有成员列表来检查

                // 添加用户到群组 - 简化处理，只增加成员计数
                val updatedGroup = existingGroup.copy(
                    memberCount = existingGroup.memberCount + 1
                )

                // 更新本地数据库
                messageDatabase.updateGroup(updatedGroup)

                // 通过MQTT发送加入群组消息
                mqttManager.joinGroup(groupId)

                updatedGroup
            } else {
                // 群组不存在，先创建本地记录
                val group = Group(
                    id = groupId,
                    name = "Group $groupId",
                    description = "Joined group",
                    creatorId = "unknown",
                    createdTime = System.currentTimeMillis(),
                    memberCount = 1,
                    isPublic = true,
                    avatarUrl = null
                )

                messageDatabase.insertGroup(group)
                mqttManager.joinGroup(groupId)

                group
            }
        }
    }

    override suspend fun leaveGroup(groupId: String): Result<Unit> {
        return safeApiCall {
            val group = messageDatabase.getGroupById(groupId)
            if (group != null) {
                // 简化处理：减少成员计数
                val updatedGroup = group.copy(
                    memberCount = maxOf(0, group.memberCount - 1)
                )

                // 如果是创建者且群组还有其他成员，转移创建者权限
                val finalGroup = if (group.creatorId == currentUserId && updatedGroup.memberCount > 0) {
                    // 简化处理：清空创建者
                    updatedGroup.copy(creatorId = "")
                } else {
                    updatedGroup
                }

                if (finalGroup.memberCount > 0) {
                    messageDatabase.updateGroup(finalGroup)
                } else {
                    // 群组没有成员了，删除群组
                    messageDatabase.deleteGroup(groupId)
                }

                // 通过MQTT发送离开群组消息
                mqttManager.leaveGroup(groupId)
            }

            Unit
        }
    }

    override suspend fun getGroup(groupId: String): Result<Group?> {
        return safeApiCall {
            messageDatabase.getGroupById(groupId)
        }
    }

    override suspend fun getUserGroups(): Result<List<Group>> {
        return safeApiCall {
            messageDatabase.getGroupsForUser(currentUserId)
        }
    }

    override suspend fun updateGroup(group: Group): Result<Group> {
        return safeApiCall {
            messageDatabase.updateGroup(group)
            group
        }
    }

    override suspend fun deleteGroup(groupId: String): Result<Unit> {
        return safeApiCall {
            val group = messageDatabase.getGroupById(groupId)
            if (group != null && group.creatorId == currentUserId) {
                // 只有创建者可以删除群组
                messageDatabase.deleteGroup(groupId)

                // 通过MQTT发送删除群组消息
                mqttManager.leaveGroup(groupId)
            }
            Unit
        }
    }

    override fun observeUserGroups(): Flow<List<Group>> {
        return messageDatabase.groups.map { groups ->
            groups.filter { group ->
                group.creatorId == currentUserId // 简化处理：只显示创建的群组
            }
        }
    }

    override suspend fun searchGroups(query: String): Result<List<Group>> {
        return safeApiCall {
            messageDatabase.groups.value.filter { group ->
                group.name.contains(query, ignoreCase = true) ||
                group.description.contains(query, ignoreCase = true)
            }
        }
    }

    override suspend fun getPublicGroups(): Result<List<Group>> {
        return safeApiCall {
            messageDatabase.groups.value.filter { group ->
                group.isPublic
            }
        }
    }

    /**
     * 检查用户是否在群组中
     */
    fun isUserInGroup(groupId: String): Boolean {
        val group = messageDatabase.getGroupById(groupId)
        return group?.creatorId == currentUserId // 简化处理：创建者被认为在群组中
    }

    /**
     * 获取群组成员信息
     */
    suspend fun getGroupMembers(groupId: String): Result<List<String>> {
        return safeApiCall("获取群组成员失败") {
            val group = messageDatabase.getGroupById(groupId)
            // 简化处理：返回创建者ID
            group?.let { listOf(it.creatorId) } ?: emptyList()
        }
    }

    /**
     * 邀请用户加入群组
     * 简化版本：只增加成员计数，不管理具体成员列表
     */
    suspend fun inviteToGroup(groupId: String, userId: String): Result<Unit> {
        return safeApiCall {
            val group = messageDatabase.getGroupById(groupId)
            if (group != null && group.creatorId == currentUserId) {
                // 只有群主可以邀请其他用户
                val updatedGroup = group.copy(
                    memberCount = group.memberCount + 1
                )
                messageDatabase.updateGroup(updatedGroup)
            }
            Unit
        }
    }
}