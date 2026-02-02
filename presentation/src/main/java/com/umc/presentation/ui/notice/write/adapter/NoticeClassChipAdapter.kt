package com.umc.presentation.ui.notice.write.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.umc.domain.model.notice.NoticeChipState
import com.umc.presentation.databinding.ItemHomeCategoryBinding

class NoticeClassChipAdapter(
    private val listener: NoticeClassChipDelegate
) : ListAdapter<NoticeChipState, RecyclerView.ViewHolder> (
    NoticeClassChipDiffCallBack()
) {

    interface NoticeClassChipDelegate {
        fun onClickChip(item: NoticeChipState)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder) {
            is NoticeClassChipViewHolder -> holder.bind(currentList[position])
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemHomeCategoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false // <- 반드시 false
        )
        return NoticeClassChipViewHolder(binding, listener)
    }

    fun getItemPosition(item: NoticeChipState): Int {
        return currentList.indexOf(item)
    }
}

class NoticeClassChipDiffCallBack : DiffUtil.ItemCallback<NoticeChipState>() {
    override fun areContentsTheSame(
        oldItem: NoticeChipState,
        newItem: NoticeChipState
    ): Boolean {
        return oldItem == newItem
    }

    override fun areItemsTheSame(
        oldItem: NoticeChipState,
        newItem: NoticeChipState
    ): Boolean {
        return oldItem.text == newItem.text
    }
}