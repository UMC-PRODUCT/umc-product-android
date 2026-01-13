package com.umc.presentation.ui.act.check

import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.umc.presentation.R
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.databinding.FragmentUserCheckBinding
import com.umc.presentation.ui.act.adapter.CheckAvailableAdapter
import com.umc.presentation.ui.act.adapter.CheckHistoryAdapter
import com.umc.presentation.ui.act.adapter.SectionHeaderAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UserCheckFragment : BaseFragment<FragmentUserCheckBinding, UserCheckUiState, UserCheckEvent, UserCheckViewModel>(
    FragmentUserCheckBinding::inflate
) {
    override val viewModel: UserCheckViewModel by viewModels()

    private val availableHeaderAdapter by lazy { SectionHeaderAdapter(getString(R.string.attendance_header_available)) }
    private val availableAdapter by lazy {
        CheckAvailableAdapter { sessionId -> viewModel.toggleSessionExpansion(sessionId) }
    }
    private val historyHeaderAdapter by lazy { SectionHeaderAdapter(getString(R.string.attendance_header_history)) }
    private val historyAdapter by lazy { CheckHistoryAdapter() }

    override fun initView() {
        setupMainRecyclerView()
    }

    private fun setupMainRecyclerView() {
        val concatAdapter = ConcatAdapter(
            availableHeaderAdapter,
            availableAdapter,
            historyHeaderAdapter,
            historyAdapter
        )

        binding.rvUserCheckMain.apply {
            adapter = concatAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    override fun initStates() {
        // HomeFragment의 패턴을 따라 상태 수집
        repeatOnStarted(viewLifecycleOwner) {
            launch {
                viewModel.uiState.collect { state ->
                    // 뷰모델의 상태에 따라 어댑터 데이터 갱신
                    availableAdapter.submitList(state.availableSessions)
                    availableHeaderAdapter.updateCount(state.availableCount)
                    historyAdapter.submitList(state.attendanceHistories)
                }
            }

            launch {
                viewModel.uiEvent.collect { event ->
                    handleEvent(event)
                }
            }
        }
    }

    private fun handleEvent(event: UserCheckEvent) {
        when (event) {
            is UserCheckEvent.ShowToast -> { /* 토스트 구현 */ }
            is UserCheckEvent.NavigateToFailureReason -> { /* 이동 구현 */ }
        }
    }

    override fun onDestroyView() {
        // 메모리 누수 방지
        binding.rvUserCheckMain.adapter = null
        super.onDestroyView()
    }
}