package com.umc.presentation.ui.act.check

import androidx.lifecycle.viewModelScope
import com.umc.domain.model.act.check.UserCheckHistory
import com.umc.domain.model.act.check.UserCheckAvailable
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.enums.CheckAvailableStatus
import com.umc.domain.model.enums.CheckHistoryStatus
import com.umc.domain.usecase.attendance.GetAttendanceAvailableUseCase
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserCheckViewModel @Inject constructor(
    private val getAttendanceAvailableUseCase: GetAttendanceAvailableUseCase //
) : BaseViewModel<UserCheckUiState, UserCheckEvent>(UserCheckUiState()) {

    init {
        fetchAvailableAttendance()
    }

    fun submitAttendanceReason(sessionId: Int, reason: String) {
        // TODO: API 전송 로직 구현
        emitEvent(UserCheckEvent.ShowToast("사유가 성공적으로 제출되었습니다."))
    }

    private fun fetchAvailableAttendance() {
        viewModelScope.launch {
            when (val result = getAttendanceAvailableUseCase()) {
                is ApiState.Success -> {
                    val uiModels = result.data.map { session ->
                        CheckAvailableUIModel(
                            session = session,
                            address = session.address
                        )
                    }

                    updateState {
                        copy(
                            availableSessions = uiModels,
                            availableCount = uiModels.size
                        )
                    }
                }
                is ApiState.Fail -> {
                    emitEvent(UserCheckEvent.ShowToast(result.failState.message))
                }
            }
        }
    }

    fun updateLocation(userLat: Double, userLng: Double) {
        updateState {
            val updatedList = availableSessions.map { uiModel ->
                if (uiModel.session.status == CheckAvailableStatus.BEFORE) {
                    val results = FloatArray(1)
                    android.location.Location.distanceBetween(
                        userLat, userLng,
                        uiModel.session.latitude, uiModel.session.longitude,
                        results
                    )
                    uiModel.copy(isWithinRange = results[0] <= geofenceRadius)
                } else {
                    uiModel.copy(isWithinRange = false)
                }
            }
            copy(availableSessions = updatedList)
        }
    }

    /**
     * 특정 아이템을 클릭했을 때 확장 상태를 토글
     */
    fun toggleSessionExpansion(sessionId: Int) {
        updateState {
            val newList = availableSessions.map { uiModel ->
                if (uiModel.session.id == sessionId) {
                    uiModel.copy(isExpanded = !uiModel.isExpanded)
                } else {
                    uiModel.copy(isExpanded = false)
                }
            }
            copy(availableSessions = newList)
        }
    }
}

data class UserCheckUiState(
    val availableSessions: List<CheckAvailableUIModel> = emptyList(),
    val attendanceHistories: List<CheckHistoryUIModel> = emptyList(),
    val availableCount: Int = 0,
    val geofenceRadius: Float = 50f
) : UiState
sealed class UserCheckEvent : UiEvent {
    data class ShowToast(val message: String) : UserCheckEvent()
    data class ShowReasonDialog(val sessionId: Int) : UserCheckEvent()
    data class NavigateToFailureReason(val sessionId: Int) : UserCheckEvent()
}