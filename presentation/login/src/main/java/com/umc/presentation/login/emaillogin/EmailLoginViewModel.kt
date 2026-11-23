package com.umc.presentation.login.emaillogin

import androidx.lifecycle.viewModelScope
import com.umc.component.base.BaseViewModel
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.umc.domain.model.JwtToken
import com.umc.domain.model.base.isNetworkError
import com.umc.domain.usecase.PostEmailLoginUseCase
import com.umc.domain.usecase.appDataStore.SaveTokenUseCase
import com.umc.domain.usecase.member.GetMyProfileUseCase
import com.umc.component.fcm.FcmTokenSynchronizer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EmailLoginViewModel @Inject constructor(
    private val postEmailLoginUseCase: PostEmailLoginUseCase,
    private val saveTokenUseCase: SaveTokenUseCase,
    private val tokenSynchronizer: FcmTokenSynchronizer,
    private val getMyProfileUseCase: GetMyProfileUseCase,
) : BaseViewModel<EmailLoginState, EmailLoginEvent>(
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

    // 이메일 로그인 시도. 요청이 끝나기 전에는 다시 호출돼도 무시해 연타해도 요청이 한 번만 나간다
    fun login() = viewModelScope.launch {
        if (uiState.value.isSubmitting) return@launch
        updateState { copy(isSubmitting = true) }
        startLoading()
        resultResponse(
            response = postEmailLoginUseCase(
                email = uiState.value.email,
                password = uiState.value.password,
            ),
            successCallback = {
                saveToken(it)
            },
            errorCallback = { failState ->
                // 네트워크 단절은 계정 오류가 아니므로 입력 필드 에러 상태 없이 토스트만 띄운다.
                // 계정 정보 불일치 → 입력 필드 에러 상태 표시 + 서버 에러 메시지 토스트
                if (!failState.isNetworkError) {
                    updateState { copy(isLoginFailed = true) }
                }
                updateState { copy(isSubmitting = false) }
                emitEvent(EmailLoginEvent.ShowErrorToast(failState.message))
            }
        )
    }

    // JWT 토큰 저장
    private fun saveToken(request: JwtToken) = viewModelScope.launch {
        resultResponse(
            response = saveTokenUseCase(request),
            successCallback = {
                checkUserChallengerRecord()
            },
            errorCallback = { failState ->
                updateState { copy(isSubmitting = false) }
                emitEvent(EmailLoginEvent.ShowErrorToast(failState.message))
            }
        )
    }

    // 내 정보 먼저 가져와서 메인 진입 가능 여부(챌린저 기록·프로덕트팀 계정) 확인
    private fun checkUserChallengerRecord() = viewModelScope.launch {
        resultResponse(
            response = getMyProfileUseCase(),
            successCallback = { userInfo ->
                if (userInfo.canEnterMain()) {
                    registerFcmToken()
                } else {
                    // 코드 입력 화면에서 뒤로 돌아올 수 있어 버튼 잠금을 풀고 이동한다
                    updateState { copy(isSubmitting = false) }
                    // 챌린저 코드 입력 화면으로 이동
                    emitEvent(EmailLoginEvent.MoveToInputCodeEvent)
                }
            },
            errorCallback = {
                updateState { copy(isSubmitting = false) }
                emitEvent(EmailLoginEvent.ShowErrorToast(it.message))
            }
        )
    }

    // FCM 토큰 등록
    private fun registerFcmToken() {
        tokenSynchronizer.refresh()
        emitEvent(EmailLoginEvent.MoveToMainEvent)
    }
}

data class EmailLoginState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoginFailed: Boolean = false,
    // 로그인 요청부터 화면 이동까지 진행 중인지. 메인으로 이동하는 성공 경로에서는 화면을 떠나므로 풀지 않는다
    val isSubmitting: Boolean = false,
) : UiState {
    val enableLoginButton: Boolean
        get() = email.isNotEmpty() && password.isNotEmpty() && !isSubmitting
}

sealed interface EmailLoginEvent : UiEvent {

    object MoveToMainEvent : EmailLoginEvent

    object MoveToInputCodeEvent : EmailLoginEvent

    data class ShowErrorToast(val message: String) : EmailLoginEvent
}
