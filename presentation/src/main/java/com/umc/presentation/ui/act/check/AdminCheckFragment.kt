package com.umc.presentation.ui.act.check

import androidx.fragment.app.viewModels
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.databinding.FragmentAdminCheckBinding
import com.umc.presentation.ui.act.adapter.AdminCheckAdapter
import com.umc.presentation.ui.home.dialog.BottomSheetLocationDialog
import com.umc.presentation.util.UToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AdminCheckFragment : BaseFragment<FragmentAdminCheckBinding, AdminCheckUiState, AdminCheckEvent, AdminCheckViewModel>(
    FragmentAdminCheckBinding::inflate
) {
    override val viewModel: AdminCheckViewModel by viewModels()

    private val adminAdapter by lazy {
        AdminCheckAdapter(
            onChangeLocation = { sessionId ->
                showLocationChangeDialog(sessionId)
            },
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

    private fun showLocationChangeDialog(sessionId: Long) {
        val locationDialog = BottomSheetLocationDialog(
            title = "출석 위치 변경",
            description = "지도에서 새로운 출석 체크 위치를 지정해주세요. \n이 위치 반경 50m 이내에서만 출석할 수 있습니다."
        ) { selectedItem ->
            viewModel.updateSessionLocation(
                sessionId = sessionId,
                lat = selectedItem.latitude,
                lng = selectedItem.longitude,
                address = selectedItem.address
            )
        }
        locationDialog.show(childFragmentManager, "LocationSelect")
    }

    /**
     * 승인 대기 명단 바텀 시트를 띄우는 함수
     */
    private fun showPendingBottomSheet(sessionId: Long) {
        val pendingBottomSheet = AdminPendingBottomSheet(sessionId)
        pendingBottomSheet.show(childFragmentManager, "AdminPendingList")
    }
}