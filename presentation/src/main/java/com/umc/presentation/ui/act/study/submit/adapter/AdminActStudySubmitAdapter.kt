package com.umc.presentation.ui.act.study.submit.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.umc.presentation.databinding.ItemActAdminStudySubmitBinding
import com.umc.presentation.ui.act.study.submit.model.AdminActStudySubmitItemUiModel

class AdminActStudySubmitAdapter(
    private val onClickBest: (AdminActStudySubmitItemUiModel) -> Unit,
    private val onClickReview: (AdminActStudySubmitItemUiModel) -> Unit,
) : ListAdapter<AdminActStudySubmitItemUiModel, AdminActStudySubmitAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemActAdminStudySubmitBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding, onClickBest, onClickReview)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
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
