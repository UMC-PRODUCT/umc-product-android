package com.umc.presentation.ui.notice.write.adapter

import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.umc.presentation.databinding.ItemNoticeImageBinding
import com.umc.presentation.ui.notice.write.model.NoticeImageItem

class NoticeImageViewHolder(
    private val binding: ItemNoticeImageBinding,
    private val listener: NoticeImageAdapter.NoticeImageDelegate
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: NoticeImageItem) {
        binding.apply {
            when {
                item.uri != null -> {
                    imageItem.load(item.uri) {
                        crossfade(true)
                    }
                }
                item.url.isNotBlank() -> {
                    imageItem.load(item.url) {
                        crossfade(true)
                    }
                }
            }
            
            imageDelete.setOnClickListener { listener.onClickDelete(item) }
        }
    }
}
