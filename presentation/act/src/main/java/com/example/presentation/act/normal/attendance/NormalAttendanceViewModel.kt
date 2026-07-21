package com.example.presentation.act.normal.attendance

import androidx.lifecycle.viewModelScope
import com.umc.component.base.BaseViewModel
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.umc.domain.model.act.check.UserCheckAvailable
import com.umc.domain.model.act.check.UserCheckHistory
import com.umc.domain.model.enums.CheckAvailableStatus
import com.umc.domain.model.enums.CheckHistoryStatus
import com.umc.domain.model.request.attendance.AttendanceCheckRequest
import com.umc.domain.usecase.attendance.GetAttendanceAvailableUseCase
import com.umc.domain.usecase.attendance.GetAttendanceHistoryUseCase
import com.umc.domain.usecase.attendance.PostAttendanceCheckUseCase
import com.umc.domain.usecase.attendance.PostAttendanceReasonUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NormalAttendanceViewModel @Inject constructor(
    private val getAttendanceAvailableUseCase: GetAttendanceAvailableUseCase, //출석 가능한 세션 조회
    private val getAttendanceHistoryUseCase: GetAttendanceHistoryUseCase, //내 출석 기록 조회
    private val postAttendanceCheckUseCase: PostAttendanceCheckUseCase, //출석 요청
    private val postAttendanceReasonUseCase: PostAttendanceReasonUseCase, //출석 실패 사유 제출
) : BaseViewModel<NormalAttendanceUiState, NormalAttendanceEvent>(
    NormalAttendanceUiState()
) {
    //초기 출석 정보 조회
    init {
        refresh()
    }

    //출석 가능 세션과 내 출석 기록 새로고침
    fun refresh() {
        getAvailableSessions()
        getHistorySessions()
    }

    //출석 가능 세션 펼침 상태 변경
    fun toggleSessionExpanded(sessionId: Long) {
        updateState {
            copy(expandedSessionId = if (expandedSessionId == sessionId) null else sessionId)
        }
    }

    //위치 인증된 세션 출석 요청
    fun requestAttendance(session: NormalAvailableSessionUi) {
        viewModelScope.launch {
            startLoading()
            resultResponse(
                response = postAttendanceCheckUseCase(
                    AttendanceCheckRequest(
                        attendanceSheetId = session.sheetId,
                        latitude = session.latitude,
                        longitude = session.longitude,
                        locationVerified = session.isLocationCertified
                    )
                ),
                successCallback = {
                    emitEvent(NormalAttendanceEvent.ShowToast(it))
                    refresh()
                },
                errorCallback = { emitEvent(NormalAttendanceEvent.ShowToast(it.message)) }
            )
        }
    }

    //출석 실패 사유 다이얼로그 열기
    fun openReasonDialog(sessionId: Long) {
        updateState { copy(reasonSessionId = sessionId, reason = "") }
    }

    //출석 실패 사유 다이얼로그 닫기
    fun dismissReasonDialog() {
        updateState { copy(reasonSessionId = null, reason = "") }
    }

    //출석 실패 사유 입력
    fun onReasonChanged(reason: String) {
        updateState { copy(reason = reason) }
    }

    //출석 실패 사유 제출
    fun submitReason() {
        val state = uiState.value
        val session = state.availableSessions.firstOrNull { it.id == state.reasonSessionId } ?: return
        val reason = state.reason.trim()
        if (reason.isEmpty()) return

        viewModelScope.launch {
            startLoading()
            resultResponse(
                response = postAttendanceReasonUseCase(session.sheetId, reason),
                successCallback = {
                    emitEvent(NormalAttendanceEvent.ShowToast(it))
                    updateState { copy(reasonSessionId = null, reason = "") }
                    refresh()
                },
                errorCallback = { emitEvent(NormalAttendanceEvent.ShowToast(it.message)) }
            )
        }
    }

    //출석 가능한 세션 조회
    private fun getAvailableSessions() {
        viewModelScope.launch {
            startLoading()
            resultResponse(
                response = getAttendanceAvailableUseCase(),
                successCallback = { sessions ->
                    updateState { copy(availableSessions = sessions.map { it.toUi() }) }
                },
                errorCallback = { emitEvent(NormalAttendanceEvent.ShowToast(it.message)) }
            )
        }
    }

    //내 출석 기록 조회
    private fun getHistorySessions() {
        viewModelScope.launch {
            startLoading()
            resultResponse(
                response = getAttendanceHistoryUseCase(),
                successCallback = { sessions ->
                    updateState { copy(historySessions = sessions.map { it.toUi() }) }
                },
                errorCallback = { emitEvent(NormalAttendanceEvent.ShowToast(it.message)) }
            )
        }
    }
}

data class NormalAttendanceUiState(
    //출석 가능한 세션 목록
    val availableSessions: List<NormalAvailableSessionUi> = emptyList(),
    //내 출석 기록 목록
    val historySessions: List<NormalHistorySessionUi> = emptyList(),
    //펼쳐진 세션 ID
    val expandedSessionId: Long? = null,
    //출석 실패 사유 작성 대상 세션 ID
    val reasonSessionId: Long? = null,
    //출석 실패 사유
    val reason: String = "",
) : UiState {
    //출석 가능한 세션 비어있음 여부
    val isAvailableEmpty: Boolean get() = availableSessions.isEmpty()
    //내 출석 기록 비어있음 여부
    val isHistoryEmpty: Boolean get() = historySessions.isEmpty()
}

data class NormalAvailableSessionUi(
    val id: Long,
    val sheetId: Long,
    val title: String,
    val timeRange: String,
    val status: CheckAvailableStatus,
    val isLocationCertified: Boolean,
    val address: String,
    val latitude: Double?,
    val longitude: Double?,
)

data class NormalHistorySessionUi(
    val id: Long,
    val title: String,
    val timeRange: String,
    val status: CheckHistoryStatus,
)

sealed interface NormalAttendanceEvent : UiEvent {
    //토스트 표시
    data class ShowToast(val message: String) : NormalAttendanceEvent
}

//출석 가능 세션 도메인 모델을 UI 모델로 변환
private fun UserCheckAvailable.toUi(): NormalAvailableSessionUi {
    return NormalAvailableSessionUi(
        id = id,
        sheetId = sheetId,
        title = title,
        timeRange = "$startTime - $endTime",
        status = status,
        isLocationCertified = isLocationCertified == true,
        address = address.ifBlank { "-" },
        latitude = latitude,
        longitude = longitude
    )
}

//출석 기록 도메인 모델을 UI 모델로 변환
private fun UserCheckHistory.toUi(): NormalHistorySessionUi {
    return NormalHistorySessionUi(
        id = id.toLong(),
        title = title,
        timeRange = "$startTime - $endTime",
        status = status
    )
}
