package hi.petter.presentation.ui.chat.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import hi.petter.databinding.ItemChatPreviewBinding
import hi.petter.domain.model.Contact
import hi.petter.utils.DateUtils

class ChatPreviewAdapter(
    private val onItemClick: (Contact) -> Unit
) : ListAdapter<Contact, ChatPreviewAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemChatPreviewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemChatPreviewBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(contact: Contact) {
            binding.apply {
                tvName.text = contact.getDisplayName()
                tvLastMessage.text = contact.lastMessage ?: "暂无消息"
                tvTime.text = contact.lastMessageTime.let { DateUtils.formatChatTime(it) }

                // 显示未读消息数量
                if (contact.hasUnreadMessages()) {
                    tvUnreadBadge.text = contact.unreadCount.toString()
                    tvUnreadBadge.visibility = android.view.View.VISIBLE
                } else {
                    tvUnreadBadge.visibility = android.view.View.GONE
                }

                // 设置头像（这里使用占位符，实际应该加载真实头像）
                if (contact.user.avatarUrl != null) {
                    // 加载网络头像
                    // Glide.with(root.context).load(contact.user.avatarUrl).into(ivAvatar)
                } else {
                    ivAvatar.setImageResource(hi.petter.R.drawable.ic_person)
                }

                // 设置在线状态指示器
                ivAvatar.background = if (contact.user.isOnline()) {
                    root.context.getDrawable(hi.petter.R.drawable.bg_avatar_online)
                } else {
                    null
                }

                root.setOnClickListener {
                    onItemClick(contact)
                }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Contact>() {
        override fun areItemsTheSame(oldItem: Contact, newItem: Contact): Boolean {
            return oldItem.user.id == newItem.user.id
        }

        override fun areContentsTheSame(oldItem: Contact, newItem: Contact): Boolean {
            return oldItem == newItem
        }
    }
}