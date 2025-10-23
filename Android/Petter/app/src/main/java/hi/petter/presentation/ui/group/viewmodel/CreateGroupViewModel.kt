package hi.petter.presentation.ui.group.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hi.petter.data.remote.decentralized.SimplifiedMqttManager
import kotlinx.coroutines.launch

/**
 * 创建群组ViewModel
 * 简化版本，避免Hilt依赖问题
 */
class CreateGroupViewModel(
    private val mqttManager: SimplifiedMqttManager
) : ViewModel() {

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _successMessage = MutableLiveData<String>()
    val successMessage: LiveData<String> = _successMessage

    /**
     * 创建群组
     */
    fun createGroup(groupName: String, groupDescription: String = "") {
        viewModelScope.launch {
            if (groupName.isBlank()) {
                _errorMessage.value = "群组名称不能为空"
                return@launch
            }

            try {
                _isLoading.value = true

                // 使用MQTT管理器创建群组
                val groupId = mqttManager.createGroup(groupName)

                if (groupId.isNotEmpty()) {
                    _successMessage.value = "群组创建成功：$groupName"
                } else {
                    _errorMessage.value = "群组创建失败"
                }
            } catch (e: Exception) {
                _errorMessage.value = "创建群组时发生错误：${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 清除消息
     */
    fun clearMessages() {
        _errorMessage.value = ""
        _successMessage.value = ""
    }
}