package com.umc.presentation.ui.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.umc.domain.model.home.LocationItem
import com.umc.presentation.databinding.ItemBottomSheetLocationRecentBinding

interface RecentLocationDelegate {
    fun onRecentClicked(item: String) // 최근 기록 클릭 시 검색창에 입력 및 검색 실행
}

class BottomSheetLocationRecentAdapter(
    private val delegate: RecentLocationDelegate
) : ListAdapter<String, BottomSheetLocationRecentAdapter.ViewHolder>(LocationDiffCallback) {

    inner class ViewHolder(private val binding: ItemBottomSheetLocationRecentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item : String) {
            binding.itemTvLocation.text = item

            // 아이템 전체 클릭 시 검색 로직 실행
            binding.root.setOnClickListener {
                delegate.onRecentClicked(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBottomSheetLocationRecentBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    companion object {

        private val LocationDiffCallback = object : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
                return oldItem == newItem
            }
        }
    }
}