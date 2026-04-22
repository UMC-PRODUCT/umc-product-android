package com.umc.presentation.login

import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import com.umc.component.base.BaseViewModel
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.umc.domain.model.JwtToken
import com.umc.domain.model.UserInfo
import com.umc.domain.model.enums.LoginType
import com.umc.domain.usecase.PostLoginUseCase
import com.umc.domain.usecase.appDataStore.SaveTokenUseCase
import com.umc.domain.usecase.member.GetMyProfileUseCase
import com.umc.domain.usecase.notification.RegisterFcmTokenUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val postLoginUseCase: PostLoginUseCase,
    private val saveTokenUseCase: SaveTokenUseCase,
    private val registerFcmTokenUseCase: RegisterFcmTokenUseCase,
    private val getMyProfileUseCase: GetMyProfileUseCase
) : BaseViewModel<UiState, LoginEvent>(
    UiState.Default,
) {
    // 로그인 시도
    fun login(token: String, loginType: LoginType) = viewModelScope.launch {
        startLoading()
        resultResponse(
            response = postLoginUseCase(loginType = loginType, token = token),
            successCallback = {
                // 만약 oAuthVerificationToken (회원가입용 토큰)이 발급되었다면
                if (it.oAuthVerificationToken.isNotEmpty()) {
                    // 회원가입으로 이동
                    emitEvent(LoginEvent.MoveToSignUpEvent(it.oAuthVerificationToken))
                }
                else {
                    // 아니면 로그인 성공 로직
                    saveToken(it)
                }
            },
            errorCallback = { failState ->
                emitEvent(LoginEvent.ShowErrorToast(failState.message))
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
                emitEvent(LoginEvent.ShowErrorToast(failState.message))
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
                    emitEvent(LoginEvent.MoveToInputCodeEvent)
                }
            },
            errorCallback = {
                emitEvent(LoginEvent.ShowErrorToast(it.message))
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
                    emitEvent(LoginEvent.MoveToMainEvent)
                },
                errorCallback = {
                    emitEvent(LoginEvent.MoveToMainEvent)
                }
            )
        } catch (e: Exception) {
            emitEvent(LoginEvent.MoveToMainEvent)
        }
    }
}

sealed interface LoginEvent : UiEvent {

    object MoveToMainEvent : LoginEvent

    object MoveToInputCodeEvent : LoginEvent

    data class MoveToSignUpEvent(val oAuthToken: String) : LoginEvent

    data class ShowErrorToast(val message: String) : LoginEvent
}