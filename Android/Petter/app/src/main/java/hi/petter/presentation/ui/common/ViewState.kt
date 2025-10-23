package hi.petter.presentation.common

/**
 * 通用的视图状态类
 * 用于统一管理UI状态，替代布尔值
 */
sealed class ViewState<out T> {
    data class Success<out T>(val data: T) : ViewState<T>()
    object Loading : ViewState<Nothing>()
    data class Error(val message: String, val exception: Throwable? = null) : ViewState<Nothing>()
    object Empty : ViewState<Nothing>()
}