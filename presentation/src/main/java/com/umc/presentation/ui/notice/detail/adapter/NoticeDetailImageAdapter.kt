package com.umc.presentation.ui.notice.detail.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.umc.domain.model.notice.NoticeImage
import com.umc.presentation.databinding.ItemNoticeDetailImageBinding

class NoticeDetailImageAdapter : ListAdapter<NoticeImage, NoticeDetailImageViewHolder>(
    NoticeImageDiffCallback()
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoticeDetailImageViewHolder {
        val binding = ItemNoticeDetailImageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NoticeDetailImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoticeDetailImageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class NoticeDetailImageViewHolder(
    private val binding: ItemNoticeDetailImageBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: NoticeImage) {
        binding.imageItem.load(item.url) {
            crossfade(true)
        }
    }
}

class NoticeImageDiffCallback : DiffUtil.ItemCallback<NoticeImage>() {
    override fun areItemsTheSame(oldItem: NoticeImage, newItem: NoticeImage): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: NoticeImage, newItem: NoticeImage): Boolean {
        return oldItem == newItem
    }
}
