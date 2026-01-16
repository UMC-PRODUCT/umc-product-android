package com.umc.presentation.ui.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.umc.domain.model.home.CategoryItem
import com.umc.presentation.R
import com.umc.presentation.databinding.ItemHomeCategoryBinding

class ShowCategoryAdapter(
    private val onCategoryClick: (CategoryItem) -> Unit,
) : ListAdapter<CategoryItem, ShowCategoryAdapter.ViewHolder>(CategoryDiffCallback) {

    inner class ViewHolder(private val binding: ItemHomeCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CategoryItem) {
            binding.itemChip.apply {
                setText(item.name)

                // 상태에 따른 스타일 분기 처리
                if (item.isChecked) {
                    // 선택됨
                    setUChipBackgroundColor(ContextCompat.getColor(context, R.color.primary500))
                    setUChipBorder(ContextCompat.getColor(context, R.color.primary500), 1)
                    setUChipTextColor(ContextCompat.getColor(context, R.color.neutral000))
                } else {
                    // 해제됨
                    setUChipBackgroundColor(ContextCompat.getColor(context, R.color.neutral000))
                    setUChipBorder(ContextCompat.getColor(context, R.color.neutral200), 1)
                    setUChipTextColor(ContextCompat.getColor(context, R.color.neutral500))
                }

                // 클릭 시 ViewModel로 알림
                setOnClickListener { onCategoryClick(item) }
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHomeCategoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }



    companion object CategoryDiffCallback : DiffUtil.ItemCallback<CategoryItem>() {
        override fun areItemsTheSame(oldItem: CategoryItem, newItem: CategoryItem) = oldItem.name == newItem.name
        override fun areContentsTheSame(oldItem: CategoryItem, newItem: CategoryItem) = oldItem == newItem
    }


}