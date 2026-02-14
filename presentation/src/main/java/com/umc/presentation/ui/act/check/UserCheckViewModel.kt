package com.umc.presentation.ui.act.check

import androidx.lifecycle.viewModelScope
import com.umc.domain.model.act.check.UserCheckAvailable
import com.umc.domain.model.act.check.UserCheckHistory
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.enums.CheckAvailableStatus
import com.umc.domain.model.enums.CheckHistoryStatus
import com.umc.domain.model.request.attendance.AttendanceCheckRequest
import com.umc.domain.usecase.attendance.GetAttendanceAvailableUseCase
import com.umc.domain.usecase.attendance.GetAttendanceHistoryUseCase
import com.umc.domain.usecase.attendance.PostAttendanceCheckUseCase
import com.umc.domain.usecase.attendance.PostAttendanceReasonUseCase
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
    private val getAttendanceHistoryUseCase: GetAttendanceHistoryUseCase,
    private val getScheduleDetailUseCase: GetScheduleDetailUseCase,
    private val postAttendanceCheckUseCase: PostAttendanceCheckUseCase,
    private val postAttendanceReasonUseCase: PostAttendanceReasonUseCase
) : BaseViewModel<UserCheckUiState, UserCheckEvent>(UserCheckUiState()) {

    private var lastUserLat: Double? = null
    private var lastUserLng: Double? = null

    init {
        fetchAttendanceData()
        fetchAttendanceHistory()
    }

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
    }

    private fun fetchAttendanceHistory() {
        viewModelScope.launch {
            when (val result = getAttendanceHistoryUseCase()) {
                is ApiState.Success -> {
                    val historyUIList = result.data.mapIndexed { index, history ->
                        CheckHistoryUIModel(
                            history = history,
                            isFirst = index == 0,
                            isLast = index == result.data.size - 1
                        )
                    }
                    updateState { copy(attendanceHistories = historyUIList) }
                }
                is ApiState.Fail -> emitEvent(UserCheckEvent.ShowToast(result.failState.message))
            }
        }
    }

    private fun fetchSessionDetail(sessionId: Long) {
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
    fun requestAttendance(sheetId: Long) {
        // 현재 UI 상태에서 해당 세션의 인증 여부를 확인
        val sessionUIModel = uiState.value.availableSessions.find { it.session.id == sheetId }
        val isVerified = sessionUIModel?.isWithinRange ?: false

        // Request 객체 생성
        val request = AttendanceCheckRequest(
            attendanceSheetId = sheetId,
            latitude = lastUserLat,
            longitude = lastUserLng,
            locationVerified = isVerified
        )

        viewModelScope.launch {
            // UseCase에 객체 전달
            when (val result = postAttendanceCheckUseCase(request)) {
                is ApiState.Success -> fetchAttendanceData()
                is ApiState.Fail -> emitEvent(UserCheckEvent.ShowToast(result.failState.message))
            }
        }
    }

    /**
     * 출석 사유 제출 API 연동
     */
    fun submitAttendanceReason(sessionId: Long, reason: String) {
        viewModelScope.launch {
            when (val result = postAttendanceReasonUseCase(sessionId, reason)) {
                is ApiState.Success -> {
                    fetchAttendanceData()
                }
                is ApiState.Fail -> {}
            }
        }
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
    fun toggleSessionExpansion(sessionId: Long) {
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
    data class ShowReasonDialog(val sessionId: Long) : UserCheckEvent()
    data class NavigateToFailureReason(val sessionId: Long) : UserCheckEvent()
}