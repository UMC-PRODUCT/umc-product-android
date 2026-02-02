package com.umc.presentation.ui.act.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.umc.domain.model.act.challenger.SimpleHistoryItem
import com.umc.presentation.databinding.ItemChallengerHistoryCheckBinding

class ChallengerInfoHistoryAdapter : ListAdapter<SimpleHistoryItem, ChallengerInfoHistoryAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemChallengerHistoryCheckBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ItemChallengerHistoryCheckBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SimpleHistoryItem) {
            binding.item = item
            binding.executePendingBindings()
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<SimpleHistoryItem>() {
        override fun areItemsTheSame(oldItem: SimpleHistoryItem, newItem: SimpleHistoryItem) = oldItem.title == newItem.title
        override fun areContentsTheSame(oldItem: SimpleHistoryItem, newItem: SimpleHistoryItem) = oldItem == newItem
    }
}