package hi.petter.domain.repository

import hi.petter.domain.model.Group
import kotlinx.coroutines.flow.Flow

/**
 * 群组数据访问接口
 */
interface IGroupRepository {
    suspend fun createGroup(name: String, description: String): Result<Group>
    suspend fun joinGroup(groupId: String): Result<Group>
    suspend fun leaveGroup(groupId: String): Result<Unit>
    suspend fun getGroup(groupId: String): Result<Group?>
    suspend fun getUserGroups(): Result<List<Group>>
    suspend fun updateGroup(group: Group): Result<Group>
    suspend fun deleteGroup(groupId: String): Result<Unit>
    suspend fun searchGroups(query: String): Result<List<Group>>
    suspend fun getPublicGroups(): Result<List<Group>>

    fun observeUserGroups(): Flow<List<Group>>
}