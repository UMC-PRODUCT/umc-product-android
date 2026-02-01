package com.umc.presentation.ui.notice.detail.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.umc.domain.model.notice.VoteItem
import com.umc.presentation.databinding.ItemNoteDetailVoteBinding

class NoticeDetailVoteAdapter(
    private val listener: NoticeDetailVoteDelegate
) : ListAdapter<VoteItem, RecyclerView.ViewHolder> (
    NoticeDetailVoteDiffCallBack()
) {

    interface NoticeDetailVoteDelegate {
        fun onClickVote(item: VoteItem)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder) {
            is NoticeDetailVoteViewHolder -> holder.bind(currentList[position])
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemNoteDetailVoteBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NoticeDetailVoteViewHolder(binding, listener)
    }

    fun getItemPosition(item: VoteItem): Int {
        return currentList.indexOf(item)
    }
}

class NoticeDetailVoteDiffCallBack : DiffUtil.ItemCallback<VoteItem>() {
    override fun areContentsTheSame(
        oldItem: VoteItem,
        newItem: VoteItem
    ): Boolean {
        return oldItem == newItem
    }

    override fun areItemsTheSame(
        oldItem: VoteItem,
        newItem: VoteItem
    ): Boolean {
        return oldItem.name == newItem.name
    }
}