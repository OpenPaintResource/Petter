package hi.petter.data.local.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import hi.petter.domain.model.User

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,

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

    @ColumnInfo(name = "last_seen")
    val lastSeen: Long
) {
    fun toDomainModel(): User {
        return User(
            id = id,
            username = username,
            email = email,
            nickname = nickname,
            avatarUrl = avatarUrl,
            status = User.UserStatus.valueOf(status),
            lastSeen = lastSeen
        )
    }
}

// 扩展函数用于转换
fun User.toEntity(): UserEntity {
    return UserEntity(
        id = id,
        username = username,
        email = email,
        nickname = nickname,
        avatarUrl = avatarUrl,
        status = status.name,
        lastSeen = lastSeen
    )
}