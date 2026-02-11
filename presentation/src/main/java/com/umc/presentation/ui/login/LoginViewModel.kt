package com.umc.presentation.ui.login

import androidx.lifecycle.viewModelScope
import com.umc.domain.model.JwtToken
import com.umc.domain.model.enums.LoginType
import com.umc.domain.usecase.PostLoginUseCase
import com.umc.domain.usecase.appDataStore.SaveTokenUseCase
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
    private val postLoginUseCase: PostLoginUseCase,
    private val saveTokenUseCase: SaveTokenUseCase
) : BaseViewModel<UiState, LoginEvent>(
    UiState.Default,
) {
    fun onClickKakaoLogin() {
        emitEvent(LoginEvent.KakaoLoginEvent)
    }

    fun login(token: String, loginType: LoginType) = viewModelScope.launch {
        resultResponse(
            response = postLoginUseCase(loginType = loginType, token = token),
            successCallback = {
                if (it.oAuthVerificationToken.isNotEmpty()) emitEvent(LoginEvent.MoveToSignUpEvent(it.oAuthVerificationToken))
                else {
                    saveToken(it)
                }
            },
        )
    }

    private fun saveToken(request: JwtToken) = viewModelScope.launch {
        resultResponse(
            response = saveTokenUseCase(request),
            successCallback = {
                emitEvent(LoginEvent.MoveToMainEvent)
            }
        )
    }

    fun onClickGoogle() {
        emitEvent(LoginEvent.GoogleLoginEvent)
    }


}

sealed interface LoginEvent : UiEvent {
    object KakaoLoginEvent : LoginEvent
    object GoogleLoginEvent: LoginEvent

    object MoveToMainEvent : LoginEvent

    data class MoveToSignUpEvent(val oAuthToken: String) : LoginEvent
}
