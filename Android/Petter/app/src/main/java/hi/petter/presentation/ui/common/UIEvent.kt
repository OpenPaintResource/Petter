package hi.petter.presentation.common

/**
 * UI事件类
 * 统一UI操作和交互事件
 */
sealed class UIEvent {
    data class ShowToast(val message: String) : UIEvent()
    data class NavigateTo(val destination: Class<*>) : UIEvent()
    data class ShowErrorDialog(val title: String, val message: String) : UIEvent()
    object RefreshData : UIEvent()
    data class ShowLoading(val isLoading: Boolean) : UIEvent()
    data class RequestPermission(val permission: String) : UIEvent()
}