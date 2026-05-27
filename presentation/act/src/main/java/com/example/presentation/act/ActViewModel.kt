package com.example.presentation.act

import androidx.lifecycle.viewModelScope
import com.umc.component.base.BaseViewModel
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.umc.domain.model.UserInfo
import com.umc.domain.model.enums.RoleType
import com.umc.domain.usecase.member.GetMyProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActViewModel @Inject constructor(
    private val getMyProfileUseCase: GetMyProfileUseCase
) : BaseViewModel<ActUiState, ActEvent>(
    ActUiState()
) {
    init {
        getUserInfo()
    }

    fun getUserInfo() {
        viewModelScope.launch {
            resultResponse(
                response = getMyProfileUseCase(),
                successCallback = { userInfo ->
                    val hasAdminAccess = userInfo.roles.any { role ->
                        runCatching { RoleType.valueOf(role.roleType) }.isSuccess
                    }

                    updateState {
                        copy(
                            userInfo = userInfo,
                            hasAdminAccess = hasAdminAccess,
                            isAdmin = isAdmin && hasAdminAccess
                        )
                    }
                },
                errorCallback = { failState ->
                    emitEvent(ActEvent.ShowToast(failState.message))
                }
            )
        }
    }

    fun setAdminMode(isAdmin: Boolean) {
        updateState {
            copy(isAdmin = isAdmin && hasAdminAccess)
        }
    }
}

data class ActUiState(
    val userInfo: UserInfo = UserInfo(),
    val isAdmin: Boolean = false,
    val hasAdminAccess: Boolean = false,
) : UiState

sealed interface ActEvent : UiEvent {
    data class ShowToast(val message: String) : ActEvent
}
