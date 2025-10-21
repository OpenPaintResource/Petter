package hi.petter.data.remote.mqtt

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import java.util.UUID


class MqttClientManager(
    private val context: Context,
    private val userId: String
) {
    private var mqttClient: MqttClient? = null
    private val messageFlow = MutableSharedFlow<org.eclipse.paho.client.mqttv3.MqttMessage>()

    private val options: MqttConnectOptions by lazy {
        MqttConnectOptions().apply {
            userName = MqttConfig.MQTT_USERNAME
            password = MqttConfig.MQTT_PASSWORD?.toCharArray()
            connectionTimeout = MqttConfig.CONNECTION_TIMEOUT / 1000
            keepAliveInterval = MqttConfig.KEEP_ALIVE_INTERVAL
            isAutomaticReconnect = true
            maxReconnectDelay = MqttConfig.MAX_RECONNECT_DELAY
        }
    }

    suspend fun connect(): Flow<MqttMessage> {
        try {
            val clientId = "${MqttConfig.CLIENT_ID_PREFIX}$userId"
            mqttClient = MqttClient(
                MqttConfig.MQTT_BROKER_URL,
                clientId,
                MemoryPersistence()
            ).apply {
                setCallback(object : MqttCallback {
                    override fun connectionLost(cause: Throwable?) {
                        // Handle connection loss
                    }

                    override fun messageArrived(topic: String?, message: MqttMessage?) {
                        message?.let {
                            messageFlow.tryEmit(it)
                        }
                    }

                    override fun deliveryComplete(token: IMqttDeliveryToken?) {
                        // Handle message delivery confirmation
                    }
                })
            }

            mqttClient?.connect(options)
            subscribeToUserTopics()

        } catch (e: Exception) {
            // Handle connection error
        }

        return messageFlow
    }

    private fun subscribeToUserTopics() {
        mqttClient?.let { client ->
            // Subscribe to user-specific topics
            val topics = arrayOf(
                MqttConfig.Topics.Chat.personal("*", userId),
                MqttConfig.Topics.Status.online("*"),
                MqttConfig.Topics.Status.typing("*", userId),
                MqttConfig.Topics.Contacts.request("*", userId),
                MqttConfig.Topics.Contacts.response("*", userId),
                MqttConfig.Topics.System.notification(userId)
            )

            topics.forEach { topic ->
                client.subscribe(topic, MqttConfig.QOS)
            }
        }
    }

    suspend fun sendMessage(topic: String, payload: String): Boolean {
        return try {
            mqttClient?.publish(
                topic,
                MqttMessage(payload.toByteArray()).apply {
                    qos = MqttConfig.QOS
                    isRetained = MqttConfig.RETAINED
                }
            )
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun sendChatMessage(receiverId: String, message: String): Boolean {
        val topic = MqttConfig.Topics.Chat.personal(userId, receiverId)
        return sendMessage(topic, message)
    }

    fun publishOnlineStatus() {
        val topic = MqttConfig.Topics.Status.online(userId)
        mqttClient?.publish(topic, MqttMessage("online".toByteArray()))
    }

    fun publishOfflineStatus() {
        val topic = MqttConfig.Topics.Status.offline(userId)
        mqttClient?.publish(topic, MqttMessage("offline".toByteArray()))
    }

    fun publishTypingStatus(receiverId: String, isTyping: Boolean) {
        val topic = MqttConfig.Topics.Status.typing(userId, receiverId)
        val status = if (isTyping) "typing" else "stop_typing"
        mqttClient?.publish(topic, MqttMessage(status.toByteArray()))
    }

    fun disconnect() {
        mqttClient?.let { client ->
            if (client.isConnected) {
                publishOfflineStatus()
                client.disconnect()
            }
        }
        mqttClient = null
    }

    fun isConnected(): Boolean = mqttClient?.isConnected == true
}