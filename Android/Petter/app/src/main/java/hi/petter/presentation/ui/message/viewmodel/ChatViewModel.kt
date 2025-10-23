package hi.petter.presentation.ui.message.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hi.petter.data.remote.decentralized.SimplifiedMqttManager
import hi.petter.data.repository.MessageRepositoryImpl
import hi.petter.domain.model.Message
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val messageRepository: MessageRepositoryImpl,
    private val mqttManager: SimplifiedMqttManager
) : ViewModel() {

    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> = _messages

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _messageSent = MutableLiveData(false)
    val messageSent: LiveData<Boolean> = _messageSent

    private var currentUserId: String = ""
    private var currentChatId: String = ""
    private var isGroupChat: Boolean = false
    private var chatName: String = ""

    /**
     * 初始化聊天
     */
    fun initializeChat(userId: String, isGroup: Boolean, name: String) {
        android.util.Log.d("ChatViewModel", "初始化聊天 - chatId: $userId, isGroup: $isGroup, name: $name")

        currentChatId = userId
        isGroupChat = isGroup
        chatName = name

        // 获取当前用户ID - 使用设备ID作为用户标识
        currentUserId = "current_user" // 简化处理，实际应该从用户管理器获取

        android.util.Log.d("ChatViewModel", "聊天初始化完成 - currentUserId: $currentUserId, currentChatId: $currentChatId")
    }

    /**
     * 获取当前用户ID
     */
    fun getCurrentUserId(): String = currentUserId

    /**
     * 连接MQTT（如果需要）
     */
    fun connectMqttIfNeeded() {
        viewModelScope.launch {
            if (!mqttManager.isConnected()) {
                mqttManager.connect()
            }

            // 监听MQTT消息
            observeMqttMessages()
        }
    }

    /**
     * 加载消息历史
     */
    fun loadMessages() {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                val result = messageRepository.getMessages(currentChatId, isGroupChat)
                if (result.isSuccess) {
                    _messages.value = result.getOrNull() ?: emptyList()
                } else {
                    _errorMessage.value = "加载消息失败: ${result.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "加载消息出错: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 发送消息
     */
    fun sendMessage(content: String) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                val message = Message(
                    id = "",
                    type = Message.MessageType.TEXT,
                    from = currentUserId,
                    to = if (!isGroupChat) currentChatId else "",
                    content = content,
                    timestamp = System.currentTimeMillis(),
                    status = Message.MessageStatus.SENT,
                    metadata = if (isGroupChat) mapOf("groupId" to currentChatId) else emptyMap()
                )

                val result = messageRepository.sendMessage(message)
                if (result.isSuccess) {
                    _messageSent.value = true
                    // 重新加载消息列表
                    loadMessages()
                } else {
                    _errorMessage.value = "发送失败: ${result.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "发送消息出错: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 监听MQTT消息
     */
    private fun observeMqttMessages() {
        viewModelScope.launch {
            mqttManager.messageFlow.collect { message ->
                // 检查消息是否与当前聊天相关
                val groupId = message.metadata["groupId"] as? String
                val isRelevantMessage = if (isGroupChat) {
                    groupId == currentChatId
                } else {
                    (message.from == currentChatId && message.to == currentUserId) ||
                    (message.from == currentUserId && message.to == currentChatId)
                }

                if (isRelevantMessage) {
                    // 处理接收到的消息
                    handleReceivedMessage(message)
                }
            }
        }

        viewModelScope.launch {
            mqttManager.errorFlow.collect { error ->
                _errorMessage.value = "MQTT错误: $error"
            }
        }
    }

    /**
     * 处理接收到的消息
     */
    private fun handleReceivedMessage(message: Message) {
        viewModelScope.launch {
            try {
                // 保存消息到本地
                messageRepository.handleReceivedMessage(message)

                // 如果是发送给当前聊天的消息，更新UI
                val groupId = message.metadata["groupId"] as? String
                val isRelevantMessage = if (isGroupChat) {
                    groupId == currentChatId
                } else {
                    message.from == currentChatId && message.to == currentUserId
                }

                if (isRelevantMessage) {
                    // 重新加载消息列表
                    loadMessages()
                }
            } catch (e: Exception) {
                _errorMessage.value = "处理消息出错: ${e.message}"
            }
        }
    }

    /**
     * 清除错误消息
     */
    fun clearError() {
        _errorMessage.value = ""
    }

    /**
     * 清除消息发送状态
     */
    fun clearMessageSent() {
        _messageSent.value = false
    }

    /**
     * 断开MQTT连接
     */
    fun disconnectMqtt() {
        // 注意：这里不完全断开MQTT，因为其他界面可能还在使用
        // 只是在Activity销毁时做必要的清理
    }
}