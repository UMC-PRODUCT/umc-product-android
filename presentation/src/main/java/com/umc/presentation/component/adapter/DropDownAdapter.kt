package com.umc.presentation.component.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.umc.presentation.databinding.ItemDropdownBinding

interface DropDownItem {
    val displayText: String
}

class DropDownAdapter<T : DropDownItem>(
    private val listener: DropDownDelegate<T>
) : ListAdapter<T, DropDownViewHolder<T>>(
    DropDownDiffCallBack<T>()
) {

    interface DropDownDelegate<T> {
        fun onClickItem(item: T)
    }

    override fun onBindViewHolder(holder: DropDownViewHolder<T>, position: Int) {
        holder.bind(currentList[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DropDownViewHolder<T> {
        val binding = ItemDropdownBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DropDownViewHolder(binding, listener)
    }
}

class DropDownDiffCallBack<T : DropDownItem> : DiffUtil.ItemCallback<T>() {
    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem.displayText == newItem.displayText
    }

    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem.displayText == newItem.displayText
    }
}