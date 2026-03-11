package com.umc.presentation.ui.notice.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.umc.domain.model.notice.NoticeSummary
import com.umc.presentation.databinding.ItemNoticeBinding

class NoticeAdapter(
    private val listener: NoticeDelegate
) : ListAdapter<NoticeSummary, RecyclerView.ViewHolder> (
    NoticeDiffCallBack()
) {

    interface NoticeDelegate {
        fun onClickNotice(item: NoticeSummary)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder) {
            is NoticeViewHolder -> holder.bind(currentList[position])
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemNoticeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false // <- 반드시 false
        )
        return NoticeViewHolder(binding, listener)
    }

}

class NoticeDiffCallBack : DiffUtil.ItemCallback<NoticeSummary>() {
    override fun areContentsTheSame(
        oldItem: NoticeSummary,
        newItem: NoticeSummary
    ): Boolean {
        return oldItem == newItem
    }

    override fun areItemsTheSame(
        oldItem: NoticeSummary,
        newItem: NoticeSummary
    ): Boolean {
        return oldItem.id == newItem.id
    }
}