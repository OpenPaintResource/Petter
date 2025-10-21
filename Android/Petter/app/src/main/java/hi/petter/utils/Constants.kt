package hi.petter.utils

object Constants {
    // Intent extras
    const val EXTRA_USER_ID = "user_id"
    const val EXTRA_USER_NAME = "user_name"
    const val EXTRA_USER_EMAIL = "user_email"
    const val EXTRA_CHAT_ROOM_ID = "chat_room_id"

    // SharedPreferences
    const val PREFS_NAME = "petter_prefs"
    const val KEY_USER_ID = "user_id"
    const val KEY_USERNAME = "username"
    const val KEY_EMAIL = "email"
    const val KEY_ACCESS_TOKEN = "access_token"
    const val KEY_REFRESH_TOKEN = "refresh_token"

    // MQTT
    const val MQTT_KEEP_ALIVE = 60
    const val MQTT_QOS = 1
    const val MQTT_TIMEOUT = 30000

    // Database
    const val DATABASE_NAME = "petter_database"
    const val DATABASE_VERSION = 1

    // API
    const val BASE_URL = "https://api.petter.chat/"
    const val CONNECT_TIMEOUT = 30L
    const val READ_TIMEOUT = 30L
    const val WRITE_TIMEOUT = 30L

    // UI
    const val ANIMATION_DURATION = 300L
    const val DEBOUNCE_DELAY = 500L

    // File upload
    const val MAX_FILE_SIZE = 10 * 1024 * 1024 // 10MB
    const val SUPPORTED_IMAGE_TYPES = "image/jpeg,image/png,image/gif"
    const val SUPPORTED_FILE_TYPES = "image/*,application/pdf,text/*"
}