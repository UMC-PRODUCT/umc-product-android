package com.umc.presentation.ui.act.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.umc.domain.model.enums.PunishCategory
import com.umc.presentation.R
import com.umc.presentation.databinding.ItemHomeCategoryBinding

interface RewardCategoryDelegate {
    fun onClickCategory(item: PunishCategory)
}


class RewardCategoryAdapter(
    private val listener: RewardCategoryDelegate
) : ListAdapter<PunishCategory, RecyclerView.ViewHolder>(
    RewardCategoryDiffCallBack()
) {
    private var selectedCategory: PunishCategory = PunishCategory.ALL

    //선택한 거 바뀌었을 때 알려주기 용도
    fun updateSelection(newSelection: PunishCategory) {
        val oldIndex = currentList.indexOf(selectedCategory)
        val newIndex = currentList.indexOf(newSelection)

        //선택된 거 바꾸기
        selectedCategory = newSelection

        //변경된 부분만 갱신
        if (oldIndex != -1) notifyItemChanged(oldIndex)
        if (newIndex != -1) notifyItemChanged(newIndex)
    }
    
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemHomeCategoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as CategoryViewHolder).bind(getItem(position))
    }

    inner class CategoryViewHolder(private val binding: ItemHomeCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PunishCategory) {
            binding.itemChip.setText(item.label)

            // 선택 여부에 따른 UI 분기
            if (item == selectedCategory) {
                bindClickedView()
            } else {
                bindUnClickedView()
            }

            binding.itemChip.setOnClickListener {
                listener.onClickCategory(item)
            }
        }

        //조나단이 만든 색깔 변경 사용
        private fun bindClickedView() {
            binding.itemChip.apply {
                setUChipBackgroundColor(ContextCompat.getColor(context, R.color.neutral800))
                setUChipBorder(ContextCompat.getColor(context, R.color.neutral800), 1)
                setUChipTextColor(ContextCompat.getColor(context, R.color.neutral100))
            }
        }
        private fun bindUnClickedView() {
            binding.itemChip.apply {
                setUChipBackgroundColor(ContextCompat.getColor(context, R.color.neutral000))
                setUChipBorder(ContextCompat.getColor(context, R.color.neutral200), 1)
                setUChipTextColor(ContextCompat.getColor(context, R.color.neutral500))
            }
        }
    }


}

class RewardCategoryDiffCallBack : DiffUtil.ItemCallback<PunishCategory>() {
    override fun areItemsTheSame(oldItem: PunishCategory, newItem: PunishCategory): Boolean = oldItem == newItem
    override fun areContentsTheSame(oldItem: PunishCategory, newItem: PunishCategory): Boolean = oldItem == newItem
}