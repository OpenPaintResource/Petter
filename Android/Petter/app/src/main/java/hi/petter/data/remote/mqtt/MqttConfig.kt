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

    // 非中心化通信主题设计
    object Topics {
        // 用户发现和状态
        object Discovery {
            fun userBroadcast(userId: String) = "petter/discovery/user/$userId"
            fun groupBroadcast(groupId: String) = "petter/discovery/group/$groupId"
            const val userList = "petter/discovery/users"
            const val groupList = "petter/discovery/groups"
        }

        // 群组管理
        object Group {
            fun create(groupId: String) = "petter/group/create/$groupId"
            fun join(groupId: String) = "petter/group/join/$groupId"
            fun leave(groupId: String) = "petter/group/leave/$groupId"
            fun memberList(groupId: String) = "petter/group/members/$groupId"
            fun groupInfo(groupId: String) = "petter/group/info/$groupId"
        }

        // 消息传输
        object Message {
            fun direct(senderId: String, receiverId: String) = "petter/msg/direct/$senderId/$receiverId"
            fun group(groupId: String) = "petter/msg/group/$groupId"
            fun encrypted(senderId: String, receiverId: String) = "petter/msg/encrypted/$senderId/$receiverId"
            fun broadcast(type: String) = "petter/msg/broadcast/$type"
        }

        // 状态同步
        object Status {
            fun online(userId: String) = "petter/status/online/$userId"
            fun offline(userId: String) = "petter/status/offline/$userId"
            fun typing(groupId: String, userId: String) = "petter/status/typing/$groupId/$userId"
            fun userStatus(userId: String) = "petter/status/user/$userId"
        }

        // 群组状态
        object GroupState {
            fun sync(groupId: String) = "petter/group/sync/$groupId"
            fun memberStatus(groupId: String, userId: String) = "petter/group/status/$groupId/$userId"
            fun groupSettings(groupId: String) = "petter/group/settings/$groupId"
        }

        // 身份验证
        object Auth {
            fun challenge(userId: String) = "petter/auth/challenge/$userId"
            fun response(userId: String) = "petter/auth/response/$userId"
            fun verify(userId: String) = "petter/auth/verify/$userId"
        }
    }

    // QoS级别定义
    object QoS {
        const val CRITICAL = 2  // 身份验证、群组管理消息
        const val HIGH = 2      // 高优先级消息
        const val NORMAL = 1    // 普通消息
        const val BROADCAST = 0 // 广播消息
    }
}