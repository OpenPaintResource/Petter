package hi.petter.domain.model

import java.util.Date

data class UserProfile(
    val userId: String,
    val username: String,
    val displayName: String,
    val avatarUrl: String? = null,
    val status: UserStatus = UserStatus.OFFLINE,
    val lastSeen: Long = 0L,
    val publicKey: String? = null, // 用于端到端加密
    val deviceInfo: DeviceInfo = DeviceInfo(),
    val preferences: UserPreferences = UserPreferences(),
    val isVerified: Boolean = false,
    val verificationLevel: VerificationLevel = VerificationLevel.NONE
)

data class DeviceInfo(
    val deviceId: String = "",
    val platform: String = "Android",
    val appVersion: String = "",
    val capabilities: Set<String> = setOf("encryption", "groups", "files")
)

data class UserPreferences(
    val allowDirectMessages: Boolean = true,
    val allowGroupInvites: Boolean = true,
    val showOnlineStatus: Boolean = true,
    val enableEncryption: Boolean = false,
    val language: String = "zh-CN",
    val theme: String = "light"
)

enum class UserStatus {
    ONLINE, AWAY, BUSY, INVISIBLE, OFFLINE
}

enum class VerificationLevel {
    NONE, BASIC, VERIFIED, TRUSTED
}

// 身份验证数据结构
data class AuthChallenge(
    val challengeId: String,
    val fromUserId: String,
    val toUserId: String,
    val challenge: String, // 加密挑战
    val timestamp: Long,
    val expiresAt: Long
)

data class AuthResponse(
    val challengeId: String,
    val response: String, // 挑战响应
    val signature: String, // 用户签名
    val publicKey: String,
    val timestamp: Long
)

data class VerificationResult(
    val userId: String,
    val isVerified: Boolean,
    val verificationLevel: VerificationLevel,
    val verifiedAt: Long,
    val expiresAt: Long
)