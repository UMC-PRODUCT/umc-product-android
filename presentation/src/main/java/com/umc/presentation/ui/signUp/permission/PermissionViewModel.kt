package com.umc.presentation.ui.signUp.permission

import androidx.lifecycle.viewModelScope
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

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

}

data class PermissionUiState(
    val isAlarmCheck: Boolean = false,
    val isLocationCheck: Boolean = false,
    val isPhotoCheck: Boolean = false,
) : UiState

sealed class PermissionEvent : UiEvent {
    object MoveToMainEvent : PermissionEvent()

    object MoveToLoginEvent : PermissionEvent()
}
