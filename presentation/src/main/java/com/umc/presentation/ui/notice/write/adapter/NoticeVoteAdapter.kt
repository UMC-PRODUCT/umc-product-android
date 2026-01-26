package com.umc.presentation.ui.notice.write.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.umc.domain.model.notice.Notice
import com.umc.presentation.databinding.ItemNoticeBinding
import com.umc.presentation.databinding.ItemNoticeWriteVoteBinding

class NoticeVoteAdapter(
    private val listener: NoticeVoteDelegate
) : ListAdapter<String, RecyclerView.ViewHolder> (
    NoticeVoteDiffCallBack()
) {

    interface NoticeVoteDelegate {
        fun onTextChanged(position: Int, text: String)
        fun onClickDelete(position: Int)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder) {
            is NoticeVoteViewHolder -> holder.bind(currentList[position], position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemNoticeWriteVoteBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NoticeVoteViewHolder(binding, listener)
    }

    fun getItemPosition(item: String): Int {
        return currentList.indexOf(item)
    }
}

class NoticeVoteDiffCallBack : DiffUtil.ItemCallback<String>() {
    override fun areContentsTheSame(
        oldItem: String,
        newItem: String
    ): Boolean {
        return oldItem == newItem
    }

    override fun areItemsTheSame(
        oldItem: String,
        newItem: String
    ): Boolean {
        return oldItem == newItem
    }
}