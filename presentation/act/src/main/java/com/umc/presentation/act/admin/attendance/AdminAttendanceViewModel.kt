package com.umc.presentation.act.admin.attendance

import androidx.lifecycle.viewModelScope
import com.umc.component.base.BaseViewModel
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.umc.domain.model.act.check.AdminSessionCheck
import com.umc.domain.usecase.schedule.DeleteScheduleUseCase
import com.umc.domain.usecase.schedule.ForceDeleteScheduleUseCase
import com.umc.domain.usecase.schedule.GetAdminSessionListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class AdminAttendanceViewModel @Inject constructor(
    private val getAdminSessionListUseCase: GetAdminSessionListUseCase, //관리자 세션 목록 조회
    private val deleteScheduleUseCase: DeleteScheduleUseCase,
    private val forceDeleteScheduleUseCase: ForceDeleteScheduleUseCase,
) : BaseViewModel<AdminAttendanceUiState, AdminAttendanceEvent>(
    AdminAttendanceUiState()
) {
    //초기 관리자 세션 목록 조회
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

    fun requestDeleteSession(scheduleId: Long) {
        if (scheduleId <= 0L) return
        updateState {
            copy(
                deleteTargetId = scheduleId,
                forceDeleteTargetId = null
            )
        }
    }

    fun dismissDeleteSession() {
        updateState { copy(deleteTargetId = null) }
    }

    fun openPendingList(scheduleId: Long) {
        if (scheduleId <= 0L) return
        updateState { copy(pendingListScheduleId = scheduleId) }
    }

    fun dismissPendingList() {
        updateState { copy(pendingListScheduleId = null) }
        getSessions()
    }

    fun deleteSelectedSession() {
        val scheduleId = uiState.value.deleteTargetId ?: return

        viewModelScope.launch {
            startLoading()
            resultResponse(
                response = deleteScheduleUseCase(scheduleId),
                successCallback = {
                    updateState {
                        copy(
                            sessions = sessions.filterNot { it.id == scheduleId },
                            deleteTargetId = null
                        )
                    }
                    emitEvent(AdminAttendanceEvent.DeleteSuccess)
                },
                errorCallback = { failState ->
                    if (failState.code == FORCE_DELETE_REQUIRED_ERROR_CODE) {
                        updateState {
                            copy(
                                deleteTargetId = null,
                                forceDeleteTargetId = scheduleId
                            )
                        }
                    } else {
                        updateState { copy(deleteTargetId = null) }
                        emitEvent(AdminAttendanceEvent.ShowToast(failState.message))
                    }
                }
            )
        }
    }

    fun dismissForceDeleteSession() {
        updateState { copy(forceDeleteTargetId = null) }
    }

    fun forceDeleteSelectedSession() {
        val scheduleId = uiState.value.forceDeleteTargetId ?: return

        viewModelScope.launch {
            startLoading()
            resultResponse(
                response = forceDeleteScheduleUseCase(scheduleId),
                successCallback = {
                    updateState {
                        copy(
                            sessions = sessions.filterNot { it.id == scheduleId },
                            forceDeleteTargetId = null
                        )
                    }
                    emitEvent(AdminAttendanceEvent.DeleteSuccess)
                },
                errorCallback = { failState ->
                    updateState { copy(forceDeleteTargetId = null) }
                    emitEvent(AdminAttendanceEvent.ShowToast(failState.message))
                }
            )
        }
    }
}

data class AdminAttendanceUiState(
    //관리자 세션 목록
    val sessions: List<AdminSessionCheck> = emptyList(),
    val deleteTargetId: Long? = null,
    val forceDeleteTargetId: Long? = null,
    val pendingListScheduleId: Long? = null,
) : UiState {
    //세션 목록 비어있음 여부
    val isEmpty: Boolean
        get() = sessions.isEmpty()
}

sealed interface AdminAttendanceEvent : UiEvent {
    data object DeleteSuccess : AdminAttendanceEvent
    //토스트 표시
    data class ShowToast(val message: String) : AdminAttendanceEvent
}

private const val FORCE_DELETE_REQUIRED_ERROR_CODE = "SCHEDULE-0033"

fun String.toAttendanceDisplayDate(): String {
    val date = substringBefore(" ")
    val localDate = runCatching {
        LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE)
    }.recoverCatching {
        LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy.MM.dd"))
    }.getOrNull() ?: return this

    return localDate.format(
        DateTimeFormatter.ofPattern("yyyy.MM.dd (E)", Locale.KOREAN)
    )
}