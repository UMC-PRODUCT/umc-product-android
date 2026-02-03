package com.umc.presentation.ui.act.study.group.create

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.umc.presentation.databinding.ItemActSelectedMemberBinding
import com.umc.presentation.ui.act.study.common.model.MemberUiModel

class SelectedMemberAdapter(
    private val onDelete: (MemberUiModel) -> Unit,
) : ListAdapter<MemberUiModel, SelectedMemberAdapter.VH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemActSelectedMemberBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VH(binding, onDelete)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    class VH(
        private val binding: ItemActSelectedMemberBinding,
        private val onDelete: (MemberUiModel) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MemberUiModel) {
            binding.item = item

            binding.btnDelete.setOnClickListener {
                onDelete(item)
            }

            binding.executePendingBindings()
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<MemberUiModel>() {
            override fun areItemsTheSame(oldItem: MemberUiModel, newItem: MemberUiModel): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: MemberUiModel, newItem: MemberUiModel): Boolean {
                return oldItem == newItem
            }
        }
    }
}
