package hi.petter.data.local.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "groups")
data class GroupEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "description")
    val description: String,

    @ColumnInfo(name = "creator_id")
    val creatorId: String,

    @ColumnInfo(name = "created_time")
    val createdTime: Long,

    @ColumnInfo(name = "member_count")
    val memberCount: Int,

    @ColumnInfo(name = "is_public")
    val isPublic: Boolean,

    @ColumnInfo(name = "is_encrypted")
    val isEncrypted: Boolean,

    @ColumnInfo(name = "avatar_url")
    val avatarUrl: String? = null,

    @ColumnInfo(name = "settings_json")
    val settingsJson: String,

    @ColumnInfo(name = "status")
    val status: String
)

@Entity(tableName = "group_members", primaryKeys = ["user_id", "group_id"])
data class GroupMemberEntity(
    @ColumnInfo(name = "user_id")
    val userId: String,

    @ColumnInfo(name = "group_id")
    val groupId: String,

    @ColumnInfo(name = "nickname")
    val nickname: String,

    @ColumnInfo(name = "role")
    val role: String,

    @ColumnInfo(name = "joined_time")
    val joinedTime: Long,

    @ColumnInfo(name = "is_online")
    val isOnline: Boolean,

    @ColumnInfo(name = "last_seen")
    val lastSeen: Long = 0L
)