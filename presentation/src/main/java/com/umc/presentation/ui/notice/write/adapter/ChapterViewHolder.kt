package com.umc.presentation.ui.notice.write.adapter

import androidx.recyclerview.widget.RecyclerView
import com.umc.domain.model.organization.Chapter
import com.umc.presentation.databinding.ItemSchoolBinding

class ChapterViewHolder(
    private val binding: ItemSchoolBinding,
    private val listener: ChapterListAdapter.ChapterListDelegate
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: Chapter) {
        binding.textSchool.text = item.name
        binding.root.setOnClickListener {
            listener.onClickChapter(item)
        }
    }
}