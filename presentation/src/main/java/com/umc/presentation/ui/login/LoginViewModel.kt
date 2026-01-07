package com.umc.presentation.ui.login

import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import javax.inject.Inject

class LoginViewModel
@Inject
constructor() : BaseViewModel<LoginUiState, LoginEvent>(
    LoginUiState(),
) {
    init {

    }
}

data class LoginUiState(
    val dummy: String = "",
) : UiState

sealed class LoginEvent : UiEvent {
    object MoveToMainEvent : LoginEvent()
    object MoveToLoginEvent : LoginEvent()
}
