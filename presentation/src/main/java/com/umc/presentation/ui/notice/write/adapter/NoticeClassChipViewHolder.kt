package com.umc.presentation.ui.notice.write.adapter

import android.annotation.SuppressLint
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.umc.domain.model.notice.NoticeChipState
import com.umc.presentation.R
import com.umc.presentation.databinding.ItemHomeCategoryBinding

class NoticeClassChipViewHolder(
    private val binding: ItemHomeCategoryBinding,
    private val listener: NoticeClassChipAdapter.NoticeClassChipDelegate
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: NoticeChipState) {
        binding.itemChip.apply {
            setText(item.selectedDisplayName ?: item.text)
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
            setUChipBackgroundColor(ContextCompat.getColor(context, R.color.neutral800))
            setUChipBorder(ContextCompat.getColor(context, R.color.neutral800), 1)
            setUChipTextColor(ContextCompat.getColor(context, R.color.neutral100))

            if (item.hanBottomSheet) setNextIcon(R.drawable.ic_delete)
        }
    }

    @SuppressLint("ResourceAsColor")
    private fun bindUnClickedView(item: NoticeChipState) {
        binding.itemChip.apply {
            setUChipBackgroundColor(ContextCompat.getColor(context, R.color.neutral000))
            setUChipBorder(ContextCompat.getColor(context, R.color.neutral200), 1)
            setUChipTextColor(ContextCompat.getColor(context, R.color.neutral500))

            if (item.hanBottomSheet) setNextIcon(R.drawable.ic_dropdown)
        }
    }
}