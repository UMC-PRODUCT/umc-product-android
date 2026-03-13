package com.umc.presentation.ui.act.adapter

import androidx.recyclerview.widget.RecyclerView
import com.umc.presentation.databinding.ItemDropdownBinding

class DropDownViewHolder(
    private val binding: ItemDropdownBinding,
    private val listener: DropDownAdapter.DropDownDelegate
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: String) {
        binding.apply {
            textItem.text = item
            root.setOnClickListener { listener.onClickItem(item) }
        }
    }
}