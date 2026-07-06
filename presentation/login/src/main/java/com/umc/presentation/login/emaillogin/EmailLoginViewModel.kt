package com.umc.presentation.login.emaillogin

import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import com.umc.component.base.BaseViewModel
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.umc.domain.model.JwtToken
import com.umc.domain.model.UserInfo
import com.umc.domain.usecase.PostEmailLoginUseCase
import com.umc.domain.usecase.appDataStore.SaveTokenUseCase
import com.umc.domain.usecase.member.GetMyProfileUseCase
import com.umc.domain.usecase.notification.RegisterFcmTokenUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class EmailLoginViewModel @Inject constructor(
    private val postEmailLoginUseCase: PostEmailLoginUseCase,
    private val saveTokenUseCase: SaveTokenUseCase,
    private val registerFcmTokenUseCase: RegisterFcmTokenUseCase,
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

    // 이메일 로그인 시도
    fun login() = viewModelScope.launch {
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
                // 계정 정보 불일치 → 입력 필드 에러 상태 표시 + 서버 에러 메시지 토스트
                updateState { copy(isLoginFailed = true) }
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
                emitEvent(EmailLoginEvent.ShowErrorToast(failState.message))
            }
        )
    }

    // 내 정보 먼저 가져와서 해당 데이터로 챌린저 ID 확인
    private fun checkUserChallengerRecord() = viewModelScope.launch {
        resultResponse(
            response = getMyProfileUseCase(),
            successCallback = { userInfo ->
                if (hasChallengerId(userInfo)) {
                    registerFcmToken()
                } else {
                    // 챌린저 코드 입력 화면으로 이동
                    emitEvent(EmailLoginEvent.MoveToInputCodeEvent)
                }
            },
            errorCallback = {
                emitEvent(EmailLoginEvent.ShowErrorToast(it.message))
            }
        )
    }

    // 챌린저 ID 확인
    private fun hasChallengerId(userInfo: UserInfo): Boolean {
        val hasRoleChallengerId = userInfo.roles.any { it.challengerId > 0 }
        val hasRecordChallengerId = userInfo.challengerRecords.any { it.challengerId > 0 }
        return hasRoleChallengerId || hasRecordChallengerId
    }

    // FCM 토큰 등록
    private fun registerFcmToken() = viewModelScope.launch {
        try {
            val fcmToken = FirebaseMessaging.getInstance().token.await()
            resultResponse(
                response = registerFcmTokenUseCase(fcmToken),
                successCallback = {
                    emitEvent(EmailLoginEvent.MoveToMainEvent)
                },
                errorCallback = {
                    emitEvent(EmailLoginEvent.MoveToMainEvent)
                }
            )
        } catch (e: Exception) {
            emitEvent(EmailLoginEvent.MoveToMainEvent)
        }
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

    object MoveToMainEvent : EmailLoginEvent

    object MoveToInputCodeEvent : EmailLoginEvent

    data class ShowErrorToast(val message: String) : EmailLoginEvent
}
