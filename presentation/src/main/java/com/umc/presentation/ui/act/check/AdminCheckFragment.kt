package com.umc.presentation.ui.act.check

import androidx.fragment.app.viewModels
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.component.ULoadingDialog
import com.umc.presentation.component.dismissLoading
import com.umc.presentation.component.showLoadingDialog
import com.umc.presentation.databinding.FragmentAdminCheckBinding
import com.umc.presentation.ui.act.adapter.AdminCheckAdapter
import com.umc.presentation.util.UToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AdminCheckFragment : BaseFragment<FragmentAdminCheckBinding, AdminCheckUiState, AdminCheckEvent, AdminCheckViewModel>(
    FragmentAdminCheckBinding::inflate
) {
    override val viewModel: AdminCheckViewModel by viewModels()
    private var loadingDialog: ULoadingDialog? = null

    private val adminAdapter by lazy {
        AdminCheckAdapter(
            onShowPendingList = { sessionId ->
                showPendingBottomSheet(sessionId)
            }
        )
    }

    override fun initView() {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.rvAdminCheckMain.adapter = adminAdapter
    }

    override fun initStates() {
        repeatOnStarted(viewLifecycleOwner) {
            launch {
                viewModel.isLoading.collect { isLoading ->
                    if (isLoading) {
                        if (loadingDialog == null) {
                            loadingDialog = childFragmentManager.showLoadingDialog()
                        }
                    } else {
                        loadingDialog?.dismissLoading()
                        loadingDialog = null
                    }
                }
            }

            launch {
                viewModel.uiState.collect { state ->
                    adminAdapter.submitList(state.adminSessions)
                    binding.uiState = state
                }
            }

            launch {
                viewModel.uiEvent.collect { event -> handleEvent(event) }
            }
        }
    }

    override fun onDestroyView() {
        loadingDialog?.dismissLoading()
        loadingDialog = null
        super.onDestroyView()
    }

    override fun handleEvent(event: AdminCheckEvent) {
        when (event) {
            is AdminCheckEvent.ShowToast -> {
                val toastState = if (event.isError) UToast.State.ERROR else UToast.State.CHECK

                UToast.createToast(
                    context = requireContext(),
                    message = event.message,
                    state = toastState
                ).show()
            }
        }
    }

    /**
     * 승인 대기 명단 바텀 시트를 띄우는 함수
     */
    private fun showPendingBottomSheet(sessionId: Long) {
        val pendingBottomSheet = AdminPendingBottomSheet(sessionId)
        pendingBottomSheet.show(childFragmentManager, "AdminPendingList")
    }
}