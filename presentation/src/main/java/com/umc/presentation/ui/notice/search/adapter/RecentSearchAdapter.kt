package com.umc.presentation.ui.notice.search.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.umc.domain.model.notice.NoticeChipState
import com.umc.presentation.databinding.ItemHomeCategoryBinding
import com.umc.presentation.databinding.ItemNoticeRecentSearchBinding

class RecentSearchAdapter(
    private val listener: RecentSearchDelegate
) : ListAdapter<String, RecyclerView.ViewHolder> (
    RecentSearchDiffCallBack()
) {

    interface RecentSearchDelegate {
        fun onClickItem(text: String)
        fun onClickDelete(text: String)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder) {
            is RecentSearchViewHolder -> holder.bind(currentList[position])
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemNoticeRecentSearchBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false // <- 반드시 false
        )
        return RecentSearchViewHolder(binding, listener)
    }
}

class RecentSearchDiffCallBack : DiffUtil.ItemCallback<String>() {
    override fun areContentsTheSame(
        oldItem: String,
        newItem: String
    ): Boolean {
        return oldItem == newItem
    }

    override fun areItemsTheSame(
        oldItem: String,
        newItem: String
    ): Boolean {
        return oldItem == newItem
    }
}