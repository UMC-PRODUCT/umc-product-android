package com.umc.presentation.ui.login

import androidx.lifecycle.viewModelScope
import com.umc.domain.usecase.PostLoginUseCase
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import com.umc.presentation.util.ULog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel
@Inject
constructor(
    private val postLoginUseCase: PostLoginUseCase
) : BaseViewModel<UiState, LoginEvent>(
    UiState.Default,
) {
    fun onClickKakaoLogin() {
        emitEvent(LoginEvent.KakaoLoginEvent)
    }

    fun kakaoLogin(token: String) = viewModelScope.launch {
        resultResponse(
            response = postLoginUseCase(token),
            successCallback = {
                if (it.oAuthVerificationToken.isNotEmpty()) emitEvent(LoginEvent.MoveToSignUpEvent(it.oAuthVerificationToken))
                else emitEvent(LoginEvent.MoveToMainEvent)
            },
        )
    }
}

sealed interface LoginEvent : UiEvent {
    object KakaoLoginEvent : LoginEvent

    object MoveToMainEvent : LoginEvent

    data class MoveToSignUpEvent(val oAuthToken: String) : LoginEvent
}
