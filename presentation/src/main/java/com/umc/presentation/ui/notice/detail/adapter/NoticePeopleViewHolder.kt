package com.umc.presentation.ui.notice.detail.adapter

import androidx.recyclerview.widget.RecyclerView
import com.umc.domain.model.notice.ChallengerReadInfo
import com.umc.presentation.databinding.ItemNoticePeopleCardBinding

class NoticePeopleViewHolder(
    private val binding: ItemNoticePeopleCardBinding,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: ChallengerReadInfo) {
        binding.apply {
            textName.text = item.name
            textBranch.text = item.chapterName
            textSchool.text = item.schoolName
            ubuttonPart.setText(item.part)
//            if (item.isSendNotification && !item.isCheck) viewNew.visible()
//            if (item.isCheck) {
//                viewNew.gone()
//                imageCheck.visible()
//            }
        }
    }
}