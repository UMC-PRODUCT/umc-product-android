package com.umc.presentation.ui.act.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.umc.presentation.databinding.ItemActUserChallengerBinding
import com.umc.presentation.ui.act.challenge.UserChallengerUIModel

class UserChallengerAdapter(
    private val onItemClick: (Long) -> Unit
) : ListAdapter<UserChallengerUIModel, UserChallengerAdapter.UserChallengerViewHolder>(UserChallengerDiffCallback()) { // UIModel로 변경

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

        fun bind(item: UserChallengerUIModel) {
            // XML 레이아웃에 맞게 바인딩 변수 설정 (model과 uiModel 모두 전달)
            binding.model = item.challenger
            binding.uiModel = item // 구분선 처리를 위한 UI 모델 전달

            binding.root.setOnClickListener {
                onItemClick(item.challenger.id) // Long 타입 ID 전달
            }

            binding.executePendingBindings()
        }
    }

    class UserChallengerDiffCallback : DiffUtil.ItemCallback<UserChallengerUIModel>() {
        override fun areItemsTheSame(oldItem: UserChallengerUIModel, newItem: UserChallengerUIModel): Boolean {
            return oldItem.challenger.id == newItem.challenger.id
        }

        override fun areContentsTheSame(oldItem: UserChallengerUIModel, newItem: UserChallengerUIModel): Boolean {
            return oldItem == newItem
        }
    }
}