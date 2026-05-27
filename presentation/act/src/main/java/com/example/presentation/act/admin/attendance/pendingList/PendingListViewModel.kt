package com.example.presentation.act.admin.attendance.pendingList

import androidx.lifecycle.viewModelScope
import com.umc.component.base.BaseViewModel
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.umc.domain.model.act.check.AdminPendingUser
import com.umc.domain.usecase.attendance.GetPendingUsersUseCase
import com.umc.domain.usecase.attendance.PostAttendanceApprovalUseCase
import com.umc.domain.usecase.attendance.PostAttendanceRejectionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PendingListViewModel @Inject constructor(
    private val getPendingUsersUseCase: GetPendingUsersUseCase,
    private val postAttendanceApprovalUseCase: PostAttendanceApprovalUseCase,
    private val postAttendanceRejectionUseCase: PostAttendanceRejectionUseCase,
) : BaseViewModel<PendingListUiState, PendingListEvent>(
    PendingListUiState()
) {
    fun getPendingUsers(scheduleId: Long) {
        updateState { copy(scheduleId = scheduleId) }
        if (scheduleId <= 0L) return

        viewModelScope.launch {
            startLoading()
            resultResponse(
                response = getPendingUsersUseCase(scheduleId),
                successCallback = { users ->
                    updateState { copy(users = users) }
                },
                errorCallback = { failState ->
                    emitEvent(PendingListEvent.ShowToast(failState.message))
                }
            )
        }
    }

    fun approve(user: AdminPendingUser) {
        approveAll(listOf(user.id))
    }

    fun approveAll(recordIds: List<Long>) {
        if (recordIds.isEmpty()) return

        viewModelScope.launch {
            startLoading()
            resultResponse(
                response = postAttendanceApprovalUseCase(recordIds),
                successCallback = {
                    updateState {
                        copy(users = users.filterNot { user -> user.id in recordIds })
                    }
                    emitEvent(PendingListEvent.ApproveSuccess)
                },
                errorCallback = { failState ->
                    emitEvent(PendingListEvent.ShowToast(failState.message))
                }
            )
        }
    }

    fun reject(user: AdminPendingUser) {
        viewModelScope.launch {
            startLoading()
            resultResponse(
                response = postAttendanceRejectionUseCase(listOf(user.id)),
                successCallback = {
                    updateState {
                        copy(users = users.filterNot { pendingUser -> pendingUser.id == user.id })
                    }
                    emitEvent(PendingListEvent.RejectSuccess)
                },
                errorCallback = { failState ->
                    emitEvent(PendingListEvent.ShowToast(failState.message))
                }
            )
        }
    }
}

data class PendingListUiState(
    val scheduleId: Long = 0L,
    val users: List<AdminPendingUser> = emptyList(),
) : UiState

sealed interface PendingListEvent : UiEvent {
    data object ApproveSuccess : PendingListEvent
    data object RejectSuccess : PendingListEvent
    data class ShowToast(val message: String) : PendingListEvent
}
