package com.umc.presentation.act.normal.attendance

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
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
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NormalAttendanceViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getAttendanceAvailableUseCase: GetAttendanceAvailableUseCase, //출석 가능한 세션 조회
    private val getAttendanceHistoryUseCase: GetAttendanceHistoryUseCase, //내 출석 기록 조회
    private val postAttendanceCheckUseCase: PostAttendanceCheckUseCase, //출석 요청
    private val postAttendanceReasonUseCase: PostAttendanceReasonUseCase, //출석 실패 사유 제출
) : BaseViewModel<NormalAttendanceUiState, NormalAttendanceEvent>(
    NormalAttendanceUiState()
) {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    //초기 출석 정보 조회
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

    //현재 위치를 조회해 세션 위치 반경 50m 이내인지 인증한 뒤 출석 요청
    fun requestAttendance(session: NormalAvailableSessionUi) {
        if (session.isOnline) {
            submitAttendance(
                session = session,
                latitude = null,
                longitude = null,
                locationVerified = true
            )
            return
        }

        if (!hasLocationPermission()) {
            emitEvent(NormalAttendanceEvent.ShowToast("정확한 위치 권한이 필요합니다."))
            return
        }

        val targetLatitude = session.latitude
        val targetLongitude = session.longitude
        if (
            targetLatitude == null ||
            targetLongitude == null ||
            (targetLatitude == 0.0 && targetLongitude == 0.0)
        ) {
            emitEvent(NormalAttendanceEvent.ShowToast("출석 위치 정보가 없습니다."))
            return
        }

        startLoading()
        try {
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                CancellationTokenSource().token
            ).addOnSuccessListener { currentLocation ->
                if (currentLocation == null) {
                    stopLoading()
                    emitEvent(NormalAttendanceEvent.ShowToast("현재 위치를 확인할 수 없습니다."))
                    return@addOnSuccessListener
                }

                val distanceResult = FloatArray(1)
                Location.distanceBetween(
                    currentLocation.latitude,
                    currentLocation.longitude,
                    targetLatitude,
                    targetLongitude,
                    distanceResult
                )

                if (distanceResult[0] > ATTENDANCE_RADIUS_METERS) {
                    stopLoading()
                    emitEvent(
                        NormalAttendanceEvent.ShowToast(
                            "출석 위치에서 ${distanceResult[0].toInt()}m 떨어져 있습니다."
                        )
                    )
                    return@addOnSuccessListener
                }

                submitAttendance(
                    session = session,
                    latitude = currentLocation.latitude,
                    longitude = currentLocation.longitude,
                    locationVerified = true,
                    loadingAlreadyStarted = true
                )
            }.addOnFailureListener {
                stopLoading()
                emitEvent(NormalAttendanceEvent.ShowToast("현재 위치 확인에 실패했습니다."))
            }
        } catch (_: SecurityException) {
            stopLoading()
            emitEvent(NormalAttendanceEvent.ShowToast("정확한 위치 권한이 필요합니다."))
        }
    }

    private fun submitAttendance(
        session: NormalAvailableSessionUi,
        latitude: Double?,
        longitude: Double?,
        locationVerified: Boolean,
        loadingAlreadyStarted: Boolean = false
    ) {
        viewModelScope.launch {
            if (!loadingAlreadyStarted) startLoading()
            resultResponse(
                response = postAttendanceCheckUseCase(
                    AttendanceCheckRequest(
                        attendanceSheetId = session.sheetId,
                        latitude = latitude,
                        longitude = longitude,
                        locationVerified = locationVerified
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

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
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
    val isOnline: Boolean = false,
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
        longitude = longitude,
        isOnline = isOnline,
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

private const val ATTENDANCE_RADIUS_METERS = 50f
