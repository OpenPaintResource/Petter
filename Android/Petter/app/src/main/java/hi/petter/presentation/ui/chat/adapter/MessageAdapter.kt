package hi.petter.presentation.ui.chat.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import hi.petter.databinding.ItemMessageReceivedBinding
import hi.petter.databinding.ItemMessageSentBinding
import hi.petter.domain.model.ChatMessage
import hi.petter.domain.model.Message

class MessageAdapter : ListAdapter<ChatMessage, RecyclerView.ViewHolder>(DiffCallback()) {

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).isFromMe) {
            VIEW_TYPE_SENT
        } else {
            VIEW_TYPE_RECEIVED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_SENT -> {
                val binding = ItemMessageSentBinding.inflate(inflater, parent, false)
                SentMessageViewHolder(binding)
            }
            VIEW_TYPE_RECEIVED -> {
                val binding = ItemMessageReceivedBinding.inflate(inflater, parent, false)
                ReceivedMessageViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SentMessageViewHolder -> {
                holder.bind(getItem(position))
            }
            is ReceivedMessageViewHolder -> {
                holder.bind(getItem(position))
            }
        }
    }

    class SentMessageViewHolder(
        private val binding: ItemMessageSentBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(chatMessage: ChatMessage) {
            val message = chatMessage.message
            binding.apply {
                tvMessage.text = message.content
                tvTime.text = "10:30" // TODO: 实现时间格式化

                // 设置消息状态图标
                when (message.status) {
                    Message.MessageStatus.SENT -> {
                        ivStatus.setImageResource(hi.petter.R.drawable.ic_check_single)
                    }
                    Message.MessageStatus.DELIVERED -> {
                        ivStatus.setImageResource(hi.petter.R.drawable.ic_check_double)
                    }
                    Message.MessageStatus.READ -> {
                        ivStatus.setImageResource(hi.petter.R.drawable.ic_check_double)
                        ivStatus.alpha = 1.0f
                    }
                    else -> {
                        ivStatus.visibility = android.view.View.GONE
                    }
                }
            }
        }
    }

    class ReceivedMessageViewHolder(
        private val binding: ItemMessageReceivedBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(chatMessage: ChatMessage) {
            val message = chatMessage.message
            binding.apply {
                tvMessage.text = message.content
                tvTime.text = "10:30" // TODO: 实现时间格式化
                ivAvatar.setImageResource(hi.petter.R.drawable.ic_person)
                // TODO: 实现头像加载
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ChatMessage>() {
        override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem.message.id == newItem.message.id
        }

        override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem.message == newItem.message
        }
    }
}