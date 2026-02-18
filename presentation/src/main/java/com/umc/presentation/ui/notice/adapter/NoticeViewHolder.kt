package com.umc.presentation.ui.notice.adapter

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.umc.domain.model.notice.NoticeSummary
import com.umc.domain.model.notice.NoticeTarget
import com.umc.presentation.R
import com.umc.presentation.databinding.ItemNoticeBinding
import com.umc.presentation.extension.gone
import com.umc.presentation.extension.visible
import com.umc.presentation.util.UFormat.parseDateTime

class NoticeViewHolder(
    private val binding: ItemNoticeBinding,
    private val listener: NoticeAdapter.NoticeDelegate
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: NoticeSummary) {
        binding.apply {
            binding.root.setOnClickListener { listener.onClickNotice(item) }
            if (item.shouldSendNotification) {
                ubuttonMustRead.visible()
                spaceBetween.visible()
                layoutNotice.setBackgroundResource(R.drawable.bg_rect_warning100_stroke_neutral200_radius12)
            } else {
                ubuttonMustRead.gone()
                spaceBetween.gone()
                layoutNotice.setBackgroundResource(R.drawable.bg_rect_neutral000_stroke_neutral200_radius12)
            }
            setCategory(item.targetInfo)
            textDate.text = parseDateTime(item.createdAt).first
            textTitle.text = item.title
            textContent.text = item.content
            textAuthor.text = item.authorNickname
            textSeeCount.text = "조회 ${item.viewCount}"
        }
    }

    private fun setCategory(target: NoticeTarget) {
        val context = binding.root.context

        binding.ubuttonCategory.apply {
            when {
                target.targetParts.isNotEmpty() -> {
                    setUBackgroundColor(ContextCompat.getColor(context, R.color.success100))
                    setTextColor(ContextCompat.getColor(context, R.color.success700))
                    setText(context.getString(R.string.part))
                }

                target.targetSchoolId != null -> {
                    setUBackgroundColor(ContextCompat.getColor(context, R.color.primary100))
                    setTextColor(ContextCompat.getColor(context, R.color.primary600))
                    setText(context.getString(R.string.school))
                }

                target.targetChapterId != null -> {
                    setUBackgroundColor(ContextCompat.getColor(context, R.color.neutral200))
                    setTextColor(ContextCompat.getColor(context, R.color.neutral700))
                    setText(context.getString(R.string.branch))
                }

                else -> {
                    setUBackgroundColor(ContextCompat.getColor(context, R.color.neutral200))
                    setTextColor(ContextCompat.getColor(context, R.color.neutral700))
                    setText(context.getString(R.string.central))
                }
            }
        }
    }
}