package com.umc.presentation.ui.signUp.adapter

import androidx.recyclerview.widget.RecyclerView
import com.umc.domain.model.signUp.School
import com.umc.presentation.databinding.ItemSchoolBinding

class SchoolListViewHolder(
    private val binding: ItemSchoolBinding,
    private val listener: SchoolListAdapter.SchoolListDelegate
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: School) {
        binding.apply {
            root.setOnClickListener { listener.onClickNotice(item) }
            textSchool.text = item.schoolName
        }
    }
}