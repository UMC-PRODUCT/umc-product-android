package com.umc.presentation.ui.notice.adapter

import android.annotation.SuppressLint
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.umc.domain.model.enums.NoticeCategory
import com.umc.domain.model.notice.Notice
import com.umc.presentation.R
import com.umc.presentation.databinding.ItemNoticeBinding
import com.umc.presentation.extension.gone
import com.umc.presentation.extension.visible

class NoticeViewHolder(
    private val binding: ItemNoticeBinding,
    private val listener: NoticeAdapter.NoticeDelegate
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: Notice) {
        binding.apply {
            binding.root.setOnClickListener { listener.onClickNotice(item) }
            if (item.isMustRead) {
                ubuttonMustRead.visible()
                spaceBetween.visible()
                layoutNotice.setBackgroundResource(R.drawable.bg_rect_warning100_stroke_neutral200_radius12)
            } else {
                ubuttonMustRead.gone()
                spaceBetween.gone()
                layoutNotice.setBackgroundResource(R.drawable.bg_rect_neutral000_stroke_neutral200_radius12)
            }
            setCategory(item.category)
            textDate.text = item.date
            textTitle.text = item.title
            textContent.text = item.content
            textAuthor.text = item.author
            textSeeCount.text = "조회 ${item.count}"
        }
    }

    private fun setCategory(category: NoticeCategory) {
        binding.ubuttonCategory.apply {
            when (category) {
                NoticeCategory.CENTRAL_OFFICE -> {
                    setUBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.neutral200))
                    setTextColor(ContextCompat.getColor(binding.root.context, R.color.neutral700))
                    setText(binding.root.context.getString(R.string.central))
                }
                NoticeCategory.BRANCH -> {
                    setUBackgroundColor(ContextCompat.getColor(context, R.color.neutral200))
                    setTextColor(ContextCompat.getColor(context, R.color.neutral700))
                    setText(context.getString(R.string.branch))
                }
                NoticeCategory.SCHOOL -> {
                    setUBackgroundColor(ContextCompat.getColor(context, R.color.primary100))
                    setTextColor(ContextCompat.getColor(context, R.color.primary600))
                    setText(context.getString(R.string.school))
                }
                NoticeCategory.PART -> {
                    setUBackgroundColor(ContextCompat.getColor(context, R.color.success100))
                    setTextColor(ContextCompat.getColor(context, R.color.success700))
                    setText(context.getString(R.string.part))
                }
            }
        }
    }

}