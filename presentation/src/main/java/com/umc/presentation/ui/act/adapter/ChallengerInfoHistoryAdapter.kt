package com.umc.presentation.ui.act.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.umc.domain.model.act.challenger.ChallengerInfoHistory
import com.umc.presentation.databinding.ItemChallengerHistoryCheckBinding

class ChallengerInfoHistoryAdapter : ListAdapter<ChallengerInfoHistory, ChallengerInfoHistoryAdapter.ViewHolder>(DiffCallback()) {

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
        fun bind(item: ChallengerInfoHistory) {
            binding.item = item
            binding.executePendingBindings()
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ChallengerInfoHistory>() {
        override fun areItemsTheSame(oldItem: ChallengerInfoHistory, newItem: ChallengerInfoHistory) = oldItem.title == newItem.title
        override fun areContentsTheSame(oldItem: ChallengerInfoHistory, newItem: ChallengerInfoHistory) = oldItem == newItem
    }
}