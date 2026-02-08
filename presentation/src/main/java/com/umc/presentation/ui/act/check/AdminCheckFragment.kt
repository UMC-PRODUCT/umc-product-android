package com.umc.presentation.ui.act.check

import androidx.fragment.app.viewModels
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.component.ULocationDialog
import com.umc.presentation.databinding.FragmentAdminCheckBinding
import com.umc.presentation.ui.act.adapter.AdminCheckAdapter
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
                viewModel.onLocationChangeClicked(sessionId)
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
            is AdminCheckEvent.ShowLocationDialog -> {
                showLocationChangeDialog(
                    sessionId = event.sessionId,
                    lat = event.lat,
                    lng = event.lng,
                    address = event.address
                )
            }
            is AdminCheckEvent.ShowToast -> {
                // TODO
            }
        }
    }

    /**
     * 위치 변경 다이얼로그를 띄우는 함수
     */
    private fun showLocationChangeDialog(sessionId: Int, lat: Double, lng: Double, address: String) {
        ULocationDialog(
            initialLat = lat,
            initialLng = lng,
            onLocationChanged = { newAddress, newLat, newLng ->
                viewModel.updateSessionLocation(sessionId, newLat, newLng, newAddress)
            }
        ).show(childFragmentManager, "ULocationChangeDialog")
    }
}