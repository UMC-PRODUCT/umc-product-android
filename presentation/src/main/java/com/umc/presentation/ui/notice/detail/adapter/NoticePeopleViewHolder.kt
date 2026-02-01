package com.umc.presentation.ui.notice.detail.adapter

import androidx.recyclerview.widget.RecyclerView
import com.umc.domain.model.notice.User
import com.umc.presentation.databinding.ItemNoticePeopleCardBinding
import com.umc.presentation.extension.gone
import com.umc.presentation.extension.visible

class NoticePeopleViewHolder(
    private val binding: ItemNoticePeopleCardBinding,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: User) {
        binding.apply {
            textName.text = "${item.name}(${item.nickName})"
            textBranch.text = item.branch
            textSchool.text = item.school
            ubuttonPart.setText(item.part)
            if (item.isSendNotification && !item.isCheck) viewNew.visible()
            if (item.isCheck) {
                viewNew.gone()
                imageCheck.visible()
            }
        }
    }
}