package com.umc.presentation.ui.act.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.umc.domain.model.act.challenger.AdminChallenger
import com.umc.presentation.databinding.ItemActAdminChallengerBinding

class AdminChallengerAdapter(
    private val onItemClick: (Int) -> Unit
) : ListAdapter<AdminChallenger, AdminChallengerAdapter.ViewHolder>(AdminChallengerDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemActAdminChallengerBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemActAdminChallengerBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: AdminChallenger) {
            binding.model = item
            binding.root.setOnClickListener { onItemClick(item.id) }
            binding.executePendingBindings()
        }
    }

    class AdminChallengerDiffCallback : DiffUtil.ItemCallback<AdminChallenger>() {
        override fun areItemsTheSame(oldItem: AdminChallenger, newItem: AdminChallenger): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: AdminChallenger, newItem: AdminChallenger): Boolean = oldItem == newItem
    }
}