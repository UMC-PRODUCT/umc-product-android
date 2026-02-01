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
) : BaseViewModel<LoginUiState, LoginEvent>(
    LoginUiState(),
) {
    fun onClickKakaoLogin() {
        emitEvent(LoginEvent.KakaoLoginEvent)
    }

    fun kakaoLogin(token: String) = viewModelScope.launch {
        // TODO 서버 연결 해야함
        resultResponse(
            response = postLoginUseCase(token),
            successCallback = {
                //TODO 성공 콜백
                ULog.d("성공")
            },
            errorCallback = {
                //TODO 에러 콜백
                ULog.d("실패")
            }
        )
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
