package hi.petter.data.remote.mqtt

object MqttConfig {
    const val MIN_RECONNECT_DELAY = 1000
    const val MAX_RECONNECT_DELAY = 30000
    val MQTT_USERNAME: String? = null
    val MQTT_PASSWORD: String? = null
    const val CONNECTION_TIMEOUT = 30_000
    const val KEEP_ALIVE_INTERVAL = 60
    const val CLIENT_ID_PREFIX = "petter_android_"
    const val MQTT_BROKER_URL = "tcp://47.92.79.116:1883"
    const val QOS = 1
    const val RETAINED = false
    const val NETWORK_TIMEOUT = 60_000

    // MQTT Topics
    object Topics {
        object Auth {
            fun login(userId: String) = "petter/auth/login/$userId"
            fun register(userId: String) = "petter/auth/register/$userId"
        }

        object Chat {
            fun personal(senderId: String, receiverId: String) = "petter/chat/personal/$senderId/$receiverId"
            fun group(groupId: String) = "petter/chat/group/$groupId"
        }

        object Status {
            fun online(userId: String) = "petter/status/online/$userId"
            fun offline(userId: String) = "petter/status/offline/$userId"
            fun typing(senderId: String, receiverId: String) = "petter/status/typing/$senderId/$receiverId"
        }

        object Contacts {
            fun request(senderId: String, receiverId: String) = "petter/contacts/request/$senderId/$receiverId"
            fun response(senderId: String, receiverId: String) = "petter/contacts/response/$senderId/$receiverId"
            fun list(userId: String) = "petter/contacts/list/$userId"
        }

        object System {
            fun notification(userId: String) = "petter/system/notification/$userId"
            fun heartbeat(userId: String) = "petter/system/heartbeat/$userId"
        }
    }
}