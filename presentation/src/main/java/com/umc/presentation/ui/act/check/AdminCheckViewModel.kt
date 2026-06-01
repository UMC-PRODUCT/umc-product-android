package com.umc.presentation.ui.act.check

import androidx.lifecycle.viewModelScope
import com.umc.domain.model.act.check.AdminSessionCheck
import com.umc.domain.usecase.schedule.GetScheduleAttendanceHistoryUseCase
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminCheckViewModel @Inject constructor(
    private val getScheduleAttendanceHistoryUseCase: GetScheduleAttendanceHistoryUseCase
) : BaseViewModel<AdminCheckUiState, AdminCheckEvent>(AdminCheckUiState()) {

    init {
        fetchAdminSessions()
    }

    fun fetchAdminSessions() {
        startLoading()

        viewModelScope.launch {
            resultResponse(
                response = getScheduleAttendanceHistoryUseCase(),
                successCallback = { data ->
                    checkPermissionsAndUpdate(data)
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
    private fun checkPermissionsAndUpdate(sessions: List<AdminSessionCheck>) = viewModelScope.launch {
        val uiModels = sessions
            .filter { it.sheetId != null }
            .map { session ->
                AdminSessionUIModel(
                    session = session,
                    hasApprovePermission = true,
                    hasWritePermission = true
                )
            }

        updateState { copy(adminSessions = uiModels) }
        stopLoading()
    }

}

data class AdminCheckUiState(
    val adminSessions: List<AdminSessionUIModel> = emptyList(),
    val isLoading: Boolean = true
) : UiState

sealed class AdminCheckEvent : UiEvent {
    data class ShowToast(val message: String, val isError: Boolean = false) : AdminCheckEvent()
}