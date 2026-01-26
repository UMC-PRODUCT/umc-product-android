package com.umc.presentation.ui.notice.write.adapter

import androidx.recyclerview.widget.RecyclerView
import com.umc.presentation.databinding.ItemNoticeWriteVoteBinding

class NoticeVoteViewHolder(
    private val binding: ItemNoticeWriteVoteBinding,
    private val listener: NoticeVoteAdapter.NoticeVoteDelegate
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(text: String, position: Int) {
        binding.apply {
            textFieldVote.setPlaceHolder("항목" + (position+1))
            textFieldVote.setOnTextChangedListener {
                listener.onTextChanged(position, text)
            }
            imageDelete.setOnClickListener {
                listener.onClickDelete(position)
            }
        }
    }
}