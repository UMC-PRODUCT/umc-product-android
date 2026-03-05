package com.umc.presentation.ui.notice.write.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.umc.presentation.databinding.ItemNoticeImageBinding
import com.umc.presentation.ui.notice.write.model.NoticeImageItem

class NoticeImageAdapter(
    private val listener: NoticeImageDelegate
) : ListAdapter<NoticeImageItem, RecyclerView.ViewHolder> (
    NoticeImageDiffCallBack()
) {

    interface NoticeImageDelegate {
        fun onClickDelete(item: NoticeImageItem)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder) {
            is NoticeImageViewHolder -> holder.bind(currentList[position])
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemNoticeImageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NoticeImageViewHolder(binding, listener)
    }

    fun getItemPosition(item: NoticeImageItem): Int {
        return currentList.indexOf(item)
    }
}

class NoticeImageDiffCallBack : DiffUtil.ItemCallback<NoticeImageItem>() {
    override fun areContentsTheSame(
        oldItem: NoticeImageItem,
        newItem: NoticeImageItem
    ): Boolean {
        return oldItem == newItem
    }

    override fun areItemsTheSame(
        oldItem: NoticeImageItem,
        newItem: NoticeImageItem
    ): Boolean {
        return if (oldItem.id != 0L && newItem.id != 0L) {
            oldItem.id == newItem.id
        } else {
            oldItem.uri == newItem.uri
        }
    }
}
