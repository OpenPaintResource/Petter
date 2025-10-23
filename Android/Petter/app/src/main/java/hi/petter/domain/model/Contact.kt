package hi.petter.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Contact(
    val user: User,
    val addedDate: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val unreadCount: Int = 0,
    val lastMessage: String? = null,
    val lastMessageTime: Long = 0L
) {
    fun hasUnreadMessages(): Boolean = unreadCount > 0

    fun getDisplayName(): String = user.nickname.ifEmpty { user.username }
}