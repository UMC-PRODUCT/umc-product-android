package com.umc.presentation.ui.signUp.fail

import androidx.lifecycle.viewModelScope
import com.kakao.sdk.auth.TokenManagerProvider
import com.umc.domain.usecase.appDataStore.ClearAllDataUseCase
import com.umc.domain.usecase.appDataStore.ClearTokensUseCase
import com.umc.domain.usecase.member.DeleteUserUseCase
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpFailViewModel
@Inject
constructor(
    private val clearTokensUseCase: ClearTokensUseCase,
    private val clearAllDataUseCase: ClearAllDataUseCase,
    private val deleteUserUseCase: DeleteUserUseCase,
) : BaseViewModel<SignUpFailUiState, SignUpFailEvent>(
    SignUpFailUiState(),
) {
    fun onClickBack() {
        emitEvent(SignUpFailEvent.MoveToBack)
    }

    fun onClickHomePage() {
        emitEvent(SignUpFailEvent.MoveToHomePage)
    }

    fun onClickNext() {
        emitEvent(SignUpFailEvent.MoveToCode)
    }

    fun onClickKakaoInquiry() {
        emitEvent(SignUpFailEvent.MoveToKakaoInquiry)
    }

    fun onClickLogout() = viewModelScope.launch {
        clearTokensUseCase()
        emitEvent(SignUpFailEvent.MoveToLogin)
    }

    fun onClickDeleteUser() = viewModelScope.launch {
        emitEvent(SignUpFailEvent.ShowDeleteUserDialog)
    }

    fun deleteUser() = viewModelScope.launch {
        // Get Kakao token
        val kakaoToken = TokenManagerProvider.instance.manager.getToken()?.accessToken ?: ""
        val googleToken = uiState.value.googleToken

        resultResponse(
            response = deleteUserUseCase(kakaoToken, googleToken),
            successCallback = {
                viewModelScope.launch {
                    clearAllDataUseCase()
                    emitEvent(SignUpFailEvent.MoveToLogin)
                }
            },
            errorCallback = {
                // Error handling
            }
        )
    }

    fun setGoogleToken(token: String) {
        updateState {
            copy(googleToken = token)
        }
    }
}

data class SignUpFailUiState(
    val googleToken: String = ""
) : UiState

sealed interface SignUpFailEvent : UiEvent {
    object MoveToBack : SignUpFailEvent
    object MoveToHomePage : SignUpFailEvent
    object MoveToCode : SignUpFailEvent
    object MoveToKakaoInquiry : SignUpFailEvent
    object MoveToLogin : SignUpFailEvent
    object ShowDeleteUserDialog : SignUpFailEvent
}
