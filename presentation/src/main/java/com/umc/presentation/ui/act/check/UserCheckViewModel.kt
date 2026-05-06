package com.umc.presentation.ui.act.check

import androidx.lifecycle.viewModelScope
import com.umc.domain.usecase.attendance.GetAttendanceAvailableUseCase
import com.umc.domain.usecase.attendance.GetAttendanceHistoryUseCase
import com.umc.domain.usecase.attendance.PostAttendanceReasonUseCase
import com.umc.domain.usecase.schedule.GetScheduleDetailUseCase
import com.umc.domain.usecase.schedule.PostScheduleAttendanceUseCase
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
    private val postScheduleAttendanceUseCase: PostScheduleAttendanceUseCase,
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
            resultResponse(
                response = getAttendanceAvailableUseCase(),
                successCallback = { data ->
                    val list = data.map { CheckAvailableUIModel(session = it) }
                    updateState { copy(availableSessions = list, availableCount = list.size) }
                    list.forEach { fetchSessionDetail(it.session.id) }
                },
                errorCallback = { failState ->
                    emitEvent(UserCheckEvent.ShowToast(failState.message, isError = true))
                }
            )
        }
    }

    private fun fetchAttendanceHistory() {
        viewModelScope.launch {
            resultResponse(
                response = getAttendanceHistoryUseCase(),
                successCallback = { data ->
                    val historyUIList = data.mapIndexed { index, history ->
                        CheckHistoryUIModel(
                            history = history,
                            isFirst = index == 0,
                            isLast = index == data.size - 1
                        )
                    }
                    updateState { copy(attendanceHistories = historyUIList) }
                },
                errorCallback = { failState ->
                    emitEvent(UserCheckEvent.ShowToast(failState.message, isError = true))
                }
            )
        }
    }

    private fun fetchSessionDetail(sessionId: Long) {
        viewModelScope.launch {
            resultResponse(
                response = getScheduleDetailUseCase(sessionId),
                successCallback = { detail ->
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
                },
                errorCallback = { failState ->
                    emitEvent(UserCheckEvent.ShowToast(failState.message, isError = true))
                }
            )
        }
    }

    /**
     * 실시간 위치 기반 출석 요청
     */
    fun requestAttendance(sheetId: Long) {
        val sessionUIModel = uiState.value.availableSessions.find { it.session.sheetId == sheetId }
        val scheduleId = sessionUIModel?.session?.id ?: return
        val isVerified = sessionUIModel.isWithinRange

        viewModelScope.launch {
            resultResponse(
                response = postScheduleAttendanceUseCase(scheduleId, isVerified, lastUserLat, lastUserLng),
                successCallback = { fetchAttendanceData() },
                errorCallback = { failState ->
                    emitEvent(UserCheckEvent.ShowToast(failState.message, isError = true))
                }
            )
        }
    }

    /**
     * 출석 사유 제출
     */
    fun submitAttendanceReason(sheetId: Long, reason: String) {
        viewModelScope.launch {
            resultResponse(
                response = postAttendanceReasonUseCase(sheetId, reason),
                successCallback = {
                    fetchAttendanceData()
                    fetchAttendanceHistory()
                },
                errorCallback = { failState ->
                    emitEvent(UserCheckEvent.ShowToast(failState.message, isError = true))
                }
            )
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
    data class ShowToast(val message: String, val isError: Boolean = false) : UserCheckEvent()
    data class ShowReasonDialog(val sessionId: Long) : UserCheckEvent()
    data class NavigateToFailureReason(val sessionId: Long) : UserCheckEvent()
}