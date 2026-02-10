package com.umc.presentation.ui.signUp.adapter

import androidx.recyclerview.widget.RecyclerView
import com.umc.domain.model.school.SchoolInfo
import com.umc.presentation.databinding.ItemSchoolBinding

class SchoolListViewHolder(
    private val binding: ItemSchoolBinding,
    private val listener: SchoolListAdapter.SchoolListDelegate
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: SchoolInfo) {
        binding.apply {
            root.setOnClickListener { listener.onClickNotice(item) }
            textSchool.text = item.schoolName
        }
    }
}