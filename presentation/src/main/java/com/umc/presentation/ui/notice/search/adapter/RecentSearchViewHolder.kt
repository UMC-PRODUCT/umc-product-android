package com.umc.presentation.ui.notice.search.adapter

import androidx.recyclerview.widget.RecyclerView
import com.umc.presentation.databinding.ItemNoticeRecentSearchBinding

class RecentSearchViewHolder(
    private val binding: ItemNoticeRecentSearchBinding,
    private val listener: RecentSearchAdapter.RecentSearchDelegate
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(text: String) {
        binding.apply {
            textSearch.text = text
            textSearch.setOnClickListener { listener.onClickItem(text) }
            imageClose.setOnClickListener { listener.onClickDelete(text) }
        }
    }
}