package com.umc.presentation.ui.act.check

import androidx.lifecycle.viewModelScope
import com.umc.domain.model.act.check.AdminSessionCheck
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.enums.PermissionType
import com.umc.domain.model.enums.ResourceType
import com.umc.domain.usecase.GetAuthAccessUseCase
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
    private val updateScheduleLocationUseCase: UpdateScheduleLocationUseCase,
    private val getAuthAccessUseCase: GetAuthAccessUseCase
) : BaseViewModel<AdminCheckUiState, AdminCheckEvent>(AdminCheckUiState()) {

    init {
        fetchAdminSessions()
    }

    fun fetchAdminSessions() {
        viewModelScope.launch {
            resultResponse(
                response = getAdminSessionListUseCase(),
                successCallback = { data ->
                    viewModelScope.launch {
                        checkPermissionsAndUpdate(data)
                    }
                },
                errorCallback = { failState ->
                    emitEvent(AdminCheckEvent.ShowToast(failState.message, isError = true))
                }
            )
        }
    }
    /**
     * 각 세션별로 권한 체크를 병렬 실행
     */
    private suspend fun checkPermissionsAndUpdate(sessions: List<AdminSessionCheck>) {
        val uiModels = sessions.map { session ->
            val authResponse = getAuthAccessUseCase(ResourceType.ATTENDANCE_SHEET, session.sheetId)

            var hasApprove = false
            if (authResponse is ApiState.Success) {
                // permissions 리스트 중 APPROVE 타입이 있고, hasPermission이 true인지 확인
                hasApprove = authResponse.data.permissions.any {
                    it.type == PermissionType.APPROVE && it.hasPermission
                }
            }

            AdminSessionUIModel(
                session = session,
                hasApprovePermission = hasApprove
            )
        }
        updateState { copy(adminSessions = uiModels) }
    }

    /**
     * 위치 변경 API 호출
     */
    fun updateSessionLocation(sessionId: Long, lat: Double, lng: Double, address: String) {
        viewModelScope.launch {
            resultResponse(
                response = updateScheduleLocationUseCase(sessionId, address, lat, lng),
                successCallback = {
                    emitEvent(AdminCheckEvent.ShowToast("출석 위치가 성공적으로 변경되었습니다.", isError = false))
                    fetchAdminSessions()
                },
                errorCallback = { failState ->
                    emitEvent(AdminCheckEvent.ShowToast(failState.message, isError = true))
                }
            )
        }
    }

}

data class AdminCheckUiState(
    val adminSessions: List<AdminSessionUIModel> = emptyList()
) : UiState

sealed class AdminCheckEvent : UiEvent {
    data class ShowToast(val message: String, val isError: Boolean = false) : AdminCheckEvent()
}