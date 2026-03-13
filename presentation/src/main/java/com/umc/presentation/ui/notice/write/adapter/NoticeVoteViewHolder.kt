package com.umc.presentation.ui.notice.write.adapter

import androidx.recyclerview.widget.RecyclerView
import com.umc.presentation.databinding.ItemNoticeWriteVoteBinding
import com.umc.presentation.extension.gone

class NoticeVoteViewHolder(
    private val binding: ItemNoticeWriteVoteBinding,
    private val listener: NoticeVoteAdapter.NoticeVoteDelegate
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(text: String, position: Int) {
        binding.apply {
            textFieldVote.setText(text)
            textFieldVote.setPlaceHolder("항목" + (position+1))
            textFieldVote.setOnTextChangedListener {
                listener.onTextChanged(position, it)
            }

            if (position == 0 || position == 1) {
                imageDelete.gone()
            } else {
                imageDelete.setOnClickListener {
                    listener.onClickDelete(position)
                }
            }
        }
    }
}