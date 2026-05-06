package com.umc.presentation.ui.act.check

import androidx.lifecycle.viewModelScope
import com.umc.domain.model.act.check.AdminPendingUser
import com.umc.domain.model.act.check.AttendanceDecision
import com.umc.domain.usecase.attendance.GetPendingUsersUseCase
import com.umc.domain.usecase.schedule.PostScheduleAttendanceDecideUseCase
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminPendingViewModel @Inject constructor(
    private val getPendingUsersUseCase: GetPendingUsersUseCase,
    private val postScheduleAttendanceDecideUseCase: PostScheduleAttendanceDecideUseCase
) : BaseViewModel<AdminPendingUiState, AdminPendingEvent>(AdminPendingUiState()) {

    /**
     * 승인 대기 유저 목록 조회
     */
    fun fetchPendingUsers(sessionId: Long) {
        viewModelScope.launch {
            resultResponse(
                response = getPendingUsersUseCase(sessionId),
                successCallback = { users ->
                    updateState { copy(pendingUsers = users) }
                },
                errorCallback = { fail ->
                    emitEvent(AdminPendingEvent.ShowToast(fail.message))
                }
            )
        }
    }

    /**
     * 선택 모드 토글 및 선택 리스트 초기화
     */
    fun toggleSelectionMode(enabled: Boolean) = updateState {
        copy(isSelectionMode = enabled, selectedIds = emptySet())
    }

    /**
     * 유저 선택 상태 토글 (ImageView 체크박스 대응)
     */
    fun toggleSelection(userId: Long) = updateState {
        val newSelected = selectedIds.toMutableSet().apply {
            if (contains(userId)) remove(userId) else add(userId)
        }
        copy(selectedIds = newSelected)
    }

    /**
     * 선택된 유저들 일괄 승인
     */
    fun approveSelected(sessionId: Long) {
        val state = uiState.value
        val decisions = state.pendingUsers
            .filter { it.id in state.selectedIds }
            .map { AttendanceDecision(it.memberId, isApproved = true) }
        if (decisions.isEmpty()) return

        viewModelScope.launch {
            resultResponse(
                response = postScheduleAttendanceDecideUseCase(sessionId, decisions),
                successCallback = {
                    emitEvent(AdminPendingEvent.ShowToast("${decisions.size}명의 승인이 완료되었습니다."))
                    toggleSelectionMode(false)
                    fetchPendingUsers(sessionId)
                },
                errorCallback = { fail ->
                    emitEvent(AdminPendingEvent.ShowToast(fail.message))
                }
            )
        }
    }

    /**
     * 개별 유저 승인 (attendanceId 기준으로 memberId 조회 후 결정)
     */
    fun approveUsers(sessionId: Long, recordIds: List<Long>) {
        val decisions = uiState.value.pendingUsers
            .filter { it.id in recordIds }
            .map { AttendanceDecision(it.memberId, isApproved = true) }
        if (decisions.isEmpty()) return

        viewModelScope.launch {
            resultResponse(
                response = postScheduleAttendanceDecideUseCase(sessionId, decisions),
                successCallback = {
                    emitEvent(AdminPendingEvent.ShowToast("승인이 완료되었습니다."))
                    fetchPendingUsers(sessionId)
                },
                errorCallback = { fail ->
                    emitEvent(AdminPendingEvent.ShowToast(fail.message))
                }
            )
        }
    }

    /**
     * 개별 유저 반려 (attendanceId 기준으로 memberId 조회 후 결정)
     */
    fun rejectUsers(sessionId: Long, recordIds: List<Long>) {
        val decisions = uiState.value.pendingUsers
            .filter { it.id in recordIds }
            .map { AttendanceDecision(it.memberId, isApproved = false) }
        if (decisions.isEmpty()) return

        viewModelScope.launch {
            resultResponse(
                response = postScheduleAttendanceDecideUseCase(sessionId, decisions),
                successCallback = {
                    emitEvent(AdminPendingEvent.ShowToast("반려 처리가 완료되었습니다."))
                    fetchPendingUsers(sessionId)
                },
                errorCallback = { fail ->
                    emitEvent(AdminPendingEvent.ShowToast(fail.message))
                }
            )
        }
    }
}

/**
 * UI 상태 정의
 */
data class AdminPendingUiState(
    val pendingUsers: List<AdminPendingUser> = emptyList(),
    val isSelectionMode: Boolean = false,
    val selectedIds: Set<Long> = emptySet()
) : UiState

/**
 * UI 이벤트 정의
 */
sealed interface AdminPendingEvent : UiEvent {
    data class ShowToast(val message: String) : AdminPendingEvent
}