package com.umc.presentation.ui.notice.detail.adapter

import android.content.res.ColorStateList
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.umc.domain.model.notice.VoteItem
import com.umc.presentation.R
import com.umc.presentation.databinding.ItemNoteDetailVoteBinding

class NoticeDetailVoteViewHolder(
    private val binding: ItemNoteDetailVoteBinding,
    private val listener: NoticeDetailVoteAdapter.NoticeDetailVoteDelegate
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: VoteItem) {
        binding.apply {
            root.setOnClickListener { listener.onClickVote(item) }
            if (item.isChecked) {
                imageCheckBox.imageTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(imageCheckBox.context, R.color.primary500))
            } else {
                imageCheckBox.imageTintList = null
            }
            textName.text = item.name
        }
    }
}