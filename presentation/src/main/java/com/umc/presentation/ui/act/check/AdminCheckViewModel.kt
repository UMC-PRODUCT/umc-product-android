package com.umc.presentation.ui.act.check

import androidx.lifecycle.viewModelScope
import com.umc.domain.model.act.check.AdminPendingUser
import com.umc.domain.model.act.check.AdminSessionCheck
import com.umc.domain.model.base.ApiState
import com.umc.domain.usecase.attendance.GetPendingUsersUseCase
import com.umc.domain.usecase.attendance.PostAttendanceApprovalUseCase
import com.umc.domain.usecase.attendance.PostAttendanceRejectionUseCase
import com.umc.domain.usecase.schedule.GetAdminSessionListUseCase
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
    private val postAttendanceRejectionUseCase: PostAttendanceRejectionUseCase
) : BaseViewModel<AdminCheckUiState, AdminCheckEvent>(AdminCheckUiState()) {
    init {
        fetchAdminSessions()
    }

    fun fetchAdminSessions() {
        viewModelScope.launch {
            when (val result = getAdminSessionListUseCase()) {
                is ApiState.Success -> {
                    val currentSessions = uiState.value.adminSessions
                    val uiModels = result.data.map { domainModel ->
                        val wasExpanded = currentSessions.find { it.session.id == domainModel.id }?.isExpanded ?: false
                        AdminSessionUIModel(session = domainModel, isExpanded = wasExpanded)
                    }
                    updateState { copy(adminSessions = uiModels) }
                }
                is ApiState.Fail -> emitEvent(AdminCheckEvent.ShowToast(result.failState.message))
            }
        }
    }

    /**
     * 출석 승인 버튼 클릭 시 호출
     */
    fun approveAttendance(attendanceId: Int) {
        viewModelScope.launch {
            android.util.Log.d("AdminCheck", "승인 시작: attendanceId=$attendanceId")

            when (val result = postAttendanceApprovalUseCase(attendanceId)) {
                is ApiState.Success -> {
                    android.util.Log.d("AdminCheck", "승인 성공")

                    // 전체 목록 재호출
                    fetchAdminSessions()

                    // 펼쳐진 세션들 모두 새로고침
                    val expandedSessionIds = uiState.value.adminSessions
                        .filter { it.isExpanded }
                        .map { it.session.id }

                    expandedSessionIds.forEach { sessionId ->
                        fetchPendingUsers(sessionId)
                    }

                    emitEvent(AdminCheckEvent.ShowToast("출석이 승인되었습니다."))
                }
                is ApiState.Fail -> {
                    android.util.Log.e("AdminCheck", "승인 실패: ${result.failState.message}")
                    emitEvent(AdminCheckEvent.ShowToast(result.failState.message ?: "승인에 실패했습니다."))
                }
            }
        }
    }

    /**
     * 출석 반려 버튼 클릭 시 호출
     */
    fun rejectAttendance(attendanceId: Int) {
        viewModelScope.launch {
            android.util.Log.d("AdminCheck", "반려 시작: attendanceId=$attendanceId")

            when (val result = postAttendanceRejectionUseCase(attendanceId)) {
                is ApiState.Success -> {
                    android.util.Log.d("AdminCheck", "반려 성공")

                    // 전체 목록 재호출
                    fetchAdminSessions()

                    // 펼쳐진 세션들 모두 새로고침
                    val expandedSessionIds = uiState.value.adminSessions
                        .filter { it.isExpanded }
                        .map { it.session.id }

                    expandedSessionIds.forEach { sessionId ->
                        fetchPendingUsers(sessionId)
                    }

                    emitEvent(AdminCheckEvent.ShowToast("출석이 반려되었습니다."))
                }
                is ApiState.Fail -> {
                    android.util.Log.e("AdminCheck", "반려 실패: ${result.failState.message}")
                    emitEvent(AdminCheckEvent.ShowToast(result.failState.message ?: "반려에 실패했습니다."))
                }
            }
        }
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
        val currentSession = uiState.value.adminSessions.find { it.session.id == sessionId }

        if (currentSession?.isExpanded == false && currentSession.session.pendingUsers.isEmpty()) {
            fetchPendingUsers(sessionId)
        }

        updateExpansionState(sessionId)
    }

    private fun fetchPendingUsers(sessionId: Int) {
        viewModelScope.launch {
            when (val result = getPendingUsersUseCase(sessionId)) {
                is ApiState.Success -> {
                    updateState {
                        val updatedList = adminSessions.map { uiModel ->
                            if (uiModel.session.id == sessionId) {
                                uiModel.copy(session = uiModel.session.copy(pendingUsers = result.data))
                            } else uiModel
                        }
                        copy(adminSessions = updatedList)
                    }
                }
                is ApiState.Fail -> emitEvent(AdminCheckEvent.ShowToast(result.failState.message))
            }
        }
    }

    private fun updateExpansionState(sessionId: Int) {
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