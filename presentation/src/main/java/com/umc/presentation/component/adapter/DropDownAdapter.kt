package com.umc.presentation.component.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.umc.presentation.databinding.ItemDropdownBinding

class DropDownAdapter(
    private val listener: DropDownDelegate
) : ListAdapter<String, RecyclerView.ViewHolder> (
    DropDownDiffCallBack()
) {

    interface DropDownDelegate {
        fun onClickItem(text: String)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder) {
            is DropDownViewHolder -> holder.bind(currentList[position])
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemDropdownBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DropDownViewHolder(binding, listener)
    }
}

class DropDownDiffCallBack : DiffUtil.ItemCallback<String>() {
    override fun areContentsTheSame(
        oldItem: String,
        newItem: String
    ): Boolean {
        return oldItem == newItem
    }

    override fun areItemsTheSame(
        oldItem: String,
        newItem: String
    ): Boolean {
        return oldItem == newItem
    }
}