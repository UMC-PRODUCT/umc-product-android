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
    private val getAdminSessionListUseCase: GetAdminSessionListUseCase, //관리자 세션 목록 조회
) : BaseViewModel<AdminAttendanceUiState, AdminAttendanceEvent>(
    AdminAttendanceUiState()
) {
    //초기 관리자 세션 목록 조회
    init {
        getSessions()
    }

    //관리자가 출석 관리할 세션 목록 조회
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
    //관리자 세션 목록
    val sessions: List<AdminSessionCheck> = emptyList(),
) : UiState {
    //세션 목록 비어있음 여부
    val isEmpty: Boolean
        get() = sessions.isEmpty()
}

sealed interface AdminAttendanceEvent : UiEvent {
    //토스트 표시
    data class ShowToast(val message: String) : AdminAttendanceEvent
}
