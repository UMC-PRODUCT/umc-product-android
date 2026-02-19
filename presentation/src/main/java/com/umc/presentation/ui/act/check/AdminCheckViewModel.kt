package com.umc.presentation.ui.act.check

import androidx.lifecycle.viewModelScope
import com.umc.domain.model.act.check.AdminPendingUser
import com.umc.domain.model.act.check.AdminSessionCheck
import com.umc.domain.model.enums.AdminSessionStatus
import com.umc.domain.usecase.attendance.GetPendingUsersUseCase
import com.umc.domain.usecase.attendance.PostAttendanceApprovalUseCase
import com.umc.domain.usecase.attendance.PostAttendanceRejectionUseCase
import com.umc.domain.usecase.schedule.GetAdminSessionListUseCase
import com.umc.domain.usecase.schedule.UpdateScheduleLocationUseCase
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
    private val postAttendanceRejectionUseCase: PostAttendanceRejectionUseCase,
    private val updateScheduleLocationUseCase: UpdateScheduleLocationUseCase
) : BaseViewModel<AdminCheckUiState, AdminCheckEvent>(AdminCheckUiState()) {

    init {
        loadDummyData()
    }

    private fun loadDummyData() {
        val dummySessions = listOf(
            AdminSessionCheck(
                id = 1L,
                title = "9기 데모데이",
                date = "2025-05-20",
                startTime = "11:00",
                endTime = "17:00",
                status = AdminSessionStatus.IN_PROGRESS,
                attendanceRate = 80,
                totalChallengers = 10,
                attendedChallengers = 8,
                pendingCount = 2,
                pendingUsers = listOf(
                    AdminPendingUser(
                        id = 201L,
                        name = "김민준",
                        nickname = "minjun",
                        university = "한국대학교",
                        profileImageUrl = null,
                        requestTime = "11:15",
                        hasLateReason = true,
                        lateReason = "교통 혼잡으로 인해 지각하였습니다."
                    ),
                    AdminPendingUser(
                        id = 202L,
                        name = "이서연",
                        nickname = "seoyeon",
                        university = "서울대학교",
                        profileImageUrl = null,
                        requestTime = "11:22",
                        hasLateReason = false,
                        lateReason = null
                    )
                )
            ),
            AdminSessionCheck(
                id = 2L,
                title = "9기 OT",
                date = "2025-05-21",
                startTime = "17:00",
                endTime = "18:00",
                status = AdminSessionStatus.IN_PROGRESS,
                attendanceRate = 50,
                totalChallengers = 10,
                attendedChallengers = 5,
                pendingCount = 1,
                pendingUsers = listOf(
                    AdminPendingUser(
                        id = 203L,
                        name = "박지호",
                        nickname = "jiho",
                        university = "연세대학교",
                        profileImageUrl = null,
                        requestTime = "17:08",
                        hasLateReason = true,
                        lateReason = "수업이 늦게 끝나서 지각하였습니다."
                    )
                )
            ),
            AdminSessionCheck(
                id = 3L,
                title = "PM Day",
                date = "2025-05-19",
                startTime = "18:00",
                endTime = "20:00",
                status = AdminSessionStatus.IN_PROGRESS,
                attendanceRate = 70,
                totalChallengers = 10,
                attendedChallengers = 7,
                pendingCount = 0,
                pendingUsers = emptyList()
            ),
            AdminSessionCheck(
                id = 4L,
                title = "너디너리 해커톤",
                date = "2025-05-18",
                startTime = "13:00",
                endTime = "15:00",
                status = AdminSessionStatus.COMPLETED,
                attendanceRate = 90,
                totalChallengers = 10,
                attendedChallengers = 9,
                pendingCount = 0,
                pendingUsers = emptyList()
            )
        )

        val uiModels = dummySessions.map { AdminSessionUIModel(session = it) }
        updateState { copy(adminSessions = uiModels) }
    }

    fun fetchAdminSessions() {
        viewModelScope.launch {
            resultResponse(
                response = getAdminSessionListUseCase(),
                successCallback = { data ->
                    val currentSessions = uiState.value.adminSessions
                    val uiModels = data.map { domainModel ->
                        val wasExpanded = currentSessions.find { it.session.id == domainModel.id }?.isExpanded ?: false
                        AdminSessionUIModel(session = domainModel, isExpanded = wasExpanded)
                    }
                    updateState { copy(adminSessions = uiModels) }
                },
                errorCallback = { failState -> emitEvent(AdminCheckEvent.ShowToast(failState.message)) }
            )
        }
    }

    /**
     * 출석 승인 버튼 클릭 시 호출
     */
    fun approveAttendance(attendanceId: Long) {
        viewModelScope.launch {
            resultResponse(
                response = postAttendanceApprovalUseCase(attendanceId),
                successCallback = { updateSessionAfterApproval(attendanceId) },
                errorCallback = { failState ->
                    emitEvent(AdminCheckEvent.ShowToast(failState.message))
                }
            )
        }
    }

    /**
     * 출석 반려 버튼 클릭 시 호출
     */
    fun rejectAttendance(attendanceId: Long) {
        viewModelScope.launch {
            resultResponse(
                response = postAttendanceRejectionUseCase(attendanceId),
                successCallback = {
                    updateSessionAfterRejection(attendanceId)
                },
                errorCallback = { failState ->
                    emitEvent(AdminCheckEvent.ShowToast(failState.message))
                }
            )
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

    fun updateSessionLocation(sessionId: Long, lat: Double, lng: Double, address: String) {
        viewModelScope.launch {
            val result = updateScheduleLocationUseCase(sessionId, address, lat, lng)

            resultResponse(
                response = result,
                successCallback = {
                    emitEvent(AdminCheckEvent.ShowToast("출석 위치가 성공적으로 변경되었습니다."))
                },
                errorCallback = { failState ->
                    emitEvent(AdminCheckEvent.ShowToast(failState.message))
                }
            )
        }
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
            resultResponse(
                response = getPendingUsersUseCase(sessionId),
                successCallback = { data ->
                    updateState {
                        val updatedList = adminSessions.map { uiModel ->
                            if (uiModel.session.id == sessionId) {
                                uiModel.copy(session = uiModel.session.copy(pendingUsers = data))
                            } else uiModel
                        }
                        copy(adminSessions = updatedList)
                    }
                },
                errorCallback = { failState -> emitEvent(AdminCheckEvent.ShowToast(failState.message)) }
            )
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
}