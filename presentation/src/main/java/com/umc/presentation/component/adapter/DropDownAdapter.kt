package com.umc.presentation.component.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.umc.domain.model.organization.GisuItem
import com.umc.presentation.databinding.ItemDropdownBinding

class DropDownAdapter(
    private val listener: DropDownDelegate
) : ListAdapter<GisuItem, RecyclerView.ViewHolder> (
    DropDownDiffCallBack()
) {

    interface DropDownDelegate {
        fun onClickItem(text: String, gisu: Long)
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

class DropDownDiffCallBack : DiffUtil.ItemCallback<GisuItem>() {
    override fun areContentsTheSame(
        oldItem: GisuItem,
        newItem: GisuItem
    ): Boolean {
        return oldItem == newItem
    }

    override fun areItemsTheSame(
        oldItem: GisuItem,
        newItem: GisuItem
    ): Boolean {
        return oldItem.gisuId == newItem.gisuId
    }
}