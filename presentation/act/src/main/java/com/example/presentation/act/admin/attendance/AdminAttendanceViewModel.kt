package com.example.presentation.act.admin.attendance

import androidx.lifecycle.viewModelScope
import com.umc.component.base.BaseViewModel
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.umc.domain.model.act.check.AdminSessionCheck
import com.umc.domain.usecase.schedule.GetAdminSessionListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminAttendanceViewModel @Inject constructor(
    private val getAdminSessionListUseCase: GetAdminSessionListUseCase,
) : BaseViewModel<AdminAttendanceUiState, AdminAttendanceEvent>(
    AdminAttendanceUiState()
) {
    init {
        getSessions()
    }

    fun getSessions() {
        viewModelScope.launch {
            startLoading()
            resultResponse(
                response = getAdminSessionListUseCase(),
                successCallback = { sessions ->
                    updateState { copy(sessions = sessions) }
                },
                errorCallback = { failState ->
                    emitEvent(AdminAttendanceEvent.ShowToast(failState.message))
                }
            )
        }
    }
}

data class AdminAttendanceUiState(
    val sessions: List<AdminSessionCheck> = emptyList(),
) : UiState {
    val isEmpty: Boolean
        get() = sessions.isEmpty()
}

sealed interface AdminAttendanceEvent : UiEvent {
    data class ShowToast(val message: String) : AdminAttendanceEvent
}
