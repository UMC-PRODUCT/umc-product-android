package com.umc.presentation.ui.notice.detail.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.umc.domain.model.notice.NoticeVoteParticipant
import com.umc.presentation.databinding.ItemNoticeVoteParticipantBinding

class NoticeVoteParticipantAdapter :
    ListAdapter<NoticeVoteParticipant, NoticeVoteParticipantAdapter.NoticeVoteParticipantViewHolder>(
        NoticeVoteParticipantDiffCallback()
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoticeVoteParticipantViewHolder {
        val binding = ItemNoticeVoteParticipantBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NoticeVoteParticipantViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoticeVoteParticipantViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class NoticeVoteParticipantViewHolder(
        private val binding: ItemNoticeVoteParticipantBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: NoticeVoteParticipant) {
            binding.profileImageUrl = item.profileImageUrl
            binding.displayName = "${item.nickname} / ${item.name}"
            binding.executePendingBindings()
        }
    }

    class NoticeVoteParticipantDiffCallback : DiffUtil.ItemCallback<NoticeVoteParticipant>() {
        override fun areItemsTheSame(
            oldItem: NoticeVoteParticipant,
            newItem: NoticeVoteParticipant
        ): Boolean = oldItem.memberId == newItem.memberId

        override fun areContentsTheSame(
            oldItem: NoticeVoteParticipant,
            newItem: NoticeVoteParticipant
        ): Boolean = oldItem == newItem
    }
}
