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
            if (isSelected) {
                imageCheckBox.imageTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(imageCheckBox.context, R.color.primary500))
            } else {
                imageCheckBox.imageTintList = null
            }
            textName.text = item.content
        }
    }
}