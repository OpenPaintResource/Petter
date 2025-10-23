package hi.petter.presentation.ui.main.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hi.petter.data.remote.decentralized.SimplifiedMqttManager
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 主界面ViewModel
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val mqttManager: SimplifiedMqttManager
) : ViewModel() {

    private val _connectionState = MutableLiveData(false)
    val connectionState: LiveData<Boolean> = _connectionState

    private val _errorMessages = MutableLiveData<String>()
    val errorMessages: LiveData<String> = _errorMessages

    init {
        observeMqttConnection()
    }

    private fun observeMqttConnection() {
        viewModelScope.launch {
            mqttManager.connectionState.collect { isConnected ->
                _connectionState.value = isConnected
            }
        }
    }

    /**
     * 断开MQTT连接
     */
    fun disconnect() {
        viewModelScope.launch {
            try {
                mqttManager.disconnect()
            } catch (e: Exception) {
                showError("断开连接失败: ${e.message}")
            }
        }
    }

    /**
     * 显示错误消息
     */
    fun showError(message: String) {
        _errorMessages.value = message
    }

    /**
     * 清除错误消息
     */
    fun clearError() {
        _errorMessages.value = ""
    }
}