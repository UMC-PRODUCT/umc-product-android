package com.umc.presentation.ui.act.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.umc.domain.model.act.challenger.UserChallenger
import com.umc.presentation.databinding.ItemActUserChallengerBinding

class UserChallengerAdapter(
    private val onItemClick: (Int) -> Unit
) : ListAdapter<UserChallenger, UserChallengerAdapter.UserChallengerViewHolder>(UserChallengerDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserChallengerViewHolder {
        val binding = ItemActUserChallengerBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UserChallengerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserChallengerViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class UserChallengerViewHolder(
        private val binding: ItemActUserChallengerBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: UserChallenger) {
            binding.model = item

            binding.root.setOnClickListener {
                onItemClick(item.id)
            }

            binding.executePendingBindings()
        }
    }

    class UserChallengerDiffCallback : DiffUtil.ItemCallback<UserChallenger>() {
        override fun areItemsTheSame(oldItem: UserChallenger, newItem: UserChallenger): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: UserChallenger, newItem: UserChallenger): Boolean {
            return oldItem == newItem
        }
    }
}