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

    private var selectedChapterId: Long? = null

    interface ChapterListDelegate {
        fun onClickChapter(item: Chapter)
    }

    fun setSelectedChapterId(id: Long?) {
        val previousId = selectedChapterId
        selectedChapterId = id
        
        // 이전 선택 항목과 새 선택 항목의 position을 찾아서 갱신
        val previousPosition = currentList.indexOfFirst { it.id.toLong() == previousId }
        val newPosition = currentList.indexOfFirst { it.id.toLong() == id }
        
        if (previousPosition != -1) {
            notifyItemChanged(previousPosition)
        }
        if (newPosition != -1 && newPosition != previousPosition) {
            notifyItemChanged(newPosition)
        }
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
            is ChapterViewHolder -> {
                val item = currentList[position]
                val isSelected = item.id.toLong() == selectedChapterId
                holder.bind(item, isSelected)
            }
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
