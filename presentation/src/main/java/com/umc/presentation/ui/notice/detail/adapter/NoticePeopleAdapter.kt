package com.umc.presentation.ui.notice.detail.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.umc.domain.model.notice.User
import com.umc.presentation.databinding.ItemNoticeBinding
import com.umc.presentation.databinding.ItemNoticePeopleCardBinding

class NoticePeopleAdapter(
) : ListAdapter<User, RecyclerView.ViewHolder> (
    NoticeDiffCallBack()
) {

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder) {
            is NoticePeopleViewHolder -> holder.bind(currentList[position])
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemNoticePeopleCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NoticePeopleViewHolder(binding)
    }

    fun getItemPosition(item: User): Int {
        return currentList.indexOf(item)
    }
}

class NoticeDiffCallBack : DiffUtil.ItemCallback<User>() {
    override fun areContentsTheSame(
        oldItem: User,
        newItem: User
    ): Boolean {
        return oldItem == newItem
    }

    override fun areItemsTheSame(
        oldItem: User,
        newItem: User
    ): Boolean {
        return oldItem.id == newItem.id
    }
}