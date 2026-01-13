package com.umc.presentation.ui.act.check

import com.umc.domain.model.act.check.CheckHistory
import com.umc.domain.model.act.check.AttendanceStatus
import com.umc.domain.model.act.check.CheckAvailable
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class UserCheckViewModel @Inject constructor() :
    BaseViewModel<UserCheckUiState, UserCheckEvent>(UserCheckUiState()) {

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        val availableList = listOf(
            CheckAvailable(1, "스터디", "14:00", "18:00", AttendanceStatus.BEFORE),
            CheckAvailable(2, "정기 세션 3주차", "14:00", "18:00", AttendanceStatus.BEFORE)
        ).map { CheckAvailableUIModel(it) }

        val rawHistoryList = listOf(
            CheckHistory(1, "3주차", "정기 세션", "14:00", "18:00", AttendanceStatus.SUCCESS),
            CheckHistory(2, "2주차", "정기 세션", "14:00", "18:00", AttendanceStatus.LATE),
            CheckHistory(3, "1주차", "정기 세션", "14:00", "18:00", AttendanceStatus.ABSENT)
        )

        val historyUIList = rawHistoryList.mapIndexed { index, history ->
            CheckHistoryUIModel(
                history = history,
                isFirst = index == 0,
                isLast = index == rawHistoryList.size - 1
            )
        }

        updateState {
            copy(
                availableSessions = availableList,
                attendanceHistories = historyUIList,
                availableCount = availableList.size
            )
        }
    }

    fun toggleSessionExpansion(sessionId: Int) {
        updateState {
            val newList = availableSessions.map { uiModel ->
                if (uiModel.session.id == sessionId) {
                    uiModel.copy(isExpanded = !uiModel.isExpanded)
                } else {
                    uiModel
                }
            }
            copy(availableSessions = newList)
        }
    }
}

data class UserCheckUiState(
    val availableSessions: List<CheckAvailableUIModel> = emptyList(),
    val attendanceHistories: List<CheckHistoryUIModel> = emptyList(),
    val availableCount: Int = 0
) : UiState

sealed class UserCheckEvent : UiEvent {
    data class ShowToast(val message: String) : UserCheckEvent()
    data class NavigateToFailureReason(val sessionId: Int) : UserCheckEvent()
}