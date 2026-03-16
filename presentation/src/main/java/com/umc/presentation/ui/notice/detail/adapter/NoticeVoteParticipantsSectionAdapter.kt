package com.umc.presentation.ui.notice.detail.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.umc.domain.model.notice.NoticeVoteParticipant
import com.umc.presentation.databinding.ItemNoticeVoteParticipantBinding
import com.umc.presentation.databinding.ItemNoticeVoteParticipantHeaderBinding

sealed interface VoteParticipantListItem {
    data class Header(
        val optionId: Long,
        val title: String,
        val count: Int
    ) : VoteParticipantListItem

    data class Participant(
        val optionId: Long,
        val member: NoticeVoteParticipant
    ) : VoteParticipantListItem
}

class NoticeVoteParticipantsSectionAdapter :
    ListAdapter<VoteParticipantListItem, RecyclerView.ViewHolder>(DiffCallback()) {

    companion object {
        const val TYPE_HEADER = 0
        const val TYPE_PARTICIPANT = 1
    }

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is VoteParticipantListItem.Header -> TYPE_HEADER
        is VoteParticipantListItem.Participant -> TYPE_PARTICIPANT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> HeaderViewHolder(
                ItemNoticeVoteParticipantHeaderBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            else -> ParticipantViewHolder(
                ItemNoticeVoteParticipantBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> holder.bind(getItem(position) as VoteParticipantListItem.Header)
            is ParticipantViewHolder -> holder.bind(getItem(position) as VoteParticipantListItem.Participant)
        }
    }

    class HeaderViewHolder(
        private val binding: ItemNoticeVoteParticipantHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: VoteParticipantListItem.Header) {
            binding.textOptionTitle.text = item.title
            binding.textOptionCount.text = binding.root.context.getString(
                com.umc.presentation.R.string.notice_vote_people_count,
                item.count
            )
}
    }

    class ParticipantViewHolder(
        private val binding: ItemNoticeVoteParticipantBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: VoteParticipantListItem.Participant) {
            binding.profileImageUrl = item.member.profileImageUrl
            binding.displayName = "${item.member.nickname} / ${item.member.name}"
            binding.executePendingBindings()
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<VoteParticipantListItem>() {
        override fun areItemsTheSame(oldItem: VoteParticipantListItem, newItem: VoteParticipantListItem): Boolean {
            return when {
                oldItem is VoteParticipantListItem.Header && newItem is VoteParticipantListItem.Header -> {
                    oldItem.optionId == newItem.optionId
                }
                oldItem is VoteParticipantListItem.Participant && newItem is VoteParticipantListItem.Participant -> {
                    oldItem.optionId == newItem.optionId && oldItem.member.memberId == newItem.member.memberId
                }
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: VoteParticipantListItem, newItem: VoteParticipantListItem): Boolean {
            return oldItem == newItem
        }
    }
}
