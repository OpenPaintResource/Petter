package hi.petter.presentation.ui.main.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.hdodenhof.circleimageview.CircleImageView
import hi.petter.R
import hi.petter.data.conversation.Conversation
import hi.petter.utils.DateUtils
import javax.inject.Inject

/**
 * 会话列表Adapter - 微信风格
 */
class ConversationAdapter @Inject constructor(
    private val onConversationClick: (String, Boolean) -> Unit
) : ListAdapter<Conversation, ConversationAdapter.ConversationViewHolder>(ConversationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_conversation, parent, false)
        return ConversationViewHolder(view)
    }

    override fun onBindViewHolder(holder: ConversationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ConversationViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        private val ivAvatar: CircleImageView = itemView.findViewById(R.id.ivAvatar)
        private val ivGroupIcon: ImageView = itemView.findViewById(R.id.ivGroupIcon)
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        private val tvLastMessage: TextView = itemView.findViewById(R.id.tvLastMessage)
        private val layoutUnread: LinearLayout = itemView.findViewById(R.id.layoutUnread)
        private val tvUnreadCount: TextView = itemView.findViewById(R.id.tvUnreadCount)
        private val ivMute: ImageView = itemView.findViewById(R.id.ivMute)

        fun bind(conversation: Conversation) {
            // 设置名称
            tvName.text = conversation.name

            // 设置时间
            tvTime.text = DateUtils.formatMessageTime(conversation.lastMessageTime)

            // 设置最后一条消息
            tvLastMessage.text = conversation.lastMessage

            // 设置头像
            if (conversation.isGroup) {
                // 群组头像逻辑
                ivGroupIcon.visibility = View.VISIBLE
                ivAvatar.setImageResource(R.drawable.ic_group_placeholder)

                // 根据群组名称设置背景颜色
                setGroupAvatarColor(ivAvatar, conversation.name)
            } else {
                // 私聊头像逻辑
                ivGroupIcon.visibility = View.GONE
                ivAvatar.setImageResource(R.drawable.ic_person_placeholder)

                // 根据用户名设置背景颜色
                setUserAvatarColor(ivAvatar, conversation.name)
            }

            // 设置未读消息数
            if (conversation.unreadCount > 0) {
                layoutUnread.visibility = View.VISIBLE
                if (conversation.unreadCount > 99) {
                    tvUnreadCount.text = "99+"
                } else {
                    tvUnreadCount.text = conversation.unreadCount.toString()
                }
            } else {
                layoutUnread.visibility = View.GONE
            }

            // 设置免打扰状态
            ivMute.visibility = if (conversation.isMuted) View.VISIBLE else View.GONE

            // 点击事件
            itemView.setOnClickListener {
                onConversationClick(conversation.id, conversation.isGroup)
            }
        }

        /**
         * 设置用户头像背景颜色
         */
        private fun setUserAvatarColor(imageView: CircleImageView, userName: String) {
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

            val colorIndex = kotlin.math.abs(userName.hashCode()) % colors.size
            imageView.setColorFilter(colors[colorIndex])
        }

        /**
         * 设置群组头像背景颜色
         */
        private fun setGroupAvatarColor(imageView: CircleImageView, groupName: String) {
            val colors = intArrayOf(
                0xFF4CAF50.toInt(), // Green
                0xFF2196F3.toInt(), // Blue
                0xFFFF9800.toInt(), // Orange
                0xFF9C27B0.toInt(), // Purple
                0xFFF44336.toInt(), // Red
                0xFF009688.toInt(), // Teal
                0xFF795548.toInt(), // Brown
                0xFF607D8B.toInt()  // Blue Grey
            )

            val colorIndex = kotlin.math.abs(groupName.hashCode()) % colors.size
            imageView.setColorFilter(colors[colorIndex])
        }
    }

    class ConversationDiffCallback : DiffUtil.ItemCallback<Conversation>() {
        override fun areItemsTheSame(oldItem: Conversation, newItem: Conversation): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Conversation, newItem: Conversation): Boolean {
            return oldItem == newItem
        }
    }
}