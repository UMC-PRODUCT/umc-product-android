package com.umc.presentation.ui.act.check

import com.umc.domain.model.act.check.CheckHistory
import com.umc.domain.model.act.check.CheckAvailable
import com.umc.domain.model.act.check.CheckAvailableStatus
import com.umc.domain.model.act.check.CheckHistoryStatus
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
            CheckAvailable(1, "스터디", "14:00", "18:00", "스터디장", CheckAvailableStatus.BEFORE, 37.021877, 127.080960, "경기도 평택시 지제동삭1로 어쩌구저쩌구 주소를 길게 쓰기 위한 발악"),
            CheckAvailable(2, "스터디", "14:00", "18:00", "스터디장", CheckAvailableStatus.BEFORE, 37.582568, 127.001488, "서울특별시 종로구 명륜4가 88-2번지 주소를 길게 써보자ㅏㅏㅏㅏㅏㅏㅏ"),
            CheckAvailable(3, "정기 세션 3주차", "14:00", "18:00", "운영진", CheckAvailableStatus.COMPLETED, 37.5665, 126.9780,"서울특별시 중구 소공동 세종대로18길 2", true),
            CheckAvailable(4, "UMCON", "14:00", "18:00", "스터디장", CheckAvailableStatus.PENDING, 37.5665, 126.9780, "서울특별시 중구 소공동 세종대로18길 2", false)
        ).map {
            CheckAvailableUIModel(
                session = it,
                address = it.address
            )
        }

        val rawHistoryList = listOf(
            CheckHistory(1, "3주차", "정기 세션", "14:00", "18:00", CheckHistoryStatus.SUCCESS),
            CheckHistory(2, "2주차", "정기 세션", "14:00", "18:00", CheckHistoryStatus.LATE),
            CheckHistory(3, "1주차", "정기 세션", "14:00", "18:00", CheckHistoryStatus.ABSENT)
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

    fun updateLocation(userLat: Double, userLng: Double) {
        updateState {
            val updatedList = availableSessions.map { uiModel ->
                if (uiModel.session.status == CheckAvailableStatus.BEFORE) {
                    val results = FloatArray(1)
                    android.location.Location.distanceBetween(
                        userLat, userLng,
                        uiModel.session.latitude, uiModel.session.longitude,
                        results
                    )
                    uiModel.copy(isWithinRange = results[0] <= 50)
                } else {
                    uiModel.copy(isWithinRange = false)
                }
            }
            copy(availableSessions = updatedList)
        }
    }

    /**
     * 특정 아이템을 클릭했을 때 확장 상태를 토글
     */
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