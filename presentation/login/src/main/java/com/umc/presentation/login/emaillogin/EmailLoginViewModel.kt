package com.umc.presentation.login.emaillogin

import com.umc.component.base.BaseViewModel
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EmailLoginViewModel @Inject constructor() : BaseViewModel<EmailLoginState, EmailLoginEvent>(
    EmailLoginState(),
) {
    // 이메일 입력 (재입력 시 로그인 실패 상태 해제)
    fun updateEmail(email: String) = updateState {
        copy(email = email, isLoginFailed = false)
    }

    // 비밀번호 입력 (재입력 시 로그인 실패 상태 해제)
    fun updatePassword(password: String) = updateState {
        copy(password = password, isLoginFailed = false)
    }

    // 비밀번호 표시/숨김 전환
    fun togglePasswordVisible() = updateState {
        copy(isPasswordVisible = !isPasswordVisible)
    }

    // 이메일 로그인 시도
    fun login() {
        // TODO: 이메일 로그인 API 연결 (실패 시 updateState { copy(isLoginFailed = true) })
    }
}

data class EmailLoginState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoginFailed: Boolean = false,
) : UiState {
    val enableLoginButton: Boolean
        get() = email.isNotEmpty() && password.isNotEmpty()
}

sealed interface EmailLoginEvent : UiEvent {
    // TODO: 이메일 로그인 API 연결 후 이동/에러 이벤트 정의
}
