package com.umc.presentation.ui.notice.write.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.umc.domain.model.notice.Notice
import com.umc.presentation.databinding.ItemNoticeBinding
import com.umc.presentation.databinding.ItemNoticeImageBinding

class NoticeImageAdapter(
    private val listener: NoticeImageDelegate
) : ListAdapter<Uri, RecyclerView.ViewHolder> (
    NoticeImageDiffCallBack()
) {

    interface NoticeImageDelegate {
        fun onClickDelete(uri: Uri)
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

    fun getItemPosition(item: Uri): Int {
        return currentList.indexOf(item)
    }
}

class NoticeImageDiffCallBack : DiffUtil.ItemCallback<Uri>() {
    override fun areContentsTheSame(
        oldItem: Uri,
        newItem: Uri
    ): Boolean {
        return oldItem == newItem
    }

    override fun areItemsTheSame(
        oldItem: Uri,
        newItem: Uri
    ): Boolean {
        return oldItem.path == newItem.path
    }
}