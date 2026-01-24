package com.umc.presentation.ui.notice.adapter

import android.annotation.SuppressLint
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.umc.domain.model.notice.NoticeChipState
import com.umc.presentation.R
import com.umc.presentation.databinding.ItemHomeCategoryBinding

class NoticeChipViewHolder(
    private val binding: ItemHomeCategoryBinding,
    private val listener: NoticeChipAdapter.NoticeChipDelegate
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: NoticeChipState) {
        binding.itemChip.apply {
            setText(item.text)
            setTextStyle(R.style.CalloutBold)
            setOnClickListener { listener.onClickChip(item) }
        }
        if (item.isClicked) {
            bindClickedView(item)
        } else {
            bindUnClickedView(item)
        }
    }

    @SuppressLint("ResourceAsColor")
    private fun bindClickedView(item: NoticeChipState) {
        binding.itemChip.apply {
            setUChipBackgroundColor(ContextCompat.getColor(context, R.color.primary500))
            setUChipBorder(ContextCompat.getColor(context, R.color.primary500), 1)
            setUChipTextColor(ContextCompat.getColor(context, R.color.neutral000))
        }
    }

    @SuppressLint("ResourceAsColor")
    private fun bindUnClickedView(item: NoticeChipState) {
        binding.itemChip.apply {
            setUChipBackgroundColor(ContextCompat.getColor(context, R.color.neutral000))
            setUChipBorder(ContextCompat.getColor(context, R.color.neutral200), 1)
            setUChipTextColor(ContextCompat.getColor(context, R.color.neutral500))
        }
    }
}