package com.umc.presentation.ui.act.study.submit

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.umc.presentation.databinding.ItemActAdminStudySubmitBinding

class AdminActStudySubmitAdapter(
    private val onClickBest: (AdminActStudySubmitItemUiModel) -> Unit,
    private val onClickReview: (AdminActStudySubmitItemUiModel) -> Unit,
) : ListAdapter<AdminActStudySubmitItemUiModel, AdminActStudySubmitAdapter.VH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemActAdminStudySubmitBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VH(binding, onClickBest, onClickReview)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    class VH(
        private val binding: ItemActAdminStudySubmitBinding,
        private val onClickBest: (AdminActStudySubmitItemUiModel) -> Unit,
        private val onClickReview: (AdminActStudySubmitItemUiModel) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AdminActStudySubmitItemUiModel) {
            binding.item = item
            binding.executePendingBindings()


            binding.root.setOnClickListener(null)

        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<AdminActStudySubmitItemUiModel>() {
            override fun areItemsTheSame(
                oldItem: AdminActStudySubmitItemUiModel,
                newItem: AdminActStudySubmitItemUiModel
            ) = oldItem.userId == newItem.userId && oldItem.weekText == newItem.weekText && oldItem.studyTitle == newItem.studyTitle

            override fun areContentsTheSame(
                oldItem: AdminActStudySubmitItemUiModel,
                newItem: AdminActStudySubmitItemUiModel
            ) = oldItem == newItem
        }
    }
}
