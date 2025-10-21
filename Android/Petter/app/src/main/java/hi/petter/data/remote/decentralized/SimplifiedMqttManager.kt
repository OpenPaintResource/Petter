package hi.petter.data.remote.decentralized

import android.content.Context
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.Flow
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import hi.petter.data.remote.mqtt.MqttConfig

/**
 * 简化的MQTT管理器
 * 移除复杂的状态同步和分布式功能，专注于基本的消息传输
 */
class SimplifiedMqttManager(
    private val context: Context,
    private val userId: String
) {
    private var mqttClient: MqttClient? = null

    // 简化的消息流
    private val _messageFlow = MutableSharedFlow<String>()
    val messageFlow: Flow<String> = _messageFlow

    private val mqttOptions: MqttConnectOptions by lazy {
        MqttConnectOptions().apply {
            userName = MqttConfig.MQTT_USERNAME
            password = MqttConfig.MQTT_PASSWORD?.toCharArray()
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
            val clientId = "${MqttConfig.CLIENT_ID_PREFIX}$userId"
            mqttClient = MqttClient(MqttConfig.MQTT_BROKER_URL, clientId, MemoryPersistence())

            setupCallback()
            mqttClient?.connect(mqttOptions)

            // 简化订阅，只订阅基本主题
            subscribeToBasicTopics()
            publishOnlineStatus()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun setupCallback() {
        mqttClient?.setCallback(object : MqttCallback {
            override fun connectionLost(cause: Throwable?) {
                // 连接丢失处理
            }

            override fun messageArrived(topic: String, message: MqttMessage) {
                try {
                    handleMessage(topic, String(message.payload))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                // 消息发送完成
            }
        })
    }

    /**
     * 处理接收到的消息
     */
    private fun handleMessage(topic: String, payload: String) {
        when {
            // 私人消息
            topic.startsWith("petter/msg/direct/") && topic.endsWith("/$userId") -> {
                _messageFlow.tryEmit(payload)
            }

            // 群组消息 (需要用户自己判断是否加入该群组)
            topic.startsWith("petter/msg/group/") -> {
                val groupId = extractGroupIdFromTopic(topic)
                if (isUserInGroup(groupId)) {
                    _messageFlow.tryEmit(payload)
                }
            }

            // 状态消息
            topic.startsWith("petter/status/") -> {
                // 处理在线/离线状态
                _messageFlow.tryEmit("STATUS:$topic:$payload")
            }
        }
    }

    /**
     * 简化的主题订阅
     */
    private fun subscribeToBasicTopics() {
        mqttClient?.let { client ->
            val topics = arrayOf(
                // 私人消息 - 只订阅发给当前用户的
                "petter/msg/direct/+/+$userId",
                // 群组消息 - 需要动态订阅
                "petter/msg/group/+/+",
                // 状态消息
                "petter/status/online/+",
                "petter/status/offline/+"
            )
            val qoses = IntArray(topics.size) { MqttConfig.QoS.NORMAL }
            client.subscribe(topics, qoses)
        }
    }

    /**
     * 发送私人消息
     */
    suspend fun sendDirectMessage(receiverId: String, content: String): Boolean {
        return try {
            val topic = "petter/msg/direct/$userId/$receiverId"
            val mqttMessage = MqttMessage(content.toByteArray()).apply {
                qos = MqttConfig.QoS.NORMAL
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
     * 发送群组消息
     */
    suspend fun sendGroupMessage(groupId: String, content: String): Boolean {
        return try {
            val topic = "petter/msg/group/$groupId"
            val mqttMessage = MqttMessage(content.toByteArray()).apply {
                qos = MqttConfig.QoS.NORMAL
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

    /**
     * 简化：假设用户在所有群组中（实际应该从数据库查询）
     */
    private fun isUserInGroup(groupId: String): Boolean {
        // TODO: 实际应该从本地数据库查询用户是否在该群组
        return true
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