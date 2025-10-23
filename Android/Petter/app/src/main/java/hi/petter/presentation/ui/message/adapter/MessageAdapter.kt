package hi.petter.presentation.ui.message.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import hi.petter.R
import hi.petter.domain.model.Message
import hi.petter.utils.DateUtils

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
        private val tvSenderName: TextView = itemView.findViewById(R.id.tvSenderName)
        private val tvMessageContent: TextView = itemView.findViewById(R.id.tvMessageContent)
        private val tvMessageTime: TextView = itemView.findViewById(R.id.tvMessageTime)

        // 右侧消息控件 (自己发送)
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
            }
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