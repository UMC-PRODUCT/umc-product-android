package com.umc.presentation.ui.notice.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.umc.domain.model.notice.Notice
import com.umc.presentation.databinding.ItemNoticeBinding

class NoticeAdapter(
    private val listener: NoticeDelegate
) : ListAdapter<Notice, RecyclerView.ViewHolder> (
    NoticeDiffCallBack()
) {

    interface NoticeDelegate {
        fun onClickNotice(item: Notice)
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

    fun getItemPosition(item: Notice): Int {
        return currentList.indexOf(item)
    }
}

class NoticeDiffCallBack : DiffUtil.ItemCallback<Notice>() {
    override fun areContentsTheSame(
        oldItem: Notice,
        newItem: Notice
    ): Boolean {
        return oldItem == newItem
    }

    override fun areItemsTheSame(
        oldItem: Notice,
        newItem: Notice
    ): Boolean {
        return oldItem.id == newItem.id
    }
}