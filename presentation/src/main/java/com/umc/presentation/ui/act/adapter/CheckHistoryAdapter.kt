package com.umc.presentation.ui.act.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.umc.presentation.databinding.ItemActCheckHistoryBinding
import com.umc.presentation.ui.act.check.CheckHistoryUIModel

class CheckHistoryAdapter : ListAdapter<CheckHistoryUIModel, CheckHistoryAdapter.ViewHolder>(HistoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemActCheckHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemActCheckHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(uiModel: CheckHistoryUIModel) {
            binding.uiModel = uiModel
            binding.executePendingBindings()
        }
    }

    class HistoryDiffCallback : DiffUtil.ItemCallback<CheckHistoryUIModel>() {
        override fun areItemsTheSame(oldItem: CheckHistoryUIModel, newItem: CheckHistoryUIModel): Boolean {
            return oldItem.history.id == newItem.history.id
        }

        override fun areContentsTheSame(oldItem: CheckHistoryUIModel, newItem: CheckHistoryUIModel): Boolean {
            return oldItem == newItem
        }
    }
}