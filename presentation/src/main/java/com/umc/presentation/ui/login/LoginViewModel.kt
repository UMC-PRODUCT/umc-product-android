package com.umc.presentation.ui.login

import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import com.umc.domain.model.JwtToken
import com.umc.domain.model.UserInfo
import com.umc.domain.model.enums.LoginType
import com.umc.domain.usecase.PostLoginUseCase
import com.umc.domain.usecase.appDataStore.SaveTokenUseCase
import com.umc.domain.usecase.member.GetMyProfileUseCase
import com.umc.domain.usecase.notification.RegisterFcmTokenUseCase
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import com.umc.presentation.util.ULog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class LoginViewModel
@Inject
constructor(
    private val postLoginUseCase: PostLoginUseCase,
    private val saveTokenUseCase: SaveTokenUseCase,
    private val registerFcmTokenUseCase: RegisterFcmTokenUseCase,
    private val getMyProfileUseCase: GetMyProfileUseCase
) : BaseViewModel<UiState, LoginEvent>(
    UiState.Default,
) {
    fun onClickKakaoLogin() {
        emitEvent(LoginEvent.KakaoLoginEvent)
    }

    fun login(token: String, loginType: LoginType) = viewModelScope.launch {
        startLoading()
        resultResponse(
            response = postLoginUseCase(loginType = loginType, token = token),
            successCallback = {
                if (it.oAuthVerificationToken.isNotEmpty()) emitEvent(LoginEvent.MoveToSignUpEvent(it.oAuthVerificationToken))
                else {
                    saveToken(it)
                }
            },
            errorCallback = { failState ->
                emitEvent(LoginEvent.ShowErrorToast(failState.message))
            }
        )
    }

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

    private fun checkUserChallengerRecord() = viewModelScope.launch {
        resultResponse(
            response = getMyProfileUseCase(),
            successCallback = { userInfo ->
                if (hasChallengerId(userInfo)) {
                    registerFcmToken()
                } else {
                    emitEvent(LoginEvent.MoveToSignUpFailEvent)
                }
            },
            errorCallback = {
                emitEvent(LoginEvent.ShowErrorToast("사용자 정보를 불러오는데 실패했습니다."))
            }
        )
    }

    private fun hasChallengerId(userInfo: UserInfo): Boolean {
        val hasRoleChallengerId = userInfo.roles.any { it.challengerId > 0 }
        val hasRecordChallengerId = userInfo.challengerRecords.any { it.challengerId > 0 }
        return hasRoleChallengerId || hasRecordChallengerId
    }

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
            ULog.d("FCM 토큰 획득 실패: ${e.message}")
            emitEvent(LoginEvent.MoveToMainEvent)
        }
    }

    fun onClickGoogle() {
        emitEvent(LoginEvent.GoogleLoginEvent)
    }


}

sealed interface LoginEvent : UiEvent {
    object KakaoLoginEvent : LoginEvent
    object GoogleLoginEvent: LoginEvent

    object MoveToMainEvent : LoginEvent

    object MoveToSignUpFailEvent : LoginEvent

    data class MoveToSignUpEvent(val oAuthToken: String) : LoginEvent

    data class ShowErrorToast(val message: String) : LoginEvent
}
