package com.umc.presentation.ui.notice.write.adapter

import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.umc.domain.model.enums.NoticeCategory
import com.umc.domain.model.notice.Notice
import com.umc.presentation.R
import com.umc.presentation.databinding.ItemNoticeBinding
import com.umc.presentation.databinding.ItemNoticeImageBinding
import com.umc.presentation.extension.gone
import com.umc.presentation.extension.visible

class NoticeImageViewHolder(
    private val binding: ItemNoticeImageBinding,
    private val listener: NoticeImageAdapter.NoticeImageDelegate
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(uri: Uri) {
        binding.apply {
            imageItem.setImageURI(uri)
            imageDelete.setOnClickListener { listener.onClickDelete(uri) }
        }
    }
}