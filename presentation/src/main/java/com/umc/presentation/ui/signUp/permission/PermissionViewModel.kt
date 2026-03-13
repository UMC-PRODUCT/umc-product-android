package com.umc.presentation.ui.signUp.permission

import androidx.lifecycle.viewModelScope
import com.umc.domain.model.UserInfo
import com.umc.domain.usecase.member.GetMyProfileUseCase
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PermissionViewModel
@Inject
constructor(
    private val getMyProfileUseCase: GetMyProfileUseCase,
) : BaseViewModel<PermissionUiState, PermissionEvent>(
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

    fun signUp() = viewModelScope.launch {
        resultResponse(
            response = getMyProfileUseCase(),
            successCallback = { userInfo ->
                if (hasChallengerRecords(userInfo)) {
                    emitEvent(PermissionEvent.MoveToMainEvent)
                } else {
                    emitEvent(PermissionEvent.MoveToFailEvent)
                }
            },
            errorCallback = {
                emitEvent(PermissionEvent.MoveToFailEvent)
            }
        )
    }

    private fun hasChallengerRecords(userInfo: UserInfo): Boolean {
        val hasRoleChallengerId = userInfo.roles.any { it.challengerId > 0 }
        val hasRecordChallengerId = userInfo.challengerRecords.any { it.challengerId > 0 }
        return hasRoleChallengerId || hasRecordChallengerId
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
