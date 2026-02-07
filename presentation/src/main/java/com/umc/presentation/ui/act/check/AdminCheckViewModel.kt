package com.umc.presentation.ui.act.check

import androidx.lifecycle.viewModelScope
import com.umc.domain.model.act.check.AdminPendingUser
import com.umc.domain.model.act.check.AdminSessionCheck
import com.umc.domain.model.base.ApiState
import com.umc.domain.usecase.schedule.GetAdminSessionListUseCase
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminCheckViewModel @Inject constructor(
    private val getAdminSessionListUseCase: GetAdminSessionListUseCase
) : BaseViewModel<AdminCheckUiState, AdminCheckEvent>(AdminCheckUiState()) {

    init {
        fetchAdminSessions()
    }

    fun fetchAdminSessions() {
        viewModelScope.launch {
            when (val result = getAdminSessionListUseCase()) {
                is ApiState.Success -> {
                    val uiModels = result.data.map { domainModel ->
                        AdminSessionUIModel(session = domainModel)
                    }
                    updateState { copy(adminSessions = uiModels) }
                }
                is ApiState.Fail -> {
                    emitEvent(AdminCheckEvent.ShowToast(result.failState.message))
                }
            }
        }
    }

    /**
     * 출석 승인 버튼 클릭 시 호출
     */
    fun approveAttendance(user: AdminPendingUser) {
        // TODO: 출석 승인 API 호출
    }

    /**
     * 출석 반려 버튼 클릭 시 호출
     */
    fun rejectAttendance(user: AdminPendingUser) {
        // TODO: 출석 반려 API 호출
    }

    /**
     * 위치 변경 버튼 클릭 시 호출
     */
    fun onLocationChangeClicked(sessionId: Int) {
        emitEvent(AdminCheckEvent.ShowLocationDialog(
            sessionId = sessionId,
            lat = 37.582568,
            lng = 127.001488,
            address = "서울특별시 종로구 명륜4가"
        ))
    }

    /**
     * 다이얼로그에서 변경 완료 시 호출
     */
    fun updateSessionLocation(sessionId: Int, lat: Double, lng: Double, address: String) {
        // TODO: 서버 위치 업데이트 API 호출
        emitEvent(AdminCheckEvent.ShowToast("출석 위치가 성공적으로 변경되었습니다."))
    }

    fun toggleSessionExpansion(sessionId: Int) {
        updateState {
            val newList = adminSessions.map { uiModel ->
                if (uiModel.session.id == sessionId) {
                    uiModel.copy(isExpanded = !uiModel.isExpanded)
                } else {
                    uiModel
                }
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
    data class ShowLocationDialog(
        val sessionId: Int,
        val lat: Double,
        val lng: Double,
        val address: String
    ) : AdminCheckEvent()
}