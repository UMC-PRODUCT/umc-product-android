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
import com.umc.presentation.ui.act.adapter.EmptyStateAdapter // [수정] import 확인
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UserCheckFragment : BaseFragment<FragmentUserCheckBinding, UserCheckUiState, UserCheckEvent, UserCheckViewModel>(
    FragmentUserCheckBinding::inflate
) {
    override val viewModel: UserCheckViewModel by viewModels()

    // 1. 출석 가능 섹션 관련 어댑터
    private val availableHeaderAdapter by lazy { SectionHeaderAdapter(getString(R.string.attendance_header_available)) }
    private val availableAdapter by lazy {
        CheckAvailableAdapter { sessionId -> viewModel.toggleSessionExpansion(sessionId) }
    }
    private val availableEmptyAdapter by lazy { EmptyStateAdapter() }

    // 2. 나의 출석 현황 섹션 관련 어댑터
    private val historyHeaderAdapter by lazy { SectionHeaderAdapter(getString(R.string.attendance_header_history)) }
    private val historyAdapter by lazy { CheckHistoryAdapter() }
    private val historyEmptyAdapter by lazy { EmptyStateAdapter() }

    override fun initView() {
        setupMainRecyclerView()
    }

    /**
     * ConcatAdapter를 사용하여 헤더-리스트-빈화면 순서로 결합
     */
    private fun setupMainRecyclerView() {
        val concatAdapter = ConcatAdapter(
            availableHeaderAdapter,
            availableAdapter,
            availableEmptyAdapter,   // 출석 가능 리스트 하단
            historyHeaderAdapter,
            historyAdapter,
            historyEmptyAdapter      // 히스토리 리스트 하단
        )

        binding.rvUserCheckMain.apply {
            adapter = concatAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    override fun initStates() {
        repeatOnStarted(viewLifecycleOwner) {
            launch {
                viewModel.uiState.collect { state ->
                    availableAdapter.submitList(state.availableSessions)
                    availableHeaderAdapter.updateCount(state.availableCount)

                    if (state.availableSessions.isEmpty()) {
                        availableEmptyAdapter.submitList(listOf(EmptyStateUIModel(
                            R.drawable.ic_people,
                            getString(R.string.attendance_empty_available)
                        )))
                    } else {
                        availableEmptyAdapter.submitList(emptyList<EmptyStateUIModel>())
                    }

                    historyAdapter.submitList(state.attendanceHistories)

                    if (state.attendanceHistories.isEmpty()) {
                        historyEmptyAdapter.submitList(listOf(EmptyStateUIModel(
                            R.drawable.ic_document,
                            getString(R.string.attendance_empty_history)
                        )))
                    } else {
                        historyEmptyAdapter.submitList(emptyList<EmptyStateUIModel>())
                    }
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
        binding.rvUserCheckMain.adapter = null
        super.onDestroyView()
    }
}