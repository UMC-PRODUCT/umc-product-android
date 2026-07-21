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
    private val getPendingUsersUseCase: GetPendingUsersUseCase, //승인 대기 유저 조회
    private val postAttendanceApprovalUseCase: PostAttendanceApprovalUseCase, //출석 승인
    private val postAttendanceRejectionUseCase: PostAttendanceRejectionUseCase, //출석 거절
) : BaseViewModel<PendingListUiState, PendingListEvent>(
    PendingListUiState()
) {
    //세션별 승인 대기 유저 목록 조회
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

    //단일 유저 출석 승인
    fun approve(user: AdminPendingUser) {
        approveAll(listOf(user.id))
    }

    //선택된 유저 출석 일괄 승인
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

    //단일 유저 출석 거절
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
    //현재 세션 ID
    val scheduleId: Long = 0L,
    //승인 대기 유저 목록
    val users: List<AdminPendingUser> = emptyList(),
) : UiState

sealed interface PendingListEvent : UiEvent {
    //출석 승인 성공
    data object ApproveSuccess : PendingListEvent
    //출석 거절 성공
    data object RejectSuccess : PendingListEvent
    //토스트 표시
    data class ShowToast(val message: String) : PendingListEvent
}
