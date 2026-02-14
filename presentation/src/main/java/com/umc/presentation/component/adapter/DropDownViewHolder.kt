package com.umc.presentation.component.adapter

import androidx.recyclerview.widget.RecyclerView
import com.umc.domain.model.organization.GisuItem
import com.umc.presentation.databinding.ItemDropdownBinding

class DropDownViewHolder(
    private val binding: ItemDropdownBinding,
    private val listener: DropDownAdapter.DropDownDelegate
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: GisuItem) {
        binding.apply {
            val text = "${item.generation}기 공지사항"
            textItem.text = text
            root.setOnClickListener { listener.onClickItem(text) }
        }
    }
}