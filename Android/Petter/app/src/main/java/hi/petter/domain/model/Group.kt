package hi.petter.domain.model

import java.util.Date

data class Group(
    val id: String,
    val name: String,
    val description: String = "",
    val creatorId: String,
    val createdTime: Long = System.currentTimeMillis(),
    val memberCount: Int = 0,
    val isPublic: Boolean = true,
    val isEncrypted: Boolean = false,
    val avatarUrl: String? = null,
    val settings: GroupSettings = GroupSettings(),
    val status: GroupStatus = GroupStatus.ACTIVE
)

data class GroupMember(
    val userId: String,
    val groupId: String,
    val nickname: String = "",
    val role: MemberRole = MemberRole.MEMBER,
    val joinedTime: Long = System.currentTimeMillis(),
    val isOnline: Boolean = false,
    val lastSeen: Long = 0L,
    val permissions: Set<MemberPermission> = setOf(MemberPermission.READ_MESSAGES)
)

data class GroupSettings(
    val allowInvites: Boolean = true,
    val requireApproval: Boolean = false,
    val maxMembers: Int = 100,
    val messageRetention: Int = 30, // days
    val allowFileSharing: Boolean = true,
    val allowVoiceMessages: Boolean = true,
    val isReadOnly: Boolean = false
)

enum class GroupStatus {
    ACTIVE, INACTIVE, ARCHIVED, DELETED
}

enum class MemberRole {
    OWNER, ADMIN, MODERATOR, MEMBER
}

enum class MemberPermission {
    READ_MESSAGES,
    SEND_MESSAGES,
    DELETE_MESSAGES,
    INVITE_MEMBERS,
    REMOVE_MEMBERS,
    MANAGE_SETTINGS,
    MUTE_MEMBERS,
    BAN_MEMBERS
}