package com.umc.presentation.ui.notice.detail.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.umc.domain.model.notice.NoticeImage
import com.umc.presentation.databinding.ItemNoticeDetailFullscreenImageBinding

class NoticeDetailFullScreenImageAdapter : ListAdapter<NoticeImage, NoticeDetailFullScreenImageViewHolder>(
    NoticeFullScreenImageDiffCallback()
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoticeDetailFullScreenImageViewHolder {
        val binding = ItemNoticeDetailFullscreenImageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NoticeDetailFullScreenImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoticeDetailFullScreenImageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class NoticeDetailFullScreenImageViewHolder(
    private val binding: ItemNoticeDetailFullscreenImageBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: NoticeImage) {
        binding.imageFullScreen.load(item.url) {
            crossfade(true)
        }
    }
}

class NoticeFullScreenImageDiffCallback : DiffUtil.ItemCallback<NoticeImage>() {
    override fun areItemsTheSame(oldItem: NoticeImage, newItem: NoticeImage): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: NoticeImage, newItem: NoticeImage): Boolean {
        return oldItem == newItem
    }
}
