package com.umc.presentation.ui.act.check

import androidx.lifecycle.viewModelScope
import com.umc.domain.model.enums.CheckAvailableStatus
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
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import com.umc.domain.model.act.check.AdminPendingUser

@HiltViewModel
class UserCheckViewModel @Inject constructor(
    private val getAttendanceAvailableUseCase: GetAttendanceAvailableUseCase,
    private val getAttendanceHistoryUseCase: GetAttendanceHistoryUseCase,
    private val getScheduleDetailUseCase: GetScheduleDetailUseCase,
    private val postAttendanceCheckUseCase: PostAttendanceCheckUseCase,
    private val postAttendanceReasonUseCase: PostAttendanceReasonUseCase,
    private val dummyRepo: DummyAttendanceRepository
) : BaseViewModel<UserCheckUiState, UserCheckEvent>(UserCheckUiState()) {

    private var lastUserLat: Double? = null
    private var lastUserLng: Double? = null

    // sheetId → AdminSessionCheck.id 매핑 (더미 전용)
    private val sheetIdToSessionId = mapOf(
        101L to 1L,
        102L to 2L,
        103L to 3L,
        104L to 4L
    )

    init {
        // Repository의 StateFlow를 구독 → ViewModel 재생성 시에도 최신 상태 반영
        viewModelScope.launch {
            dummyRepo.userSessions.collect { sessions ->
                val list = sessions.map { CheckAvailableUIModel(session = it) }
                updateState { copy(availableSessions = list, availableCount = list.size) }
            }
        }
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
     * 현 위치 기반 출석 요청
     * 더미: Repository에 바로 저장 → AdminCheckViewModel이 언제 생성되든 최신 상태를 읽음
     */
    fun requestAttendance(sheetId: Long) {
        val sessionUIModel = uiState.value.availableSessions.find { it.session.sheetId == sheetId }
        val isVerified = sessionUIModel?.isWithinRange ?: false

        // 1. User 세션 상태 변경 (Repository → StateFlow → UI 자동 반영)
        dummyRepo.updateUserSessionStatus(
            sheetId = sheetId,
            status = CheckAvailableStatus.PENDING,
            isLocationCertified = isVerified
        )

        // 2. Admin 승인 대기 목록에 유엠씨 추가
        val sessionId = sheetIdToSessionId[sheetId] ?: return
        dummyRepo.addPendingUser(
            sessionId = sessionId,
            user = dummyPendingUser(hasLateReason = false, lateReason = null)
        )

        /* API 연동 시 위 블록 전체를 아래로 교체
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
        */
    }

    /**
     * 출석 사유 제출
     * 더미: Repository에 바로 저장 → AdminCheckViewModel이 언제 생성되든 최신 상태를 읽음
     */
    fun submitAttendanceReason(sheetId: Long, reason: String) {
        // 1. User 세션 상태 변경 (Repository → StateFlow → UI 자동 반영)
        dummyRepo.updateUserSessionStatus(
            sheetId = sheetId,
            status = CheckAvailableStatus.PENDING,
            isLocationCertified = false
        )

        // 2. Admin 승인 대기 목록에 유엠씨 추가 (사유 포함)
        val sessionId = sheetIdToSessionId[sheetId] ?: return
        dummyRepo.addPendingUser(
            sessionId = sessionId,
            user = dummyPendingUser(hasLateReason = true, lateReason = reason)
        )

        /* API 연동 시 위 블록 전체를 아래로 교체
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
        */
    }

    private fun dummyPendingUser(hasLateReason: Boolean, lateReason: String?) = AdminPendingUser(
        id = 999L,
        name = "유엠씨",
        nickname = "umc",
        university = "UMC대학교",
        profileImageUrl = null,
        requestTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")),
        hasLateReason = hasLateReason,
        lateReason = lateReason
    )

    fun updateLocation(userLat: Double, userLng: Double) {
        lastUserLat = userLat
        lastUserLng = userLng

        updateState {
            val updatedList = availableSessions.map { uiModel ->
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