package hi.petter.data.remote.decentralized

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import com.google.gson.Gson
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import hi.petter.data.remote.mqtt.MqttConfig
import hi.petter.domain.model.Message
import hi.petter.domain.model.User
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * 简化的MQTT管理器
 * 移除复杂的状态同步和分布式功能，专注于基本的消息传输
 */
class SimplifiedMqttManager(
    private val context: Context,
    private val gson: Gson = Gson()
) {
    private var userId: String = ""

    /**
     * 设置用户ID
     */
    fun setUserId(userId: String) {
        this.userId = userId
    }
    companion object {
        private const val TAG = "SimplifiedMqttManager"
    }

    private var mqttClient: MqttClient? = null

    // 连接状态流
    private val _connectionState = MutableStateFlow(false)
    val connectionState: Flow<Boolean> = _connectionState

    // 消息流 - 改为强类型消息
    private val _messageFlow = MutableSharedFlow<Message>()
    val messageFlow: Flow<Message> = _messageFlow

    // 状态通知流
    private val _statusFlow = MutableSharedFlow<String>()
    val statusFlow: Flow<String> = _statusFlow

    // 错误流
    private val _errorFlow = MutableSharedFlow<String>()
    val errorFlow: Flow<String> = _errorFlow

    private val json = Json { ignoreUnknownKeys = true }

    private val mqttOptions: MqttConnectOptions by lazy {
        MqttConnectOptions().apply {
            userName = MqttConfig.MQTT_USERNAME
            // 只有当密码不为null时才设置密码
            MqttConfig.MQTT_PASSWORD?.let { password = it.toCharArray() }
            connectionTimeout = MqttConfig.CONNECTION_TIMEOUT / 1000
            keepAliveInterval = MqttConfig.KEEP_ALIVE_INTERVAL
            isAutomaticReconnect = true
            maxReconnectDelay = MqttConfig.MAX_RECONNECT_DELAY
        }
    }

    /**
     * 连接到MQTT代理
     */
    suspend fun connect(): Boolean {
        return try {
            Log.d(TAG, "开始连接MQTT代理，用户ID: $userId")

            val clientId = "${MqttConfig.CLIENT_ID_PREFIX}$userId"
            mqttClient = MqttClient(MqttConfig.MQTT_BROKER_URL, clientId, MemoryPersistence())

            setupCallback()

            Log.d(TAG, "正在建立MQTT连接...")
            mqttClient?.connect(mqttOptions)

            Log.d(TAG, "MQTT连接成功，订阅主题...")
            subscribeToBasicTopics()
            publishOnlineStatus()

            _connectionState.value = true
            Log.d(TAG, "MQTT连接和订阅完成")
            true
        } catch (e: Exception) {
            Log.e(TAG, "MQTT连接失败", e)
            _errorFlow.tryEmit("连接失败: ${e.message}")
            _connectionState.value = false
            false
        }
    }

    private fun setupCallback() {
        mqttClient?.setCallback(object : MqttCallback {
            override fun connectionLost(cause: Throwable?) {
                Log.w(TAG, "MQTT连接丢失", cause)
                _connectionState.value = false
                _errorFlow.tryEmit("连接丢失: ${cause?.message}")
            }

            override fun messageArrived(topic: String, message: MqttMessage) {
                try {
                    Log.d(TAG, "收到消息 - 主题: $topic")
                    handleMessage(topic, String(message.payload))
                } catch (e: Exception) {
                    Log.e(TAG, "处理消息失败 - 主题: $topic", e)
                    _errorFlow.tryEmit("消息处理失败: ${e.message}")
                }
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                Log.d(TAG, "消息发送完成")
            }
        })
    }

    /**
     * 处理接收到的消息
     */
    private fun handleMessage(topic: String, payload: String) {
        try {
            when {
                // 私人消息
                topic.startsWith("petter/msg/direct/") -> {
                    val topicParts = topic.split("/")
                    if (topicParts.size >= 5) {
                        val senderId = topicParts[3]
                        val receiverId = topicParts[4]

                        if (receiverId == userId) {
                            // 这是发给当前用户的消息
                            try {
                                // 暂时跳过序列化，直接创建简单消息
                                val message = Message(
                                    id = "msg_${System.currentTimeMillis()}_${senderId}",
                                    type = Message.MessageType.TEXT,
                                    from = senderId,
                                    to = userId,
                                    content = payload,
                                    timestamp = System.currentTimeMillis(),
                                    status = Message.MessageStatus.DELIVERED
                                )
                                _messageFlow.tryEmit(message)
                                Log.d(TAG, "收到私人消息，来自: $senderId")
                            } catch (e: Exception) {
                                // 如果解析失败，创建简单消息
                                val message = Message(
                                    id = "msg_${System.currentTimeMillis()}_${senderId}",
                                    type = Message.MessageType.TEXT,
                                    from = senderId,
                                    to = userId,
                                    content = payload,
                                    timestamp = System.currentTimeMillis(),
                                    status = Message.MessageStatus.DELIVERED
                                )
                                _messageFlow.tryEmit(message)
                            }
                        }
                    }
                }

                // 群组消息
                topic.startsWith("petter/msg/group/") -> {
                    val groupId = extractGroupIdFromTopic(topic)
                    if (groupId.isNotEmpty() && isUserInGroup(groupId)) {
                        try {
                            // 暂时跳过序列化，直接创建简单群组消息
                            val message = Message(
                                id = "msg_${System.currentTimeMillis()}_group_$groupId",
                                type = Message.MessageType.TEXT,
                                from = "unknown",
                                to = "",
                                content = payload,
                                timestamp = System.currentTimeMillis(),
                                status = Message.MessageStatus.DELIVERED,
                                metadata = mapOf("groupId" to groupId)
                            )
                            _messageFlow.tryEmit(message)
                            Log.d(TAG, "收到群组消息，群组: $groupId")
                        } catch (e: Exception) {
                            // 如果解析失败，创建简单群组消息
                            val message = Message(
                                id = "msg_${System.currentTimeMillis()}_group_$groupId",
                                type = Message.MessageType.TEXT,
                                from = "unknown",
                                to = "",
                                content = payload,
                                timestamp = System.currentTimeMillis(),
                                status = Message.MessageStatus.DELIVERED,
                                metadata = mapOf("groupId" to groupId)
                            )
                            _messageFlow.tryEmit(message)
                        }
                    }
                }

                // 状态消息
                topic.startsWith("petter/status/") -> {
                    val statusInfo = "STATUS:$topic:$payload"
                    _statusFlow.tryEmit(statusInfo)
                    Log.d(TAG, "收到状态更新: $statusInfo")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "处理消息时发生错误", e)
            _errorFlow.tryEmit("消息处理错误: ${e.message}")
        }
    }

    /**
     * 简化的主题订阅
     */
    private fun subscribeToBasicTopics() {
        mqttClient?.let { client ->
            try {
                val topics = arrayOf(
                    // 私人消息 - 修复主题格式，订阅发给当前用户的
                    "petter/msg/direct/+/$userId",
                    // 群组消息 - 订阅所有群组消息，在接收时过滤
                    "petter/msg/group/+",
                    // 状态消息
                    "petter/status/online/+",
                    "petter/status/offline/+"
                )
                val qoses = IntArray(topics.size) { MqttConfig.QoS.NORMAL }

                Log.d(TAG, "订阅主题: ${topics.contentToString()}")
                client.subscribe(topics, qoses)
                Log.d(TAG, "主题订阅完成")
            } catch (e: Exception) {
                Log.e(TAG, "订阅主题失败", e)
                _errorFlow.tryEmit("订阅失败: ${e.message}")
            }
        }
    }

    /**
     * 发送私人消息
     */
    suspend fun sendDirectMessage(receiverId: String, content: String): Boolean {
        return try {
            if (!isConnected()) {
                Log.w(TAG, "MQTT未连接，无法发送消息")
                _errorFlow.tryEmit("发送失败: MQTT未连接")
                return false
            }

            val message = Message(
                id = "msg_${System.currentTimeMillis()}_${userId}_$receiverId",
                type = Message.MessageType.TEXT,
                from = userId,
                to = receiverId,
                content = content,
                timestamp = System.currentTimeMillis(),
                status = Message.MessageStatus.SENT
            )

            val topic = "petter/msg/direct/$userId/$receiverId"
            val payload = json.encodeToString(message)
            val mqttMessage = MqttMessage(payload.toByteArray()).apply {
                qos = MqttConfig.QoS.NORMAL
                isRetained = false
            }

            Log.d(TAG, "发送私人消息到: $receiverId")
            mqttClient?.publish(topic, mqttMessage)
            true
        } catch (e: Exception) {
            Log.e(TAG, "发送私人消息失败", e)
            _errorFlow.tryEmit("发送失败: ${e.message}")
            false
        }
    }

    /**
     * 发送群组消息
     */
    suspend fun sendGroupMessage(groupId: String, content: String): Boolean {
        return try {
            if (!isConnected()) {
                Log.w(TAG, "MQTT未连接，无法发送群组消息")
                _errorFlow.tryEmit("发送群组消息失败: MQTT未连接")
                return false
            }

            val message = Message(
                id = "msg_${System.currentTimeMillis()}_group_${groupId}_$userId",
                type = Message.MessageType.TEXT,
                from = userId,
                to = "",
                content = content,
                timestamp = System.currentTimeMillis(),
                status = Message.MessageStatus.SENT,
                metadata = mapOf("groupId" to groupId)
            )

            val topic = "petter/msg/group/$groupId"
            val payload = json.encodeToString(message)
            val mqttMessage = MqttMessage(payload.toByteArray()).apply {
                qos = MqttConfig.QoS.NORMAL
                isRetained = false
            }

            Log.d(TAG, "发送群组消息到群组: $groupId")
            mqttClient?.publish(topic, mqttMessage)
            true
        } catch (e: Exception) {
            Log.e(TAG, "发送群组消息失败", e)
            _errorFlow.tryEmit("发送群组消息失败: ${e.message}")
            false
        }
    }

    /**
     * 广播用户在线状态
     */
    private fun publishOnlineStatus() {
        try {
            val topic = "petter/status/online/$userId"
            val payload = "online"
            mqttClient?.publish(topic, payload.toByteArray(), MqttConfig.QoS.CRITICAL, true)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // 群组成员检查接口，由外部设置
    var groupMembershipChecker: ((String) -> Boolean)? = null

    /**
     * 检查用户是否在群组中
     */
    private fun isUserInGroup(groupId: String): Boolean {
        return groupMembershipChecker?.invoke(groupId) ?: false
    }

    /**
     * 从主题中提取群组ID
     */
    private fun extractGroupIdFromTopic(topic: String): String {
        val parts = topic.split("/")
        return if (parts.size >= 4) parts[3] else ""
    }

    /**
     * 断开连接
     */
    fun disconnect() {
        mqttClient?.let { client ->
            if (client.isConnected) {
                val topic = "petter/status/offline/$userId"
                val payload = "offline"
                client.publish(topic, payload.toByteArray(), MqttConfig.QoS.CRITICAL, true)
                client.disconnect()
            }
        }
        mqttClient = null
    }

    /**
     * 检查连接状态
     */
    fun isConnected(): Boolean = mqttClient?.isConnected == true

    /**
     * 创建群组
     */
    suspend fun createGroup(groupName: String): String {
        val groupId = "group_${System.currentTimeMillis()}"
        val payload = """
            {
                "action": "create_group",
                "groupId": "$groupId",
                "groupName": "$groupName",
                "creatorId": "$userId",
                "timestamp": "${System.currentTimeMillis()}"
            }
        """

        return try {
            val topic = "petter/group/create"
            val mqttMessage = MqttMessage(payload.toByteArray()).apply {
                qos = MqttConfig.QoS.CRITICAL
                isRetained = true
            }
            mqttClient?.publish(topic, mqttMessage)
            groupId
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    /**
     * 加入群组
     */
    suspend fun joinGroup(groupId: String): Boolean {
        val payload = """
            {
                "action": "join_group",
                "groupId": "$groupId",
                "userId": "$userId",
                "timestamp": "${System.currentTimeMillis()}"
            }
        """

        return try {
            val topic = "petter/group/join/$groupId"
            val mqttMessage = MqttMessage(payload.toByteArray()).apply {
                qos = MqttConfig.QoS.CRITICAL
                isRetained = false
            }
            mqttClient?.publish(topic, mqttMessage)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 离开群组
     */
    suspend fun leaveGroup(groupId: String): Boolean {
        val payload = """
            {
                "action": "leave_group",
                "groupId": "$groupId",
                "userId": "$userId",
                "timestamp": "${System.currentTimeMillis()}"
            }
        """

        return try {
            val topic = "petter/group/leave/$groupId"
            val mqttMessage = MqttMessage(payload.toByteArray()).apply {
                qos = MqttConfig.QoS.CRITICAL
                isRetained = true
            }
            mqttClient?.publish(topic, mqttMessage)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}