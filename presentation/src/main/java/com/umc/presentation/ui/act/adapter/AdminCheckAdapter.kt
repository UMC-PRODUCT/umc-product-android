package com.umc.presentation.ui.act.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.umc.presentation.databinding.ItemAdminCheckSessionBinding
import com.umc.presentation.ui.act.check.AdminSessionUIModel

class AdminCheckAdapter(
    private val onToggleExpansion: (Int) -> Unit,
    private val onChangeLocation: (Int) -> Unit
) : ListAdapter<AdminSessionUIModel, AdminCheckAdapter.ViewHolder>(AdminCheckDiffCallback()) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            // 페이로드가 없으면 전체 바인딩 수행
            super.onBindViewHolder(holder, position, payloads)
        } else {
            // 페이로드가 있으면 데이터만 갱신
            holder.bind(getItem(position))
        }
    }

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

        private val pendingUserAdapter = AdminPendingUserAdapter(
            onApprove = { /* TODO: 승인 */ },
            onReject = { /* TODO: 거절 */ },
            onShowLateReason = { /* TODO: 사유 확인 */ }
        )

        init {
            binding.rvAdminPendingUsers.adapter = pendingUserAdapter
        }

        fun bind(uiModel: AdminSessionUIModel) {
            binding.uiModel = uiModel

            // 확장 상태일 때만 하위 리스트 업데이트
            if (uiModel.isExpanded) {
                pendingUserAdapter.submitList(uiModel.session.pendingUsers)
            }

            binding.btnAdminChangeLocation.setOnClickListener {
                onChangeLocation(uiModel.session.id)
            }

            binding.btnAdminPendingListTrigger.setOnClickListener {
                onToggleExpansion(uiModel.session.id)
            }

            binding.executePendingBindings()
        }
    }

    class AdminCheckDiffCallback : DiffUtil.ItemCallback<AdminSessionUIModel>() {
        override fun areItemsTheSame(oldItem: AdminSessionUIModel, newItem: AdminSessionUIModel) =
            oldItem.session.id == newItem.session.id

        override fun areContentsTheSame(oldItem: AdminSessionUIModel, newItem: AdminSessionUIModel) =
            oldItem == newItem

        override fun getChangePayload(oldItem: AdminSessionUIModel, newItem: AdminSessionUIModel): Any? {
            return if (oldItem.isExpanded != newItem.isExpanded) {
                PAYLOAD_EXPAND
            } else {
                super.getChangePayload(oldItem, newItem)
            }
        }

        companion object {
            private const val PAYLOAD_EXPAND = "PAYLOAD_EXPAND"
        }
    }
}