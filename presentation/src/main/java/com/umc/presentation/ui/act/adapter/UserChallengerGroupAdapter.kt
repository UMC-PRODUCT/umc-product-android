package com.umc.presentation.ui.act.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.umc.presentation.databinding.ItemActChallengerGroupBinding
import com.umc.presentation.ui.act.challenge.ChallengerGroupUIModel

class UserChallengerGroupAdapter(
    private val onItemClick: (Long) -> Unit
) : ListAdapter<ChallengerGroupUIModel, UserChallengerGroupAdapter.GroupViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        return GroupViewHolder(ItemActChallengerGroupBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class GroupViewHolder(private val binding: ItemActChallengerGroupBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(groupItem: ChallengerGroupUIModel) {
            binding.group = groupItem

            // 중첩 리사이클러뷰 설정
            val memberAdapter = UserChallengerAdapter { id -> onItemClick(id) }
            binding.rvMemberList.adapter = memberAdapter
            memberAdapter.submitList(groupItem.items)

            binding.executePendingBindings()
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ChallengerGroupUIModel>() {
        override fun areItemsTheSame(old: ChallengerGroupUIModel, new: ChallengerGroupUIModel) = old.part == new.part
        override fun areContentsTheSame(old: ChallengerGroupUIModel, new: ChallengerGroupUIModel) = old == new
    }
}