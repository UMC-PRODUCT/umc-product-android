package com.umc.presentation.ui.notice.write.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.umc.domain.model.organization.Chapter
import com.umc.presentation.databinding.ItemSchoolBinding

class ChapterViewHolder(
    private val binding: ItemSchoolBinding,
    private val listener: ChapterListAdapter.ChapterListDelegate
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: Chapter, isSelected: Boolean) {
        binding.textSchool.text = item.name
        binding.imageCheck.visibility = if (isSelected) View.VISIBLE else View.GONE
        binding.root.setOnClickListener {
            listener.onClickChapter(item)
        }
    }
}