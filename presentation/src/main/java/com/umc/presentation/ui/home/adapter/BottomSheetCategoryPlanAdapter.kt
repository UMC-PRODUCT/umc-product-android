package com.umc.presentation.ui.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.umc.domain.model.home.CategoryItem
import com.umc.presentation.databinding.ItemBottomSheetCategoryPlanBinding

interface CategoryPlanDelegate {
    fun onCategoryToggled(item: CategoryItem)
}

class BottomSheetCategoryPlanAdapter(
    private val delegate: CategoryPlanDelegate
) : ListAdapter<CategoryItem, BottomSheetCategoryPlanAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(private val binding: ItemBottomSheetCategoryPlanBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CategoryItem) {
            binding.item = item

            // 아이템 클릭 시 체크박스 상태 반전 및 이벤트 전달
            binding.root.setOnClickListener {
                delegate.onCategoryToggled(item)
            }
            binding.cbCategory.setOnClickListener {
                delegate.onCategoryToggled(item)
            }

            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBottomSheetCategoryPlanBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<CategoryItem>() {
            override fun areItemsTheSame(oldItem: CategoryItem, newItem: CategoryItem) = oldItem.name == newItem.name
            override fun areContentsTheSame(oldItem: CategoryItem, newItem: CategoryItem) = oldItem == newItem
        }
    }
}