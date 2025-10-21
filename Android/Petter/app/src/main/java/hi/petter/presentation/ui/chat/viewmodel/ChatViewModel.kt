package hi.petter.presentation.ui.chat.viewmodel

import androidx.lifecycle.*
import hi.petter.domain.model.ChatMessage
import hi.petter.domain.model.Message
import hi.petter.domain.usecase.GetMessagesUseCase
import hi.petter.domain.usecase.SendMessageUseCase
import hi.petter.domain.usecase.MarkMessageAsReadUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel(
    private val getMessagesUseCase: GetMessagesUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val markMessageAsReadUseCase: MarkMessageAsReadUseCase
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _isTyping = MutableLiveData<Boolean>()
    val isTyping: LiveData<Boolean> = _isTyping

    private var currentUserId: String = ""
    private var otherUserId: String = ""

    fun initialize(currentUserId: String, otherUserId: String) {
        this.currentUserId = currentUserId
        this.otherUserId = otherUserId
        loadMessages()
    }

    fun loadMessages() {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // 简化实现，暂时使用空数据
                _messages.value = emptyList()
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "加载消息失败"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun sendMessage(content: String) {
        if (content.isBlank()) return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val message = Message(
                    id = java.util.UUID.randomUUID().toString(),
                    from = currentUserId,
                    to = otherUserId,
                    content = content.trim(),
                    type = Message.MessageType.TEXT,
                    status = Message.MessageStatus.SENT
                )

                sendMessageUseCase(message)

            } catch (e: Exception) {
                _errorMessage.postValue(e.message ?: "发送消息失败")
            }
        }
    }

    fun sendTypingIndicator(isTyping: Boolean) {
        _isTyping.value = isTyping
        // 发送正在输入状态到对方
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 通过MQTT发送正在输入状态
                // mqttClientManager.publishTypingStatus(otherUserId, isTyping)
            } catch (e: Exception) {
                // 忽略输入状态发送失败
            }
        }
    }

    fun refreshMessages() {
        loadMessages()
    }
}

class ChatViewModelFactory(
    private val getMessagesUseCase: GetMessagesUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val markMessageAsReadUseCase: MarkMessageAsReadUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(
                getMessagesUseCase,
                sendMessageUseCase,
                markMessageAsReadUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}