package com.umc.presentation.ui.act.check

import com.umc.domain.model.act.check.UserCheckHistory
import com.umc.domain.model.act.check.UserCheckAvailable
import com.umc.domain.model.enums.CheckAvailableStatus
import com.umc.domain.model.enums.CheckHistoryStatus
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

    fun submitAttendanceReason(sessionId: Int, reason: String) {
        // TODO: API 전송 로직 구현
        emitEvent(UserCheckEvent.ShowToast("사유가 성공적으로 제출되었습니다."))
    }

    private fun loadInitialData() {
        val availableList = listOf(
            UserCheckAvailable(1, "스터디", "14:00", "18:00", CheckAvailableStatus.BEFORE, 37.504562, 126.956926, "서울특별시 동작구 흑석로 84"),
            UserCheckAvailable(2, "스터디", "14:00", "18:00", CheckAvailableStatus.BEFORE, 37.582568, 127.001488, "서울특별시 종로구 명륜4가 88-2번지 주소를 길게 써보자ㅏㅏㅏㅏㅏㅏㅏ"),
            UserCheckAvailable(3, "정기 세션 3주차", "14:00", "18:00", CheckAvailableStatus.COMPLETED, 37.5665, 126.9780,"서울특별시 중구 소공동 세종대로18길 2", true),
            UserCheckAvailable(4, "UMCON", "14:00", "18:00", CheckAvailableStatus.PENDING, 37.5665, 126.9780, "서울특별시 중구 소공동 세종대로18길 2", false)
        ).map {
            CheckAvailableUIModel(
                session = it,
                address = it.address
            )
        }

        val rawHistoryList = listOf(
            UserCheckHistory(1, "3주차", "정기 세션", "14:00", "18:00", CheckHistoryStatus.SUCCESS),
            UserCheckHistory(2, "2주차", "정기 세션", "14:00", "18:00", CheckHistoryStatus.LATE),
            UserCheckHistory(3, "1주차", "정기 세션", "14:00", "18:00", CheckHistoryStatus.ABSENT)
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
                    uiModel.copy(isWithinRange = results[0] <= geofenceRadius)
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
                    uiModel.copy(isExpanded = false)
                }
            }
            copy(availableSessions = newList)
        }
    }
}

data class UserCheckUiState(
    val availableSessions: List<CheckAvailableUIModel> = emptyList(),
    val attendanceHistories: List<CheckHistoryUIModel> = emptyList(),
    val availableCount: Int = 0,
    val geofenceRadius: Float = 50f
) : UiState
sealed class UserCheckEvent : UiEvent {
    data class ShowToast(val message: String) : UserCheckEvent()
    data class ShowReasonDialog(val sessionId: Int) : UserCheckEvent()
    data class NavigateToFailureReason(val sessionId: Int) : UserCheckEvent()
}