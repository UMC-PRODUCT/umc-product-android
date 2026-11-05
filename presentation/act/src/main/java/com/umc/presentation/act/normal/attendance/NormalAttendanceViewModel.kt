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
import com.umc.component.theme.AppStrings
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
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
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

    /**
     * 현재 위치로 세션의 출석 가능 여부를 판정한다.
     * silent 이면 실패해도 토스트를 내지 않는다(목록 재조회 후 자동 재판정용).
     */
    fun verifySessionLocation(sessionId: Long, silent: Boolean = false) {
        val session = uiState.value.availableSessions.firstOrNull { it.id == sessionId } ?: return
        if (session.isOnline) return

        if (!hasLocationPermission()) {
            if (!silent) emitEvent(NormalAttendanceEvent.ShowToast(AppStrings.ATTENDANCE_LOCATION_PERMISSION_REQUIRED_TOAST))
            return
        }

        val targetLatitude = session.latitude
        val targetLongitude = session.longitude
        if (
            targetLatitude == null ||
            targetLongitude == null ||
            (targetLatitude == 0.0 && targetLongitude == 0.0)
        ) {
            if (!silent) emitEvent(NormalAttendanceEvent.ShowToast(AppStrings.ATTENDANCE_LOCATION_MISSING_TOAST))
            return
        }

        try {
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                CancellationTokenSource().token
            ).addOnSuccessListener { currentLocation ->
                if (currentLocation == null) {
                    if (!silent) emitEvent(NormalAttendanceEvent.ShowToast(AppStrings.ACT_CURRENT_LOCATION_UNAVAILABLE_TOAST))
                    return@addOnSuccessListener
                }

                val distanceMeters = calculateDistanceMeters(
                    currentLatitude = currentLocation.latitude,
                    currentLongitude = currentLocation.longitude,
                    targetLatitude = targetLatitude,
                    targetLongitude = targetLongitude,
                )
                updateLocationVerification(
                    sessionId = sessionId,
                    isCertified = distanceMeters <= ATTENDANCE_RADIUS_METERS,
                    distanceMeters = distanceMeters.toInt(),
                )
            }.addOnFailureListener {
                if (!silent) emitEvent(NormalAttendanceEvent.ShowToast(AppStrings.ATTENDANCE_LOCATION_LOOKUP_FAILED_TOAST))
            }
        } catch (_: SecurityException) {
            if (!silent) emitEvent(NormalAttendanceEvent.ShowToast(AppStrings.ATTENDANCE_LOCATION_PERMISSION_REQUIRED_TOAST))
        }
    }

    //현재 위치를 조회해 세션 위치 반경 50m 이내인지 인증한 뒤 출석 요청
    fun requestAttendance(session: NormalAvailableSessionUi) {
        // 위치 확인·요청 응답을 기다리는 동안 다시 눌러도 같은 요청이 중복 전송되지 않게 한다
        if (uiState.value.isRequestingAttendance) return

        if (session.isOnline) {
            updateState { copy(isRequestingAttendance = true) }
            submitAttendance(
                session = session,
                latitude = null,
                longitude = null,
                locationVerified = true
            )
            return
        }

        if (!hasLocationPermission()) {
            emitEvent(NormalAttendanceEvent.ShowToast(AppStrings.ATTENDANCE_LOCATION_PERMISSION_REQUIRED_TOAST))
            return
        }

        val targetLatitude = session.latitude
        val targetLongitude = session.longitude
        if (
            targetLatitude == null ||
            targetLongitude == null ||
            (targetLatitude == 0.0 && targetLongitude == 0.0)
        ) {
            emitEvent(NormalAttendanceEvent.ShowToast(AppStrings.ATTENDANCE_LOCATION_MISSING_TOAST))
            return
        }

        updateState { copy(isRequestingAttendance = true) }
        startLoading()
        try {
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                CancellationTokenSource().token
            ).addOnSuccessListener { currentLocation ->
                if (currentLocation == null) {
                    finishAttendanceRequest()
                    emitEvent(NormalAttendanceEvent.ShowToast(AppStrings.ACT_CURRENT_LOCATION_UNAVAILABLE_TOAST))
                    return@addOnSuccessListener
                }

                val distanceMeters = calculateDistanceMeters(
                    currentLatitude = currentLocation.latitude,
                    currentLongitude = currentLocation.longitude,
                    targetLatitude = targetLatitude,
                    targetLongitude = targetLongitude,
                )

                if (distanceMeters > ATTENDANCE_RADIUS_METERS) {
                    finishAttendanceRequest()
                    updateLocationVerification(
                        sessionId = session.id,
                        isCertified = false,
                        distanceMeters = distanceMeters.toInt(),
                    )
                    emitEvent(
                        NormalAttendanceEvent.ShowToast(
                            AppStrings.ATTENDANCE_LOCATION_DISTANCE_FORMAT.format(distanceMeters.toInt())
                        )
                    )
                    return@addOnSuccessListener
                }

                updateLocationVerification(
                    sessionId = session.id,
                    isCertified = true,
                    distanceMeters = distanceMeters.toInt(),
                )
                submitAttendance(
                    session = session,
                    latitude = currentLocation.latitude,
                    longitude = currentLocation.longitude,
                    locationVerified = true,
                    loadingAlreadyStarted = true
                )
            }.addOnFailureListener {
                finishAttendanceRequest()
                emitEvent(NormalAttendanceEvent.ShowToast(AppStrings.ATTENDANCE_LOCATION_LOOKUP_FAILED_TOAST))
            }.addOnCanceledListener {
                finishAttendanceRequest()
            }
        } catch (_: SecurityException) {
            finishAttendanceRequest()
            emitEvent(NormalAttendanceEvent.ShowToast(AppStrings.ATTENDANCE_LOCATION_PERMISSION_REQUIRED_TOAST))
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
                    finishAttendanceRequest()
                    refresh()
                },
                errorCallback = {
                    finishAttendanceRequest()
                    emitEvent(NormalAttendanceEvent.ShowToast(it.message))
                }
            )
        }
    }

    //출석 요청 진행 상태 해제
    private fun finishAttendanceRequest() {
        stopLoading()
        updateState { copy(isRequestingAttendance = false) }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun calculateDistanceMeters(
        currentLatitude: Double,
        currentLongitude: Double,
        targetLatitude: Double,
        targetLongitude: Double,
    ): Float {
        val result = FloatArray(1)
        Location.distanceBetween(
            currentLatitude,
            currentLongitude,
            targetLatitude,
            targetLongitude,
            result,
        )
        return result[0]
    }

    private fun updateLocationVerification(
        sessionId: Long,
        isCertified: Boolean,
        distanceMeters: Int,
    ) {
        updateState {
            copy(
                availableSessions = availableSessions.map { session ->
                    if (session.id == sessionId) {
                        session.copy(
                            isLocationCertified = isCertified,
                            locationDistanceMeters = distanceMeters,
                        )
                    } else {
                        session
                    }
                }.toImmutableList()
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
        // 제출 응답을 기다리는 동안 다시 눌러도 같은 사유가 중복 전송되지 않게 한다
        if (state.isSubmittingReason) return

        updateState { copy(isSubmittingReason = true) }
        viewModelScope.launch {
            startLoading()
            resultResponse(
                response = postAttendanceReasonUseCase(session.sheetId, reason),
                successCallback = {
                    updateState { copy(reasonSessionId = null, reason = "", isSubmittingReason = false) }
                    refresh()
                },
                errorCallback = {
                    updateState { copy(isSubmittingReason = false) }
                    emitEvent(NormalAttendanceEvent.ShowToast(it.message))
                }
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
                    updateState {
                        // 서버 응답에는 클라이언트가 판정한 위치 인증 결과가 없으므로,
                        // 출석 위치가 그대로인 세션은 직전 인증 결과를 이어받는다
                        val previousSessions = availableSessions.associateBy { it.id }
                        copy(
                            availableSessions = sessions.map { session ->
                                val ui = session.toUi()
                                val previous = previousSessions[ui.id]
                                if (previous != null &&
                                    previous.latitude == ui.latitude &&
                                    previous.longitude == ui.longitude
                                ) {
                                    ui.copy(
                                        isLocationCertified = previous.isLocationCertified,
                                        locationDistanceMeters = previous.locationDistanceMeters,
                                    )
                                } else {
                                    ui
                                }
                            }.toImmutableList()
                        )
                    }
                    // 펼쳐진 카드는 새 목록 기준으로 현재 위치를 다시 판정한다.
                    // 출석 전·반려 상태 카드만 대상이며, 재조회 경로에서는 실패 토스트를 내지 않는다.
                    if (hasLocationPermission()) {
                        val expandedId = uiState.value.expandedSessionId
                        val expanded = uiState.value.availableSessions
                            .firstOrNull { it.id == expandedId }
                        if (expanded != null &&
                            (expanded.status == CheckAvailableStatus.BEFORE ||
                                expanded.status == CheckAvailableStatus.REJECTED)
                        ) {
                            verifySessionLocation(expanded.id, silent = true)
                        }
                    }
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
                    updateState { copy(historySessions = sessions.map { it.toUi() }.toImmutableList()) }
                },
                errorCallback = { emitEvent(NormalAttendanceEvent.ShowToast(it.message)) }
            )
        }
    }
}

data class NormalAttendanceUiState(
    //출석 가능한 세션 목록
    val availableSessions: ImmutableList<NormalAvailableSessionUi> = persistentListOf(),
    //내 출석 기록 목록
    val historySessions: ImmutableList<NormalHistorySessionUi> = persistentListOf(),
    //펼쳐진 세션 ID
    val expandedSessionId: Long? = null,
    //출석 실패 사유 작성 대상 세션 ID
    val reasonSessionId: Long? = null,
    //출석 실패 사유
    val reason: String = "",
    //출석 요청 진행 중 여부
    val isRequestingAttendance: Boolean = false,
    //출석 실패 사유 제출 진행 중 여부
    val isSubmittingReason: Boolean = false,
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
    val isLocationCertified: Boolean?,
    val address: String,
    val latitude: Double?,
    val longitude: Double?,
    val isOnline: Boolean = false,
    val locationDistanceMeters: Int? = null,
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
        timeRange = AppStrings.ATTENDANCE_TIME_RANGE_FORMAT.format(startTime, endTime),
        status = status,
        isLocationCertified = isLocationCertified,
        address = address
            .toShortAttendanceAddress()
            .ifBlank { "-" },
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
        timeRange = AppStrings.ATTENDANCE_TIME_RANGE_FORMAT.format(startTime, endTime),
        status = status
    )
}

private const val ATTENDANCE_RADIUS_METERS = 50f

private val shortAdministrativeAreas = linkedMapOf(
    "서울특별시" to "서울",
    "부산광역시" to "부산",
    "대구광역시" to "대구",
    "인천광역시" to "인천",
    "광주광역시" to "광주",
    "대전광역시" to "대전",
    "울산광역시" to "울산",
    "세종특별자치시" to "세종",
    "경기도" to "경기",
    "강원특별자치도" to "강원",
    "강원도" to "강원",
    "충청북도" to "충북",
    "충청남도" to "충남",
    "전북특별자치도" to "전북",
    "전라북도" to "전북",
    "전라남도" to "전남",
    "경상북도" to "경북",
    "경상남도" to "경남",
    "제주특별자치도" to "제주",
)

internal fun String.toShortAttendanceAddress(): String {
    val normalized = trim()
        .replace(Regex("""\s*[\(（][^\)）]*[\)）]\s*$"""), "")
        .replace(Regex("\\s+"), " ")
    val (fullName, shortName) = shortAdministrativeAreas.entries
        .firstOrNull { (fullName, _) -> normalized.startsWith(fullName) }
        ?.let { it.key to it.value }
        ?: return normalized

    return shortName + normalized.removePrefix(fullName)
}
