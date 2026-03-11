package com.umc.presentation.ui.act.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.umc.presentation.databinding.ItemActAdminChallengerGroupBinding
import com.umc.presentation.ui.act.challenge.AdminChallengerGroupUIModel

class AdminChallengerGroupAdapter(
    private val onItemClick: (Int) -> Unit
) : ListAdapter<AdminChallengerGroupUIModel, AdminChallengerGroupAdapter.GroupViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        return GroupViewHolder(
            ItemActAdminChallengerGroupBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class GroupViewHolder(private val binding: ItemActAdminChallengerGroupBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(groupItem: AdminChallengerGroupUIModel) {
            binding.group = groupItem

            // 내부 리사이클러뷰에 기존 AdminChallengerAdapter 연결
            val memberAdapter = AdminChallengerAdapter { challenger ->
                onItemClick(challenger.id)
            }

            binding.rvMemberList.apply {
                adapter = memberAdapter
                setHasFixedSize(true)
            }

            memberAdapter.submitList(groupItem.items)
            binding.executePendingBindings()
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<AdminChallengerGroupUIModel>() {
        override fun areItemsTheSame(old: AdminChallengerGroupUIModel, new: AdminChallengerGroupUIModel) =
            old.part == new.part
        override fun areContentsTheSame(old: AdminChallengerGroupUIModel, new: AdminChallengerGroupUIModel) =
            old == new
    }
}