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
    private val updateScheduleLocationUseCase: UpdateScheduleLocationUseCase
) : BaseViewModel<AdminCheckUiState, AdminCheckEvent>(AdminCheckUiState()) {

    init {
        fetchAdminSessions()
    }

    fun fetchAdminSessions() {
        viewModelScope.launch {
            resultResponse(
                response = getAdminSessionListUseCase(),
                successCallback = { data ->
                    val uiModels = data.map { domainModel ->
                        AdminSessionUIModel(session = domainModel)
                    }
                    updateState { copy(adminSessions = uiModels) }
                },
                errorCallback = { failState -> emitEvent(AdminCheckEvent.ShowToast(failState.message)) }
            )
        }
    }

    /**
     * 위치 변경 API 호출
     */
    fun updateSessionLocation(sessionId: Long, lat: Double, lng: Double, address: String) {
        viewModelScope.launch {
            resultResponse(
                response = updateScheduleLocationUseCase(sessionId, address, lat, lng),
                successCallback = {
                    emitEvent(AdminCheckEvent.ShowToast("출석 위치가 성공적으로 변경되었습니다."))
                    fetchAdminSessions() // 변경 후 데이터 갱신
                },
                errorCallback = { failState ->
                    emitEvent(AdminCheckEvent.ShowToast(failState.message))
                }
            )
        }
    }

}

data class AdminCheckUiState(
    val adminSessions: List<AdminSessionUIModel> = emptyList()
) : UiState

sealed class AdminCheckEvent : UiEvent {
    data class ShowToast(val message: String) : AdminCheckEvent()
}