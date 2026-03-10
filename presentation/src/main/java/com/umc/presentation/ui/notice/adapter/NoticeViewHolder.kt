package com.umc.presentation.ui.notice.adapter

import androidx.recyclerview.widget.RecyclerView
import com.umc.domain.model.enums.UserPart
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
            layoutNotice.setBackgroundResource(
                // TODO 필독 생기면 추가 if (item.shouldSendNotification) R.drawable.bg_rect_warning100_stroke_neutral200_radius12
                R.drawable.bg_rect_neutral000_stroke_neutral200_radius12
            )
            setCategories(item.targetInfo)
            textDate.text = parseDateTime(item.createdAt).first
            textTitle.text = item.title
            textContent.text = item.content
            textAuthor.text = item.authorNickname
            textSeeCount.text = "조회 ${item.viewCount}"
        }
    }

    private fun setCategories(target: NoticeTarget) {
        if (target.targetGisuId != 0 || (target.targetChapterId == null && target.targetSchoolId == null && target.targetParts.isEmpty())) {
            binding.ubuttonCentral.visible()
        } else {
            binding.ubuttonCentral.gone()
        }

        if (target.targetChapterId != null) {
            binding.ubuttonChapter.visible()
            binding.ubuttonChapter.setText(target.targetChapterName ?: "지부")
            binding.spaceCentralChapter.visible()
        } else {
            binding.ubuttonChapter.gone()
            binding.spaceCentralChapter.gone()
        }

        if (target.targetSchoolId != null) {
            binding.ubuttonSchool.visible()
            binding.spaceChapterSchool.visible()
        } else {
            binding.ubuttonSchool.gone()
            binding.spaceChapterSchool.gone()
        }

        if (target.targetParts.isNotEmpty()) {
            binding.ubuttonPart.visible()
            binding.spaceSchoolPart.visible()
            binding.ubuttonPart.setText(UserPart.from(target.targetParts.first()).label)
        } else {
            binding.ubuttonPart.gone()
            binding.spaceSchoolPart.gone()
        }
    }
}