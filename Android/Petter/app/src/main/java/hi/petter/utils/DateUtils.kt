package hi.petter.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    private val messageTimeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("MM/dd", Locale.getDefault())
    private val fullDateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())

    fun formatMessageTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val messageDate = Date(timestamp)
        val nowDate = Date(now)

        return when {
            isToday(timestamp) -> {
                messageTimeFormat.format(messageDate)
            }
            isYesterday(timestamp) -> {
                "昨天 ${messageTimeFormat.format(messageDate)}"
            }
            isThisWeek(timestamp) -> {
                getDayOfWeek(timestamp)
            }
            isThisYear(timestamp) -> {
                dateFormat.format(messageDate)
            }
            else -> {
                fullDateFormat.format(messageDate)
            }
        }
    }

    fun formatChatTime(timestamp: Long): String {
        val now = System.currentTimeMillis()

        return when {
            isToday(timestamp) -> {
                messageTimeFormat.format(Date(timestamp))
            }
            isYesterday(timestamp) -> {
                "昨天"
            }
            isThisWeek(timestamp) -> {
                getDayOfWeek(timestamp)
            }
            isThisYear(timestamp) -> {
                dateFormat.format(Date(timestamp))
            }
            else -> {
                fullDateFormat.format(Date(timestamp))
            }
        }
    }

    fun formatLastSeen(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < 60 * 1000 -> "刚刚"
            diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}分钟前"
            diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)}小时前"
            diff < 7 * 24 * 60 * 60 * 1000 -> "${diff / (24 * 60 * 60 * 1000)}天前"
            isToday(timestamp) -> "今天"
            isYesterday(timestamp) -> "昨天"
            isThisYear(timestamp) -> dateFormat.format(Date(timestamp))
            else -> fullDateFormat.format(Date(timestamp))
        }
    }

    private fun isToday(timestamp: Long): Boolean {
        val today = Calendar.getInstance()
        val messageDate = Calendar.getInstance()
        messageDate.timeInMillis = timestamp

        return today.get(Calendar.YEAR) == messageDate.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == messageDate.get(Calendar.DAY_OF_YEAR)
    }

    private fun isYesterday(timestamp: Long): Boolean {
        val yesterday = Calendar.getInstance()
        yesterday.add(Calendar.DAY_OF_YEAR, -1)

        val messageDate = Calendar.getInstance()
        messageDate.timeInMillis = timestamp

        return yesterday.get(Calendar.YEAR) == messageDate.get(Calendar.YEAR) &&
                yesterday.get(Calendar.DAY_OF_YEAR) == messageDate.get(Calendar.DAY_OF_YEAR)
    }

    private fun isThisWeek(timestamp: Long): Boolean {
        val now = Calendar.getInstance()
        val messageDate = Calendar.getInstance()
        messageDate.timeInMillis = timestamp

        return now.get(Calendar.WEEK_OF_YEAR) == messageDate.get(Calendar.WEEK_OF_YEAR) &&
                now.get(Calendar.YEAR) == messageDate.get(Calendar.YEAR)
    }

    private fun isThisYear(timestamp: Long): Boolean {
        val now = Calendar.getInstance()
        val messageDate = Calendar.getInstance()
        messageDate.timeInMillis = timestamp

        return now.get(Calendar.YEAR) == messageDate.get(Calendar.YEAR)
    }

    private fun getDayOfWeek(timestamp: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp

        return when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SUNDAY -> "周日"
            Calendar.MONDAY -> "周一"
            Calendar.TUESDAY -> "周二"
            Calendar.WEDNESDAY -> "周三"
            Calendar.THURSDAY -> "周四"
            Calendar.FRIDAY -> "周五"
            Calendar.SATURDAY -> "周六"
            else -> ""
        }
    }
}