package com.umc.presentation.ui.notice.detail.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.umc.domain.model.notice.NoticeVoteOption
import com.umc.presentation.databinding.ItemNoteDetailVoteBinding

class NoticeDetailVoteAdapter(
    private val listener: NoticeDetailVoteDelegate
) : ListAdapter<NoticeVoteOption, NoticeDetailVoteViewHolder>(
    NoticeDetailVoteDiffCallBack()
) {

    interface NoticeDetailVoteDelegate {
        fun onClickVote(item: NoticeVoteOption)
    }

    private var selectedOptionIds: Set<Long> = emptySet()

    fun setSelectedOptionIds(ids: Set<Long>) {
        val oldSelectedIds = selectedOptionIds
        selectedOptionIds = ids

        // Only notify changed positions
        currentList.forEachIndexed { index, item ->
            val wasSelected = oldSelectedIds.contains(item.optionId)
            val isSelected = ids.contains(item.optionId)
            if (wasSelected != isSelected) {
                notifyItemChanged(index)
            }
        }
    }

    override fun onBindViewHolder(holder: NoticeDetailVoteViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, selectedOptionIds.contains(item.optionId))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoticeDetailVoteViewHolder {
        val binding = ItemNoteDetailVoteBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NoticeDetailVoteViewHolder(binding, listener)
    }

    fun getItemPosition(item: NoticeVoteOption): Int {
        return currentList.indexOf(item)
    }
}

class NoticeDetailVoteDiffCallBack : DiffUtil.ItemCallback<NoticeVoteOption>() {
    override fun areContentsTheSame(
        oldItem: NoticeVoteOption,
        newItem: NoticeVoteOption
    ): Boolean {
        return oldItem == newItem
    }

    override fun areItemsTheSame(
        oldItem: NoticeVoteOption,
        newItem: NoticeVoteOption
    ): Boolean {
        return oldItem.optionId == newItem.optionId
    }
}