package com.umc.presentation.ui.act.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.umc.domain.model.act.check.AdminPendingUser
import com.umc.presentation.databinding.ItemAdminPendingUserBinding

class AdminPendingUserAdapter(
    private val onApprove: (AdminPendingUser) -> Unit,
    private val onReject: (AdminPendingUser) -> Unit,
    private val onShowLateReason: (AdminPendingUser) -> Unit
) : ListAdapter<AdminPendingUser, AdminPendingUserAdapter.ViewHolder>(AdminPendingUserDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAdminPendingUserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemAdminPendingUserBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(uiModel: AdminPendingUser) {
            binding.uiModel = uiModel

            // 승인 버튼 클릭
            binding.btnPendingApprove.setOnClickListener {
                onApprove(uiModel)
            }

            // 거절 버튼 클릭
            binding.btnPendingReject.setOnClickListener {
                onReject(uiModel)
            }

            // 경고 아이콘 클릭 (지각 사유 팝업 호출)
            binding.ivPendingWarning.setOnClickListener {
                if (uiModel.hasLateReason) {
                    onShowLateReason(uiModel)
                }
            }

            binding.executePendingBindings()
        }
    }

    class AdminPendingUserDiffCallback : DiffUtil.ItemCallback<AdminPendingUser>() {
        override fun areItemsTheSame(oldItem: AdminPendingUser, newItem: AdminPendingUser): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: AdminPendingUser, newItem: AdminPendingUser): Boolean {
            return oldItem == newItem
        }
    }
}