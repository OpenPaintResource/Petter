package hi.petter.presentation.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * 标准ViewModel基类
 * 统一错误处理、加载状态、协程管理
 */
abstract class BaseViewModel : ViewModel() {

    init {
        // 设置协程异常处理
        viewModelScope.launch {
            // 设置全局异常处理
            // TODO: 可以在这里添加全局错误上报逻辑
        }
    }

    protected fun launchSafely(
        block: suspend () -> Unit,
        onError: (String, Exception) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch {
            try {
                block()
            } catch (e: Exception) {
                onError("操作失败", e)
            }
        }
    }
}