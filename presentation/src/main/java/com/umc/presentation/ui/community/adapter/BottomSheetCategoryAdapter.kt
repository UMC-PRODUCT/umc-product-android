package com.umc.presentation.ui.community.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.umc.domain.model.enums.CommunityCategoryType
import com.umc.presentation.databinding.ItemBottomSheetCategoryBinding

class BottomSheetCategoryAdapter(
    private val onClick: (CommunityCategoryType) -> Unit
) : ListAdapter<CommunityCategoryType, BottomSheetCategoryAdapter.ViewHolder>(CategoryDiffCallback) {

    inner class ViewHolder(private val binding: ItemBottomSheetCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(category: CommunityCategoryType) {
            // XML의 <variable name="category">에 데이터 주입
            binding.itemTvCategory.text = category.label

            // 클릭 이벤트 처리
            binding.root.setOnClickListener {
                onClick(category)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBottomSheetCategoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    object CategoryDiffCallback : DiffUtil.ItemCallback<CommunityCategoryType>() {
        override fun areItemsTheSame(oldItem: CommunityCategoryType, newItem: CommunityCategoryType): Boolean {
            // Enum은 고유한 상수이므로 직접 비교가 가능합니다.
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: CommunityCategoryType, newItem: CommunityCategoryType): Boolean {
            return oldItem == newItem
        }
    }
}