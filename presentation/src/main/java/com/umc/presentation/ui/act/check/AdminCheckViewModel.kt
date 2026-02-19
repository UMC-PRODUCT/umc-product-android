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
        viewModelScope.launch {
            dummyRepo.adminSessions.collect { sessions ->
                val currentExpanded = uiState.value.adminSessions.filter { it.isExpanded }.map { it.session.id }.toSet()
                val uiModels = sessions.map { AdminSessionUIModel(it, it.id in currentExpanded) }
                updateState { copy(adminSessions = uiModels) }
            }
        }
    }

    fun approveAttendance(sessionId: Long, attendanceId: Long) {
        dummyRepo.updateAdminSessionAfterApproval(sessionId, attendanceId)
    }

    fun rejectAttendance(sessionId: Long, attendanceId: Long) {
        dummyRepo.updateAdminSessionAfterRejection(sessionId, attendanceId)
    }

    fun updateSessionLocation(sessionId: Long, lat: Double, lng: Double, address: String) {
        viewModelScope.launch {
            resultResponse(
                response = updateScheduleLocationUseCase(sessionId, address, lat, lng),
                successCallback = { emitEvent(AdminCheckEvent.ShowToast("위치가 변경되었습니다.")) },
                errorCallback = { emitEvent(AdminCheckEvent.ShowToast(it.message)) }
            )
        }
    }

    fun toggleSessionExpansion(sessionId: Long) {
        val current = uiState.value.adminSessions.find { it.session.id == sessionId }
        if (current?.isExpanded == false && current.session.pendingUsers.isEmpty()) fetchPendingUsers(sessionId)
        updateExpansionState(sessionId)
    }

    private fun fetchPendingUsers(sessionId: Long) {
        viewModelScope.launch {
            resultResponse(
                response = getPendingUsersUseCase(sessionId),
                successCallback = { data ->
                    updateState {
                        val updated = adminSessions.map { if (it.session.id == sessionId) it.copy(session = it.session.copy(pendingUsers = data)) else it }
                        copy(adminSessions = updated)
                    }
                },
                errorCallback = { emitEvent(AdminCheckEvent.ShowToast(it.message)) }
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