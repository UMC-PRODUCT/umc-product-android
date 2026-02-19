package com.umc.presentation.ui.act.check

import androidx.lifecycle.viewModelScope
import com.umc.domain.model.act.check.UserCheckAvailable
import com.umc.domain.model.act.check.UserCheckHistory
import com.umc.domain.model.enums.CategoryType
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
        loadDummyData()
    }

    private fun loadDummyData() {
        val dummySessions = listOf(
            // 출석 전
            UserCheckAvailable(
                id = 1L,
                sheetId = 101L,
                title = "9기 데모데이",
                tags = listOf(CategoryType.PRESENTATION),
                startTime = "11:00",
                endTime = "17:00",
                status = CheckAvailableStatus.BEFORE,
                latitude = 37.495608,
                longitude = 127.072235,
                address = "SETEC",
                isLocationCertified = null
            ),
            // 출석 전
            UserCheckAvailable(
                id = 2L,
                sheetId = 102L,
                title = "9기 OT",
                tags = listOf(CategoryType.ORIENTATION),
                startTime = "17:00",
                endTime = "18:00",
                status = CheckAvailableStatus.BEFORE,
                latitude = 37.655878,
                longitude = 127.063968,
                address = "서울여자대학교 50주년기념관 310호",
                isLocationCertified = null
            ),
            // 3. 승인 대기
            UserCheckAvailable(
                id = 3L,
                sheetId = 103L,
                title = "PM Day",
                tags = listOf(CategoryType.PROJECT),
                startTime = "14:00",
                endTime = "17:00",
                status = CheckAvailableStatus.PENDING,
                latitude = 37.582566,
                longitude = 127.010063,
                address = "한성대학교 상상관 203호",
                isLocationCertified = true
            ),
            // 4. 출석 완료
            UserCheckAvailable(
                id = 4L,
                sheetId = 104L,
                title = "너디너리 해커톤",
                tags = listOf(CategoryType.HACKATHON),
                startTime = "13:00",
                endTime = "07:00",
                status = CheckAvailableStatus.COMPLETED,
                latitude = 37.546760,
                longitude = 126.949971,
                address = "서울 창업허브 공덕 10층",
                isLocationCertified = true
            )
        )

        val list = dummySessions.map { CheckAvailableUIModel(session = it) }
        updateState { copy(availableSessions = list, availableCount = list.size) }
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
                errorCallback = { failState -> emitEvent(UserCheckEvent.ShowToast(failState.message)) }
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
                errorCallback = { failState -> emitEvent(UserCheckEvent.ShowToast(failState.message)) }
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
                errorCallback = { failState -> emitEvent(UserCheckEvent.ShowToast(failState.message)) }
            )
        }
    }

    /**
     * 실시간 위치 기반 출석 요청
     */
    fun requestAttendance(sheetId: Long) {
        val sessionUIModel = uiState.value.availableSessions.find { it.session.sheetId == sheetId }
        val isVerified = sessionUIModel?.isWithinRange ?: false

        val request = AttendanceCheckRequest(
            attendanceSheetId = sheetId,
            latitude = lastUserLat,
            longitude = lastUserLng,
            locationVerified = isVerified
        )

        viewModelScope.launch {
            resultResponse(
                response = postAttendanceCheckUseCase(request),
                successCallback = { fetchAttendanceData() },
                errorCallback = { failState -> emitEvent(UserCheckEvent.ShowToast(failState.message)) }
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
                    emitEvent(UserCheckEvent.ShowToast(failState.message))
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
    data class ShowToast(val message: String) : UserCheckEvent()
    data class ShowReasonDialog(val sessionId: Long) : UserCheckEvent()
    data class NavigateToFailureReason(val sessionId: Long) : UserCheckEvent()
}