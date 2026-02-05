package com.umc.presentation.ui.act.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.umc.domain.model.act.challenger.HistoryItem
import com.umc.presentation.databinding.ItemChallengerHistoryBinding
import com.umc.presentation.extension.px

class ChallengerHistoryAdapter(
    private val onDeleteClick: (HistoryItem) -> Unit
) : ListAdapter<HistoryItem, ChallengerHistoryAdapter.ViewHolder>(DiffCallback()) {

    private var isEditMode: Boolean = false

    fun setEditMode(isEdit: Boolean) {
        this.isEditMode = isEdit
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemChallengerHistoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val layoutParams = holder.itemView.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.topMargin = if (position == 0) 0 else 12.px
        holder.itemView.layoutParams = layoutParams

        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemChallengerHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: HistoryItem) {
            binding.tvDate.text = item.date
            binding.tvType.text = item.type

            val statusText = "${item.historyType.label} +${if (item.count % 1.0 == 0.0) item.count.toInt() else item.count}"
            binding.tvStatus.text = statusText

            binding.btnRemove.visibility = if (isEditMode) View.VISIBLE else View.GONE
            binding.btnRemove.setOnClickListener { onDeleteClick(item) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<HistoryItem>() {
        override fun areItemsTheSame(oldItem: HistoryItem, newItem: HistoryItem): Boolean =
            oldItem.date == newItem.date && oldItem.type == newItem.type
        override fun areContentsTheSame(oldItem: HistoryItem, newItem: HistoryItem): Boolean =
            oldItem == newItem
    }
}