package com.umc.presentation.ui.notice.detail.adapter

import android.content.res.ColorStateList
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.umc.domain.model.notice.NoticeVoteOption
import com.umc.presentation.R
import com.umc.presentation.databinding.ItemNoteDetailVoteBinding

class NoticeDetailVoteViewHolder(
    private val binding: ItemNoteDetailVoteBinding,
    private val listener: NoticeDetailVoteAdapter.NoticeDetailVoteDelegate
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: NoticeVoteOption, isSelected: Boolean = false) {
        binding.apply {
            root.setOnClickListener { listener.onClickVote(item) }
            imageCheckBox.setImageResource(
                if (isSelected) R.drawable.ic_check_box else R.drawable.ic_check_box_empty
            )
            imageCheckBox.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    imageCheckBox.context,
                    if (isSelected) R.color.primary500 else R.color.neutral400
                )
            )
            textName.text = item.content
        }
    }
}