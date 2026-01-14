package com.umc.presentation.ui.signUp.permission

import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PermissionViewModel
@Inject
constructor() : BaseViewModel<PermissionUiState, PermissionEvent>(
    PermissionUiState(),
) {

    fun onClickAlarmCheck() {
        updateState { copy(isAlarmCheck = !isAlarmCheck) }
    }

    fun onClickLocationCheck() {
        updateState { copy(isLocationCheck = !isLocationCheck) }
    }

    fun onClickPhotoCheck() {
        updateState { copy(isPhotoCheck = !isPhotoCheck) }
    }

    fun onClickBack() {
        emitEvent(PermissionEvent.MoveToBack)
    }

    fun onClickAllCheck() {
        if (uiState.value.isAlarmCheck && uiState.value.isLocationCheck && uiState.value.isPhotoCheck) {
            updateState {
                copy(
                    isAlarmCheck = false,
                    isLocationCheck = false,
                    isPhotoCheck = false
                )
            }
        } else {
            updateState {
                copy(
                    isAlarmCheck = true,
                    isLocationCheck = true,
                    isPhotoCheck = true
                )
            }
        }
    }

    fun onClickSignUp() {
        emitEvent(PermissionEvent.ShowPermissionDialog)
    }

    fun signUp() {
        //TODO 서버 통신 성공 시
        emitEvent(PermissionEvent.MoveToMainEvent)

        //TODO 실패 시
        //emitEvent(PermissionEvent.MoveToFailEvent)
    }

}

data class PermissionUiState(
    val isAlarmCheck: Boolean = false,
    val isLocationCheck: Boolean = false,
    val isPhotoCheck: Boolean = false,
) : UiState

sealed interface PermissionEvent : UiEvent {
    object MoveToBack : PermissionEvent
    object MoveToMainEvent : PermissionEvent
    object MoveToFailEvent : PermissionEvent
    object ShowPermissionDialog : PermissionEvent

}
