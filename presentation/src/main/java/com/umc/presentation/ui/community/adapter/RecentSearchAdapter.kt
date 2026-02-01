package com.umc.presentation.ui.community.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.umc.presentation.databinding.ItemCommunitySearchRecentBinding

interface RecentSearchDelegate {
    fun onRecentSearchClicked(keyword: String) // 아이템 클릭 (검색)
    fun onDeleteClicked(keyword: String) // X 버튼 클릭 (삭제)
}

class RecentSearchAdapter(private val delegate: RecentSearchDelegate) :
    ListAdapter<String, RecentSearchAdapter.RecentSearchViewHolder>(RecentSearchDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentSearchViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemCommunitySearchRecentBinding.inflate(layoutInflater, parent, false)
        return RecentSearchViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecentSearchViewHolder, position: Int) {
        holder.bind(getItem(position), delegate)
    }

    inner class RecentSearchViewHolder(private val binding: ItemCommunitySearchRecentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(keyword: String, delegate: RecentSearchDelegate) {
            binding.itemTvKeyword.text = keyword

            // 키워드 클릭 시 검색 로직 실행
            binding.root.setOnClickListener {
                delegate.onRecentSearchClicked(keyword)
            }

            // X 버튼 클릭 시 삭제 로직 실행
            binding.itemBtnDelete.setOnClickListener {
                delegate.onDeleteClicked(keyword)
            }

        }
    }

    companion object {
        private val RecentSearchDiffCallback = object : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
                return oldItem == newItem
            }
        }
    }
}