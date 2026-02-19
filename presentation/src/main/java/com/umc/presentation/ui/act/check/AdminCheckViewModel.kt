package com.umc.presentation.ui.act.check

import androidx.lifecycle.viewModelScope
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
    private val updateScheduleLocationUseCase: UpdateScheduleLocationUseCase,
    private val dummyRepo: DummyAttendanceRepository
) : BaseViewModel<AdminCheckUiState, AdminCheckEvent>(AdminCheckUiState()) {

    init {
        // Repository의 StateFlow 구독
        // ViewModel 재생성 시에도, User 화면에서 변경된 최신 상태를 바로 반영
        viewModelScope.launch {
            dummyRepo.adminSessions.collect { sessions ->
                // 기존 확장 상태(isExpanded) 유지
                val currentExpanded = uiState.value.adminSessions
                    .filter { it.isExpanded }
                    .map { it.session.id }
                    .toSet()

                val uiModels = sessions.map { session ->
                    AdminSessionUIModel(
                        session = session,
                        isExpanded = session.id in currentExpanded
                    )
                }
                updateState { copy(adminSessions = uiModels) }
            }
        }
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

    fun approveAttendance(attendanceId: Long) {
        // Repository 변경 → StateFlow → UI 자동 반영
        dummyRepo.updateAdminSessionAfterApproval(attendanceId)

        /* API 연동 시 위 한 줄을 아래로 교체
        viewModelScope.launch {
            resultResponse(
                response = postAttendanceApprovalUseCase(attendanceId),
                successCallback = { updateSessionAfterApproval(attendanceId) },
                errorCallback = { failState -> emitEvent(AdminCheckEvent.ShowToast(failState.message)) }
            )
        }
        */
    }

    fun rejectAttendance(attendanceId: Long) {
        // Repository 변경 → StateFlow → UI 자동 반영
        dummyRepo.updateAdminSessionAfterRejection(attendanceId)

        /* API 연동 시 위 한 줄을 아래로 교체
        viewModelScope.launch {
            resultResponse(
                response = postAttendanceRejectionUseCase(attendanceId),
                successCallback = { updateSessionAfterRejection(attendanceId) },
                errorCallback = { failState -> emitEvent(AdminCheckEvent.ShowToast(failState.message)) }
            )
        }
        */
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
                } else uiModel
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