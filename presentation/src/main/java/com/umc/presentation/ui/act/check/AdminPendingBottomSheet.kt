package com.umc.presentation.ui.act.check

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.umc.presentation.ui.act.adapter.AdminPendingUserAdapter
import com.umc.presentation.databinding.LayoutBottomSheetAdminPendingBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AdminPendingBottomSheet(private val sessionId: Long) : BottomSheetDialogFragment() {
    private var _binding: LayoutBottomSheetAdminPendingBinding? = null
    private val binding get() = _binding!!

    // ViewModel 주입
    private val viewModel: AdminPendingViewModel by viewModels()

    private val pendingAdapter by lazy {
        AdminPendingUserAdapter(
            fragmentManager = childFragmentManager,
            onApproveConfirmed = { user -> viewModel.approveUsers(sessionId, listOf(user.id)) },
            onRejectConfirmed = { user -> viewModel.rejectUsers(sessionId, listOf(user.id)) },
            onToggleSelection = { user -> viewModel.toggleSelection(user.id) },
            isSelected = { id -> viewModel.uiState.value.selectedIds.contains(id) }
        )
    }

    override fun onStart() {
        super.onStart()
        // BottomSheet 높이 및 상태 설정
        (dialog as? BottomSheetDialog)?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)?.let {
            val behavior = BottomSheetBehavior.from(it)
            it.layoutParams.height = (resources.displayMetrics.heightPixels * 0.8).toInt()
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LayoutBottomSheetAdminPendingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvPendingList.adapter = pendingAdapter

        // 선택 승인하기 모드 진입
        binding.btnSelectMode.setOnClickListener {
            viewModel.toggleSelectionMode(true)
            pendingAdapter.isSelectionMode = true
        }

        // 선택된 유저 최종 확인 승인
        binding.btnConfirm.setOnClickListener {
            viewModel.approveSelected(sessionId)
            pendingAdapter.isSelectionMode = false
        }

        observeState()
        viewModel.fetchPendingUsers(sessionId)
    }

    private fun observeState() {
        // UI 상태 구독
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                pendingAdapter.submitList(state.pendingUsers)
                binding.isSelectionMode = state.isSelectionMode
                binding.isAnySelected = state.selectedIds.isNotEmpty()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}