package com.umc.presentation.component.adapter

import androidx.recyclerview.widget.RecyclerView
import com.umc.domain.model.notice.DropDownItem
import com.umc.presentation.databinding.ItemDropdownBinding

class DropDownViewHolder<T : DropDownItem>(
    private val binding: ItemDropdownBinding,
    private val listener: DropDownAdapter.DropDownDelegate<T>
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: T) {
        binding.textItem.text = item.displayText
        binding.root.setOnClickListener { listener.onClickItem(item) }
    }
}