package com.umc.presentation.ui.notice.write.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.umc.domain.model.organization.Chapter
import com.umc.presentation.databinding.ItemSchoolBinding

class ChapterListAdapter(
    private val listener: ChapterListDelegate
) : ListAdapter<Chapter, RecyclerView.ViewHolder>(
    ChapterDiffCallBack()
) {

    interface ChapterListDelegate {
        fun onClickChapter(item: Chapter)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChapterViewHolder {
        val binding = ItemSchoolBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ChapterViewHolder(binding, listener)
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        when (holder) {
            is ChapterViewHolder -> holder.bind(currentList[position])
        }
    }
}

class ChapterDiffCallBack : DiffUtil.ItemCallback<Chapter>() {
    override fun areContentsTheSame(oldItem: Chapter, newItem: Chapter): Boolean {
        return oldItem == newItem
    }

    override fun areItemsTheSame(oldItem: Chapter, newItem: Chapter): Boolean {
        return oldItem.id == newItem.id
    }
}
