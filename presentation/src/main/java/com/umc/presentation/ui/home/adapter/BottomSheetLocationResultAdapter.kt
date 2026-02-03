package com.umc.presentation.ui.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.umc.domain.model.home.LocationItem
import com.umc.presentation.databinding.ItemBottomSheetLocationSearchBinding

interface LocationResultDelegate {
    fun onLocationSelected(item: LocationItem) // 선택 버튼 터치 시 최종 적용
}

class BottomSheetLocationResultAdapter(
    private val delegate: LocationResultDelegate
) : ListAdapter<LocationItem, BottomSheetLocationResultAdapter.ViewHolder>(LocationDiffCallback) {

    inner class ViewHolder(private val binding: ItemBottomSheetLocationSearchBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: LocationItem) {
            binding.itemTvLocationTitle.text = item.title
            binding.itemTvLocationContent.text = item.address

            // '선택' 버튼(UButton) 클릭 시 적용
            binding.itemBtnSelect.setOnClickListener {
                delegate.onLocationSelected(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBottomSheetLocationSearchBinding.inflate(
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