package com.umc.presentation.ui.act.check

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
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
        (dialog as? BottomSheetDialog)?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)?.let { bottomSheet ->
            val behavior = BottomSheetBehavior.from(bottomSheet)

            bottomSheet.layoutParams.height = (resources.displayMetrics.heightPixels * 0.8).toInt()
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true
            behavior.isHideable = true
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = LayoutBottomSheetAdminPendingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): BottomSheetDialog {
        return object : BottomSheetDialog(requireContext(), theme) {
            override fun onBackPressed() {
                val isSelectionMode = viewModel.uiState.value.isSelectionMode

                if (isSelectionMode) {
                    // 선택 모드면 취소하고 다이얼로그는 유지
                    viewModel.toggleSelectionMode(false)
                } else {
                    // 선택 모드가 아니면 다이얼로그 닫기
                    super.onBackPressed()
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvPendingList.apply {
            adapter = pendingAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        binding.btnSelectMode.setOnClickListener {
            viewModel.toggleSelectionMode(true)
        }

        binding.btnConfirm.setOnClickListener {
            // 선택된 인원이 있을 때만 서버 통신 실행
            if (viewModel.uiState.value.selectedIds.isNotEmpty()) {
                viewModel.approveSelected(sessionId)
            }
        }

        observeState()
        viewModel.fetchPendingUsers(sessionId)
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                pendingAdapter.submitList(state.pendingUsers)
                pendingAdapter.isSelectionMode = state.isSelectionMode

                binding.isSelectionMode = state.isSelectionMode
                binding.isAnySelected = state.selectedIds.isNotEmpty()

                dialog?.setCanceledOnTouchOutside(!state.isSelectionMode)

                binding.executePendingBindings()
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        viewModel.toggleSelectionMode(false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}