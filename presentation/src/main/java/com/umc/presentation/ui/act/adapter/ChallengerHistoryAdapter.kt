package com.umc.presentation.ui.act.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.umc.domain.model.act.challenger.ChallengerPoint
import com.umc.presentation.databinding.ItemChallengerHistoryBinding
import com.umc.presentation.extension.px

class ChallengerHistoryAdapter(
    private val onDeleteClick: (ChallengerPoint) -> Unit
) : ListAdapter<ChallengerPoint, ChallengerHistoryAdapter.ViewHolder>(DiffCallback()) {

    private var isEditMode: Boolean = false

    fun setEditMode(isEdit: Boolean) {
        this.isEditMode = isEdit
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemChallengerHistoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val layoutParams = holder.itemView.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.topMargin = if (position == 0) 0 else 12.px
        holder.itemView.layoutParams = layoutParams

        holder.bind(getItem(position), isEditMode)
    }

    inner class ViewHolder(private val binding: ItemChallengerHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ChallengerPoint, isEditMode: Boolean) {
            binding.item = item
            binding.isEditMode = isEditMode

            // 삭제 버튼 리스너 연결
            binding.btnRemove.setOnClickListener { onDeleteClick(item) }

            binding.executePendingBindings()
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ChallengerPoint>() {
        override fun areItemsTheSame(oldItem: ChallengerPoint, newItem: ChallengerPoint): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: ChallengerPoint, newItem: ChallengerPoint): Boolean =
            oldItem == newItem
    }
}