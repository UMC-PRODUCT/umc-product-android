package com.umc.presentation.ui.notice.detail.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.umc.domain.model.notice.NoticeVote
import com.umc.domain.model.notice.NoticeVoteOption
import com.umc.presentation.databinding.ItemNoteDetailVoteBinding
import com.umc.presentation.databinding.ItemNoteDetailVoteResultBinding

class NoticeDetailVoteAdapter(
    private val listener: NoticeDetailVoteDelegate
) : ListAdapter<NoticeVoteOption, RecyclerView.ViewHolder>(
    NoticeDetailVoteDiffCallBack()
) {

    companion object {
        const val VIEW_TYPE_VOTING = 0
        const val VIEW_TYPE_RESULT = 1
    }

    interface NoticeDetailVoteDelegate {
        fun onClickVote(item: NoticeVoteOption)
    }

    private var selectedOptionIds: Set<Long> = emptySet()
    private var isVoted: Boolean = false
    private var vote: NoticeVote? = null

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

    fun setVotedState(voted: Boolean, voteData: NoticeVote?) {
        val oldIsVoted = isVoted
        isVoted = voted
        vote = voteData
        if (oldIsVoted != isVoted) {
            notifyDataSetChanged()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (isVoted) VIEW_TYPE_RESULT else VIEW_TYPE_VOTING
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is NoticeDetailVoteViewHolder -> holder.bind(
                item,
                selectedOptionIds.contains(item.optionId)
            )
            is NoticeDetailVoteResultViewHolder -> holder.bind(
                item,
                selectedOptionIds.contains(item.optionId)
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_RESULT -> {
                val binding = ItemNoteDetailVoteResultBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                NoticeDetailVoteResultViewHolder(binding)
            }
            else -> {
                val binding = ItemNoteDetailVoteBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                NoticeDetailVoteViewHolder(binding, listener)
            }
        }
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
