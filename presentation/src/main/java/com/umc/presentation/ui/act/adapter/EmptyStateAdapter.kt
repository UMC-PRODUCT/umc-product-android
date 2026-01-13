package com.umc.presentation.ui.act.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.umc.presentation.databinding.LayoutActCheckEmptyBinding
import com.umc.presentation.ui.act.check.EmptyStateUIModel

class EmptyStateAdapter : ListAdapter<EmptyStateUIModel, EmptyStateAdapter.ViewHolder>(EmptyStateDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = LayoutActCheckEmptyBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: LayoutActCheckEmptyBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: EmptyStateUIModel) {
            binding.message = item.message
            binding.ivEmptyIcon.setImageResource(item.iconRes)
            binding.executePendingBindings()
        }
    }

    class EmptyStateDiffCallback : DiffUtil.ItemCallback<EmptyStateUIModel>() {
        override fun areItemsTheSame(old: EmptyStateUIModel, new: EmptyStateUIModel) = old.message == new.message
        override fun areContentsTheSame(old: EmptyStateUIModel, new: EmptyStateUIModel) = old == new
    }
}