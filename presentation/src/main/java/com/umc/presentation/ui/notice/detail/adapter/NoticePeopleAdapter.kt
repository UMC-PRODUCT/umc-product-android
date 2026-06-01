package com.umc.presentation.ui.notice.detail.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.umc.domain.model.notice.ChallengerReadInfo
import com.umc.presentation.databinding.ItemNoticePeopleCardBinding

class NoticePeopleAdapter(
) : ListAdapter<ChallengerReadInfo, NoticePeopleViewHolder>(
    NoticeDiffCallBack()
) {

    override fun onBindViewHolder(holder: NoticePeopleViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoticePeopleViewHolder {
        val binding = ItemNoticePeopleCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NoticePeopleViewHolder(binding)
    }
}

class NoticeDiffCallBack : DiffUtil.ItemCallback<ChallengerReadInfo>() {
    override fun areContentsTheSame(
        oldItem: ChallengerReadInfo,
        newItem: ChallengerReadInfo
    ): Boolean {
        return oldItem == newItem
    }

    override fun areItemsTheSame(
        oldItem: ChallengerReadInfo,
        newItem: ChallengerReadInfo
    ): Boolean {
        return oldItem.challengerId == newItem.challengerId
    }
}