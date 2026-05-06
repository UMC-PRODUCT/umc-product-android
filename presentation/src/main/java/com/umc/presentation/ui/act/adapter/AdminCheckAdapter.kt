package com.umc.presentation.ui.act.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.umc.presentation.databinding.ItemAdminCheckSessionBinding
import com.umc.presentation.ui.act.check.AdminSessionUIModel

class AdminCheckAdapter(
    private val onShowPendingList: (Long) -> Unit
) : ListAdapter<AdminSessionUIModel, AdminCheckAdapter.ViewHolder>(AdminCheckDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAdminCheckSessionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemAdminCheckSessionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(uiModel: AdminSessionUIModel) {
            binding.uiModel = uiModel

            // 승인 대기 명단 확인 버튼 클릭 (바텀시트 호출)
            binding.btnAdminPendingListTrigger.setOnClickListener {
                onShowPendingList(uiModel.session.id)
            }

            binding.executePendingBindings()
        }
    }

    class AdminCheckDiffCallback : DiffUtil.ItemCallback<AdminSessionUIModel>() {
        override fun areItemsTheSame(oldItem: AdminSessionUIModel, newItem: AdminSessionUIModel) =
            oldItem.session.id == newItem.session.id

        override fun areContentsTheSame(oldItem: AdminSessionUIModel, newItem: AdminSessionUIModel) =
            oldItem == newItem
    }
}