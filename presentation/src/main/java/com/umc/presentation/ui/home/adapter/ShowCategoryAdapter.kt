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


/**해당 어댑터는 카테고리 (UChip) 형태에 대한 어댑터입니다.
 * 게시글 카테고리의 경우 종류가 많으므로, CategoryType의 enums를 생성하고 label로 처리하러 했으나
 * 중아 건의함의 카테고리의 경우, 지역이기 때문에, String으로 통합
 *
 * TODO: 해당 어댑터는 일정 추가 탭의 BottomSheetdDialog 사용으로 인해 사용이 중지되었습니다.
 * **/
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
                    setUChipBackgroundColor(ContextCompat.getColor(context, R.color.neutral800))
                    setUChipBorder(ContextCompat.getColor(context, R.color.neutral800), 1)
                    setUChipTextColor(ContextCompat.getColor(context, R.color.neutral100))
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