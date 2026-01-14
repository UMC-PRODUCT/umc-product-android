package com.umc.presentation.ui.login

import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel
    @Inject
    constructor() : BaseViewModel<LoginUiState, LoginEvent>(
            LoginUiState(),
        ) {
        fun onClickKakaoLogin() {
            emitEvent(LoginEvent.KakaoLoginEvent)
        }

        fun kakaoLogin() {
            // TODO 서버 연결 해야함
            emitEvent(LoginEvent.MoveToSignUpEvent)
        }
    }

data class LoginUiState(
    val dummy: String = "",
) : UiState

sealed interface LoginEvent : UiEvent {
    object KakaoLoginEvent : LoginEvent

    object MoveToMainEvent : LoginEvent

    object MoveToSignUpEvent : LoginEvent
}
