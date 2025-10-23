package hi.petter.presentation.ui.message.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.hdodenhof.circleimageview.CircleImageView
import hi.petter.R
import hi.petter.domain.model.Message
import hi.petter.utils.DateUtils
import kotlin.math.abs

/**
 * 消息列表Adapter - 支持聊天气泡样式
 */
class MessageAdapter : ListAdapter<Message, MessageAdapter.MessageViewHolder>(MessageDiffCallback()) {

    // 当前用户ID，用于判断消息发送者
    private var currentUserId: String = "current_user"

    fun setCurrentUserId(userId: String) {
        currentUserId = userId
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_message_bubble, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MessageViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        private val leftMessageContainer: LinearLayout = itemView.findViewById(R.id.leftMessageContainer)
        private val rightMessageContainer: LinearLayout = itemView.findViewById(R.id.rightMessageContainer)

        // 左侧消息控件 (他人发送)
        private val ivUserAvatar: CircleImageView = itemView.findViewById(R.id.ivUserAvatar)
        private val tvSenderName: TextView = itemView.findViewById(R.id.tvSenderName)
        private val tvMessageContent: TextView = itemView.findViewById(R.id.tvMessageContent)
        private val tvMessageTime: TextView = itemView.findViewById(R.id.tvMessageTime)

        // 右侧消息控件 (自己发送)
        private val ivMyAvatar: CircleImageView = itemView.findViewById(R.id.ivMyAvatar)
        private val tvMyName: TextView = itemView.findViewById(R.id.tvMyName)
        private val tvMyMessageContent: TextView = itemView.findViewById(R.id.tvMyMessageContent)
        private val tvMyMessageTime: TextView = itemView.findViewById(R.id.tvMyMessageTime)

        fun bind(message: Message) {
            val isMyMessage = message.from == currentUserId
            val formattedTime = DateUtils.formatMessageTime(message.timestamp)

            if (isMyMessage) {
                // 显示自己发送的消息 (右侧)
                leftMessageContainer.visibility = View.GONE
                rightMessageContainer.visibility = View.VISIBLE

                tvMyMessageContent.text = message.content
                tvMyMessageTime.text = formattedTime

                // 设置我的头像 (使用默认头像)
                ivMyAvatar.setImageResource(R.drawable.ic_person_placeholder)

                // 在群组聊天中显示发送者名称
                if (message.metadata.containsKey("groupId")) {
                    tvMyName.visibility = View.VISIBLE
                    tvMyName.text = "我"
                } else {
                    tvMyName.visibility = View.GONE
                }

            } else {
                // 显示他人发送的消息 (左侧)
                leftMessageContainer.visibility = View.VISIBLE
                rightMessageContainer.visibility = View.GONE

                tvSenderName.text = message.from
                tvMessageContent.text = message.content
                tvMessageTime.text = formattedTime

                // 设置用户头像 (使用默认头像)
                ivUserAvatar.setImageResource(R.drawable.ic_person_placeholder)

                // 可以根据用户名设置不同的背景颜色以区分用户
                setUserAvatarBackgroundColor(ivUserAvatar, message.from)
            }
        }

        /**
         * 根据用户名设置头像背景颜色
         */
        private fun setUserAvatarBackgroundColor(imageView: CircleImageView, userName: String) {
            // 根据用户名生成颜色
            val colors = intArrayOf(
                0xFF2196F3.toInt(), // Blue
                0xFF4CAF50.toInt(), // Green
                0xFFFF9800.toInt(), // Orange
                0xFF9C27B0.toInt(), // Purple
                0xFFF44336.toInt(), // Red
                0xFF009688.toInt(), // Teal
                0xFF795548.toInt(), // Brown
                0xFF607D8B.toInt()  // Blue Grey
            )

            val colorIndex = abs(userName.hashCode()) % colors.size
            imageView.setColorFilter(colors[colorIndex])
        }
    }

    class MessageDiffCallback : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem == newItem
        }
    }
}