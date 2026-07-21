package com.umc.failcode

import androidx.lifecycle.viewModelScope
import com.kakao.sdk.auth.TokenManagerProvider
import com.umc.component.base.BaseViewModel
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.umc.domain.usecase.appDataStore.ClearAllDataUseCase
import com.umc.domain.usecase.appDataStore.ClearTokensUseCase
import com.umc.domain.usecase.member.DeleteUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpFailViewModel @Inject constructor(
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

    /** 인증코드 입력 화면으로 이동 */
    fun onClickNext() {
        emitEvent(SignUpFailEvent.MoveToCode)
    }

    fun onClickKakaoInquiry() {
        emitEvent(SignUpFailEvent.MoveToKakaoInquiry)
    }

    /** 카카오 토큰 초기화 후 로그인 화면으로 이동 */
    fun onClickLogout() = viewModelScope.launch {
        clearTokensUseCase()
        emitEvent(SignUpFailEvent.MoveToLogin)
    }

    /**
     * 회원탈퇴. 카카오·구글 토큰으로 서버 탈퇴 API 호출 후 로컬 데이터를 전체 초기화.
     * googleToken은 화면 진입 시 Route에서 Google Identity API를 통해 주입됨
     */
    fun deleteUser() = viewModelScope.launch {
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
            errorCallback = {}
        )
    }

    /** SignUpFailRoute 진입 시 Google Identity API로 획득한 accessToken을 주입 */
    fun setGoogleToken(token: String) {
        updateState { copy(googleToken = token) }
    }
}

data class SignUpFailUiState(
    val googleToken: String = "",
) : UiState

sealed interface SignUpFailEvent : UiEvent {
    data object MoveToBack : SignUpFailEvent
    data object MoveToHomePage : SignUpFailEvent
    data object MoveToCode : SignUpFailEvent
    data object MoveToKakaoInquiry : SignUpFailEvent
    data object MoveToLogin : SignUpFailEvent
}
