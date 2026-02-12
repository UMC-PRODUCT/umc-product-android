package com.umc.presentation.ui.act.check

import androidx.fragment.app.viewModels
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.component.ULocationDialog
import com.umc.presentation.databinding.FragmentAdminCheckBinding
import com.umc.presentation.ui.act.adapter.AdminCheckAdapter
import com.umc.presentation.ui.home.dialog.BottomSheetLocationDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AdminCheckFragment : BaseFragment<FragmentAdminCheckBinding, AdminCheckUiState, AdminCheckEvent, AdminCheckViewModel>(
    FragmentAdminCheckBinding::inflate
) {
    override val viewModel: AdminCheckViewModel by viewModels()

    private val adminAdapter by lazy {
        AdminCheckAdapter(
            fragmentManager = childFragmentManager,
            onToggleExpansion = { sessionId ->
                viewModel.toggleSessionExpansion(sessionId)
            },
            onChangeLocation = { sessionId ->
                showLocationChangeDialog(sessionId)
            },
            onApproveConfirmed = { user, _ ->
                viewModel.approveAttendance(user.id)
            },
            onRejectConfirmed = { user, _ ->
                viewModel.rejectAttendance(user.id)
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
                viewModel.uiState.collect { state ->
                    adminAdapter.submitList(state.adminSessions)
                    binding.uiState = state
                }
            }

            launch {
                viewModel.uiEvent.collect { event ->
                    handleEvent(event)
                }
            }
        }
    }

    override fun handleEvent(event: AdminCheckEvent) {
        when (event) {
            is AdminCheckEvent.ShowToast -> { /* 토스트 출력 로직 */ }
        }
    }

    /**
     * 위치 변경 바텀 시트를 띄우는 함수
     */
    private fun showLocationChangeDialog(sessionId: Long) {
        val locationDialog = BottomSheetLocationDialog { selectedItem ->
            viewModel.updateSessionLocation(
                sessionId = sessionId,
                lat = selectedItem.latitude,
                lng = selectedItem.longitude,
                address = selectedItem.address
            )
        }
        locationDialog.show(childFragmentManager, "LocationSelect")
    }
}