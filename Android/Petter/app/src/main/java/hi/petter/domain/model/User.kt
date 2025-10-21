package hi.petter.domain.model

data class User(
    val id: String,
    val username: String,
    val email: String,
    val nickname: String = username,
    val avatarUrl: String? = null,
    val status: UserStatus = UserStatus.OFFLINE,
    val lastSeen: Long = 0L
) {
    enum class UserStatus {
        ONLINE, OFFLINE, AWAY, BUSY
    }

    fun isOnline(): Boolean = status == UserStatus.ONLINE
    fun getStatusDisplayName(): String = when (status) {
        UserStatus.ONLINE -> "在线"
        UserStatus.OFFLINE -> "离线"
        UserStatus.AWAY -> "离开"
        UserStatus.BUSY -> "忙碌"
    }
}