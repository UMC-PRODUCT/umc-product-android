package com.umc.presentation.ui.act.check

import androidx.lifecycle.viewModelScope
import com.umc.domain.model.base.ApiState
import com.umc.domain.usecase.attendance.GetPendingUsersUseCase
import com.umc.domain.usecase.attendance.PostAttendanceApprovalUseCase
import com.umc.domain.usecase.attendance.PostAttendanceRejectionUseCase
import com.umc.domain.usecase.schedule.GetAdminSessionListUseCase
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminCheckViewModel @Inject constructor(
    private val getAdminSessionListUseCase: GetAdminSessionListUseCase,
    private val getPendingUsersUseCase: GetPendingUsersUseCase,
    private val postAttendanceApprovalUseCase: PostAttendanceApprovalUseCase,
    private val postAttendanceRejectionUseCase: PostAttendanceRejectionUseCase
) : BaseViewModel<AdminCheckUiState, AdminCheckEvent>(AdminCheckUiState()) {

    init {
        fetchAdminSessions()
    }

    fun fetchAdminSessions() {
        viewModelScope.launch {
            when (val result = getAdminSessionListUseCase()) {
                is ApiState.Success -> {
                    val currentSessions = uiState.value.adminSessions
                    val uiModels = result.data.map { domainModel ->
                        val wasExpanded = currentSessions.find { it.session.id == domainModel.id }?.isExpanded ?: false
                        AdminSessionUIModel(session = domainModel, isExpanded = wasExpanded)
                    }
                    updateState { copy(adminSessions = uiModels) }
                }
                is ApiState.Fail -> emitEvent(AdminCheckEvent.ShowToast(result.failState.message))
            }
        }
    }

    /**
     * 출석 승인 버튼 클릭 시 호출
     */
    fun approveAttendance(attendanceId: Long) {
        viewModelScope.launch {
            when (val result = postAttendanceApprovalUseCase(attendanceId)) {
                is ApiState.Success -> {
                    updateSessionAfterApproval(attendanceId)
                }
                is ApiState.Fail -> {
                    emitEvent(AdminCheckEvent.ShowToast(result.failState.message ?: "승인에 실패했습니다."))
                }
            }
        }
    }

    /**
     * 출석 반려 버튼 클릭 시 호출
     */
    fun rejectAttendance(attendanceId: Long) {
        viewModelScope.launch {
            android.util.Log.d("AdminCheck", "반려 시작: attendanceId=$attendanceId")

            when (val result = postAttendanceRejectionUseCase(attendanceId)) {
                is ApiState.Success -> {
                    android.util.Log.d("AdminCheck", "반려 성공")

                    // 로컬 UI 업데이트 (유저 제거만, 출석 통계는 변경 없음)
                    updateSessionAfterRejection(attendanceId)

                    emitEvent(AdminCheckEvent.ShowToast("출석이 반려되었습니다."))
                }
                is ApiState.Fail -> {
                    android.util.Log.e("AdminCheck", "반려 실패: ${result.failState.message}")
                    emitEvent(AdminCheckEvent.ShowToast(result.failState.message ?: "반려에 실패했습니다."))
                }
            }
        }
    }

    /**
     * 승인 후 세션 업데이트
     * - pending 목록에서 유저 제거
     * - attendedChallengers +1
     * - attendanceRate 재계산
     * - pendingCount -1
     */
    private fun updateSessionAfterApproval(attendanceId: Long) {
        updateState {
            val updatedSessions = adminSessions.map { uiModel ->
                val session = uiModel.session

                // 해당 유저가 이 세션에 있는지 확인
                val targetUser = session.pendingUsers.find { it.id == attendanceId }

                if (targetUser != null) {
                    // pending 목록에서 제거
                    val updatedPendingUsers = session.pendingUsers.filter { it.id != attendanceId }

                    // 출석 완료 인원 +1
                    val newAttendedChallengers = session.attendedChallengers + 1

                    // 출석률 재계산 (출석 완료 인원 / 전체 인원 * 100)
                    val newAttendanceRate = if (session.totalChallengers > 0) {
                        (newAttendedChallengers * 100) / session.totalChallengers
                    } else {
                        0
                    }

                    val updatedSession = session.copy(
                        pendingUsers = updatedPendingUsers,
                        pendingCount = updatedPendingUsers.size,
                        attendedChallengers = newAttendedChallengers,
                        attendanceRate = newAttendanceRate
                    )

                    uiModel.copy(session = updatedSession)
                } else {
                    // 다른 세션은 그대로 유지
                    uiModel
                }
            }
            copy(adminSessions = updatedSessions)
        }
    }

    /**
     * 반려 후 세션 업데이트
     * - pending 목록에서 유저 제거
     * - pendingCount -1
     * - attendedChallengers, attendanceRate는 변경 없음 (반려이므로)
     */
    private fun updateSessionAfterRejection(attendanceId: Long) {
        updateState {
            val updatedSessions = adminSessions.map { uiModel ->
                val session = uiModel.session

                // 해당 유저가 이 세션에 있는지 확인
                val targetUser = session.pendingUsers.find { it.id == attendanceId }

                if (targetUser != null) {
                    // pending 목록에서 제거
                    val updatedPendingUsers = session.pendingUsers.filter { it.id != attendanceId }

                    val updatedSession = session.copy(
                        pendingUsers = updatedPendingUsers,
                        pendingCount = updatedPendingUsers.size
                        // 반려이므로 attendedChallengers, attendanceRate는 변경 없음
                    )

                    uiModel.copy(session = updatedSession)
                } else {
                    // 다른 세션은 그대로 유지
                    uiModel
                }
            }
            copy(adminSessions = updatedSessions)
        }
    }

    fun onLocationChangeClicked(sessionId: Long) {
        emitEvent(AdminCheckEvent.ShowLocationDialog(
            sessionId = sessionId,
            lat = 37.582568,
            lng = 127.001488,
            address = "서울특별시 종로구 명륜4가"
        ))
    }

    fun updateSessionLocation(sessionId: Long, lat: Double, lng: Double, address: String) {
        // TODO: 서버 위치 업데이트 API 호출
        emitEvent(AdminCheckEvent.ShowToast("출석 위치가 성공적으로 변경되었습니다."))
    }

    fun toggleSessionExpansion(sessionId: Long) {
        val currentSession = uiState.value.adminSessions.find { it.session.id == sessionId }

        if (currentSession?.isExpanded == false && currentSession.session.pendingUsers.isEmpty()) {
            fetchPendingUsers(sessionId)
        }

        updateExpansionState(sessionId)
    }

    private fun fetchPendingUsers(sessionId: Long) {
        viewModelScope.launch {
            when (val result = getPendingUsersUseCase(sessionId)) {
                is ApiState.Success -> {
                    updateState {
                        val updatedList = adminSessions.map { uiModel ->
                            if (uiModel.session.id == sessionId) {
                                uiModel.copy(session = uiModel.session.copy(pendingUsers = result.data))
                            } else uiModel
                        }
                        copy(adminSessions = updatedList)
                    }
                }
                is ApiState.Fail -> emitEvent(AdminCheckEvent.ShowToast(result.failState.message))
            }
        }
    }

    private fun updateExpansionState(sessionId: Long) {
        updateState {
            val newList = adminSessions.map { uiModel ->
                if (uiModel.session.id == sessionId) {
                    uiModel.copy(isExpanded = !uiModel.isExpanded)
                } else {
                    uiModel
                }
            }
            copy(adminSessions = newList)
        }
    }
}

data class AdminCheckUiState(
    val adminSessions: List<AdminSessionUIModel> = emptyList()
) : UiState

sealed class AdminCheckEvent : UiEvent {
    data class ShowToast(val message: String) : AdminCheckEvent()
    data class ShowLocationDialog(
        val sessionId: Long,
        val lat: Double,
        val lng: Double,
        val address: String
    ) : AdminCheckEvent()
}