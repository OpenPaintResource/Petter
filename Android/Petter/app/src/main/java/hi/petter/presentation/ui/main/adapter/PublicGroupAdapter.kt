package hi.petter.presentation.ui.main.adapter

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
 * 公开群组列表Adapter - 简化版本
 */
class PublicGroupAdapter @Inject constructor(
    private val onGroupJoin: (String) -> Unit
) : ListAdapter<Group, PublicGroupAdapter.PublicGroupViewHolder>(PublicGroupDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PublicGroupViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return PublicGroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: PublicGroupViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PublicGroupViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        fun bind(group: Group) {
            val text1 = itemView.findViewById<TextView>(android.R.id.text1)
            val text2 = itemView.findViewById<TextView>(android.R.id.text2)

            text1?.text = group.name
            text2?.text = "${group.description.ifEmpty { "暂无描述" }} • ${group.memberCount} 成员 • 点击加入"

            itemView.setOnClickListener {
                onGroupJoin(group.id)
            }
        }
    }

    class PublicGroupDiffCallback : DiffUtil.ItemCallback<Group>() {
        override fun areItemsTheSame(oldItem: Group, newItem: Group): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Group, newItem: Group): Boolean {
            return oldItem == newItem
        }
    }
}