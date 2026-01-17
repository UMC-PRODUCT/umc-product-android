package com.umc.presentation.ui.act.check

import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
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

    // 어댑터 생성 시 필요한 콜백 함수들을 전달합니다.
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
        binding.rvAdminCheckMain.adapter = adminAdapter
    }

    override fun initStates() {
        repeatOnStarted(viewLifecycleOwner) {
            launch {
                viewModel.uiState.collect { state ->
                    adminAdapter.submitList(state.adminSessions)
                }
            }
        }
    }
}