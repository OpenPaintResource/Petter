package hi.petter.data.local.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import hi.petter.domain.model.Contact
import hi.petter.domain.model.User

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey
    val userId: String,

    @ColumnInfo(name = "username")
    val username: String,

    @ColumnInfo(name = "email")
    val email: String,

    @ColumnInfo(name = "nickname")
    val nickname: String,

    @ColumnInfo(name = "avatar_url")
    val avatarUrl: String?,

    @ColumnInfo(name = "status")
    val status: String,

    @ColumnInfo(name = "added_date")
    val addedDate: Long,

    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean,

    @ColumnInfo(name = "unread_count")
    val unreadCount: Int,

    @ColumnInfo(name = "last_message")
    val lastMessage: String?,

    @ColumnInfo(name = "last_message_time")
    val lastMessageTime: Long
) {
    fun toDomainModel(): Contact {
        return Contact(
            user = User(
                id = userId,
                username = username,
                email = email,
                nickname = nickname,
                avatarUrl = avatarUrl,
                status = User.UserStatus.valueOf(status),
                lastSeen = 0L
            ),
            addedDate = addedDate,
            isFavorite = isFavorite,
            unreadCount = unreadCount,
            lastMessage = lastMessage,
            lastMessageTime = lastMessageTime
        )
    }
}

// 扩展函数用于转换
fun Contact.toEntity(): ContactEntity {
    return ContactEntity(
        userId = user.id,
        username = user.username,
        email = user.email,
        nickname = user.nickname,
        avatarUrl = user.avatarUrl,
        status = user.status.name,
        addedDate = addedDate,
        isFavorite = isFavorite,
        unreadCount = unreadCount,
        lastMessage = lastMessage,
        lastMessageTime = lastMessageTime
    )
}