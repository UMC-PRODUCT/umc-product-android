package com.umc.presentation.ui.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.umc.domain.model.home.LocationItem
import com.umc.presentation.databinding.ItemBottomSheetLocationRecentBinding

interface RecentLocationDelegate {
    fun onRecentClicked(item: LocationItem) // 최근 기록 클릭 시 검색창에 입력 및 검색 실행
}

class BottomSheetLocationRecentAdapter(
    private val delegate: RecentLocationDelegate
) : ListAdapter<LocationItem, BottomSheetLocationRecentAdapter.ViewHolder>(LocationDiffCallback) {

    inner class ViewHolder(private val binding: ItemBottomSheetLocationRecentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item : LocationItem) {
            binding.itemTvLocation.text = item.address

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
        private val LocationDiffCallback = object : DiffUtil.ItemCallback<LocationItem>() {
            override fun areItemsTheSame(oldItem: LocationItem, newItem: LocationItem) =
                oldItem.title == newItem.title && oldItem.address == newItem.address
            override fun areContentsTheSame(oldItem: LocationItem, newItem: LocationItem) =
                oldItem == newItem
        }
    }
}