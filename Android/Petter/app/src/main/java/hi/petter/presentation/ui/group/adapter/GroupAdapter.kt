package hi.petter.presentation.ui.group.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import hi.petter.domain.model.Group
import hi.petter.utils.DateUtils
import javax.inject.Inject

/**
 * 群组列表Adapter - 简化版本
 */
class GroupAdapter @Inject constructor(
    private val onGroupClick: (String) -> Unit
) : ListAdapter<Group, GroupAdapter.GroupViewHolder>(GroupDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return GroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class GroupViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        fun bind(group: Group) {
            val text1 = itemView.findViewById<TextView>(android.R.id.text1)
            val text2 = itemView.findViewById<TextView>(android.R.id.text2)

            text1?.text = group.name
            text2?.text = "${group.description} • ${group.memberCount} 成员 • ${DateUtils.formatChatTime(group.createdTime)}"

            itemView.setOnClickListener {
                onGroupClick(group.id)
            }
        }
    }

    class GroupDiffCallback : DiffUtil.ItemCallback<Group>() {
        override fun areItemsTheSame(oldItem: Group, newItem: Group): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Group, newItem: Group): Boolean {
            return oldItem == newItem
        }
    }
}