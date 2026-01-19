package com.umc.presentation.ui.act.check

import androidx.fragment.app.viewModels
import com.umc.presentation.base.BaseFragment
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
            onToggleExpansion = { sessionId ->
                viewModel.toggleSessionExpansion(sessionId)
            },
            onChangeLocation = { sessionId ->
                // TODO: 위치 변경 다이얼로그 호출 로직 추가 예정
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
        }
    }
}