package com.umc.presentation.ui.act.check

import androidx.lifecycle.viewModelScope
import com.umc.domain.model.act.check.UserCheckAvailable
import com.umc.domain.model.act.check.UserCheckHistory
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.enums.CheckAvailableStatus
import com.umc.domain.model.enums.CheckHistoryStatus
import com.umc.domain.usecase.attendance.GetAttendanceAvailableUseCase
import com.umc.domain.usecase.attendance.PostAttendanceCheckUseCase
import com.umc.domain.usecase.schedule.GetScheduleDetailUseCase
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserCheckViewModel @Inject constructor(
    private val getAttendanceAvailableUseCase: GetAttendanceAvailableUseCase,
    private val getScheduleDetailUseCase: GetScheduleDetailUseCase,
    private val postAttendanceCheckUseCase: PostAttendanceCheckUseCase
) : BaseViewModel<UserCheckUiState, UserCheckEvent>(UserCheckUiState()) {

    private var lastUserLat: Double = 0.0
    private var lastUserLng: Double = 0.0

    init { fetchAttendanceData() }

    private fun fetchAttendanceData() {
        viewModelScope.launch {
            when (val result = getAttendanceAvailableUseCase()) {
                is ApiState.Success -> {
                    val list = result.data.map { CheckAvailableUIModel(session = it) }
                    updateState { copy(availableSessions = list, availableCount = list.size) }

                    // 목록 로드 즉시 각 아이템의 상세(위치 정보) 요청
                    list.forEach { fetchSessionDetail(it.session.id) }
                }
                is ApiState.Fail -> emitEvent(UserCheckEvent.ShowToast(result.failState.message))
            }
        }
        loadHistoryDummyData()
    }

    private fun fetchSessionDetail(sessionId: Int) {
        viewModelScope.launch {
            when (val result = getScheduleDetailUseCase(sessionId)) {
                is ApiState.Success -> {
                    val detail = result.data
                    updateState {
                        val updatedList = availableSessions.map { uiModel ->
                            if (uiModel.session.id == sessionId) {
                                uiModel.copy(
                                    address = detail.address,
                                    session = uiModel.session.copy(
                                        latitude = detail.latitude,
                                        longitude = detail.longitude,
                                        address = detail.address
                                    )
                                )
                            } else uiModel
                        }
                        copy(availableSessions = updatedList)
                    }
                }
                is ApiState.Fail -> emitEvent(UserCheckEvent.ShowToast(result.failState.message))
            }
        }
    }

    /**
     * 실시간 위치 기반 출석 요청
     */
    fun requestAttendance(sheetId: Int) {
        viewModelScope.launch {
            when (val result = postAttendanceCheckUseCase(sheetId)) {
                is ApiState.Success -> {
                    fetchAttendanceData()
                }
                is ApiState.Fail -> {
                    emitEvent(UserCheckEvent.ShowToast(result.failState.message))
                }
            }
        }
    }

    fun submitAttendanceReason(sessionId: Int, reason: String) {
        // TODO: 출석 사유 제출 API 연동
        emitEvent(UserCheckEvent.ShowToast("사유가 성공적으로 제출되었습니다."))
    }

    private fun loadHistoryDummyData() {
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

        updateState { copy(attendanceHistories = historyUIList) }
    }

    /**
     * 기존 위치 업데이트 및 거리 계산 로직 유지
     */
    fun updateLocation(userLat: Double, userLng: Double) {
        lastUserLat = userLat
        lastUserLng = userLng

        updateState {
            val updatedList = availableSessions.map { uiModel ->
                // 좌표가 0.0이 아니고 유효한 경우에만 계산
                if (uiModel.session.latitude != 0.0 && uiModel.session.longitude != 0.0) {
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
     * 특정 아이템을 클릭했을 때 확장 상태를 토글 (기존 로직 유지)
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