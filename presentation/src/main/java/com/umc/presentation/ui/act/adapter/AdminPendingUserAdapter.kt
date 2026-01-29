package com.umc.presentation.ui.act.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.umc.domain.model.act.check.AdminPendingUser
import com.umc.presentation.component.UBasicDialog
import com.umc.presentation.component.UBasicDialogModel
import com.umc.presentation.component.UCheckDialog
import com.umc.presentation.component.UCheckDialogModel
import com.umc.presentation.databinding.ItemAdminPendingUserBinding

class AdminPendingUserAdapter(
    private val fragmentManager: FragmentManager,
    private val onApproveConfirmed: (AdminPendingUser) -> Unit,
    private val onRejectConfirmed: (AdminPendingUser) -> Unit
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

            // 승인 버튼 클릭 - 승인 다이얼로그 표시
            binding.btnPendingApprove.setOnClickListener {
                showApprovalDialog(uiModel)
            }

            // 거절 버튼 클릭 - 반려 다이얼로그 표시
            binding.btnPendingReject.setOnClickListener {
                showRejectionDialog(uiModel)
            }

            // 경고 아이콘 클릭 - 지각 사유 다이얼로그 표시
            binding.btnPendingWarning.setOnClickListener {
                if (uiModel.hasLateReason) {
                    showLateReasonDialog(uiModel)
                }
            }

            binding.executePendingBindings()
        }

        private fun showApprovalDialog(user: AdminPendingUser) {// After
            val approveModel = UBasicDialogModel.Success(
                title = "출석 요청을 승인하시겠습니까?",
                content = "${user.name} | 요청 시간: ${user.requestTime}",
                positiveText = "승인하기"
            )

            UBasicDialog(
                model = approveModel,
                onConfirm = {
                    onApproveConfirmed(user)
                }
            ).show(fragmentManager, "ApproveDialog")
        }

        private fun showRejectionDialog(user: AdminPendingUser) {
            val rejectModel = UBasicDialogModel.Cancel(
                title = "출석 요청을 반려하시겠습니까?",
                content = "${user.name} | 요청 시간: ${user.requestTime}",
                positiveText = "반려하기"
            )

            UBasicDialog(
                model = rejectModel,
                onConfirm = {
                    onRejectConfirmed(user)
                }
            ).show(fragmentManager, "RejectDialog")
        }

        private fun showLateReasonDialog(user: AdminPendingUser) {
            if (user.lateReason.isNullOrBlank()) return

            val checkModel = UCheckDialogModel(
                title = "출석 사유 확인",
                subtitle = "${user.name}님이 작성하신 사유입니다.",
                positiveText = "확인",
                isWriteMode = false,
                reason = user.lateReason
            )

            UCheckDialog(
                model = checkModel,
                onConfirm = { /* 확인 버튼 클릭 시 별도 로직 없음 */ }
            ).show(fragmentManager, "LateReasonDialog")
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