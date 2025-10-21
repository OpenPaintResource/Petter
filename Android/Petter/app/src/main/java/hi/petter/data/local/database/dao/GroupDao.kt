package hi.petter.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import hi.petter.data.local.database.entities.GroupEntity
import hi.petter.data.local.database.entities.GroupMemberEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupDao {

    @Query("SELECT * FROM groups")
    fun getAllGroups(): Flow<List<GroupEntity>>

    @Query("SELECT * FROM groups WHERE creator_id = :userId")
    fun getMyCreatedGroups(userId: String): Flow<List<GroupEntity>>

    @Query("""
        SELECT g.* FROM groups g
        INNER JOIN group_members gm ON g.id = gm.group_id
        WHERE gm.user_id = :userId
        ORDER BY gm.joined_time DESC
    """)
    fun getUserGroups(userId: String): Flow<List<GroupEntity>>

    @Query("SELECT * FROM groups WHERE is_public = 1 AND status = 'ACTIVE'")
    fun getPublicGroups(): List<GroupEntity>

    @Query("SELECT * FROM groups WHERE id = :groupId")
    suspend fun getGroupById(groupId: String): GroupEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: GroupEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroups(groups: List<GroupEntity>)

    @Update
    suspend fun updateGroup(group: GroupEntity)

    @Query("DELETE FROM groups WHERE id = :groupId")
    suspend fun deleteGroup(groupId: String)

    @Query("UPDATE groups SET member_count = :count WHERE id = :groupId")
    suspend fun updateMemberCount(groupId: String, count: Int)
}

@Dao
interface GroupMemberDao {

    @Query("SELECT * FROM group_members WHERE group_id = :groupId")
    fun getGroupMembers(groupId: String): Flow<List<GroupMemberEntity>>

    @Query("SELECT * FROM group_members WHERE group_id = :groupId ORDER BY joined_time ASC")
    fun getGroupMembersOrdered(groupId: String): Flow<List<GroupMemberEntity>>

    @Query("SELECT * FROM group_members WHERE user_id = :userId")
    fun getUserMemberships(userId: String): Flow<List<GroupMemberEntity>>

    @Query("SELECT * FROM group_members WHERE user_id = :userId AND group_id = :groupId")
    suspend fun getMember(userId: String, groupId: String): GroupMemberEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: GroupMemberEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMembers(members: List<GroupMemberEntity>)

    @Query("DELETE FROM group_members WHERE user_id = :userId AND group_id = :groupId")
    suspend fun deleteMember(userId: String, groupId: String)

    @Query("DELETE FROM group_members WHERE group_id = :groupId")
    suspend fun deleteAllGroupMembers(groupId: String)

    @Query("UPDATE group_members SET is_online = :isOnline, last_seen = :lastSeen WHERE user_id = :userId AND group_id = :groupId")
    suspend fun updateMemberOnlineStatus(userId: String, groupId: String, isOnline: Boolean, lastSeen: Long = System.currentTimeMillis())

    @Query("UPDATE group_members SET role = :role WHERE user_id = :userId AND group_id = :groupId")
    suspend fun updateMemberRole(userId: String, groupId: String, role: String)

    @Query("SELECT COUNT(*) FROM group_members WHERE group_id = :groupId")
    suspend fun getGroupMemberCount(groupId: String): Int

    @Query("SELECT COUNT(*) FROM group_members WHERE group_id = :groupId AND is_online = 1")
    suspend fun getOnlineMemberCount(groupId: String): Int
}