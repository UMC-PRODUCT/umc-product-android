package com.umc.presentation.signup.social

import androidx.lifecycle.viewModelScope
import com.umc.component.base.BaseViewModel
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.umc.component.util.isValidEmail
import com.umc.domain.model.enums.EmailVerifyPurpose
import com.umc.domain.model.enums.EmailVerifyType
import com.umc.domain.model.request.EmailVerificationCompleteRequest
import com.umc.domain.model.request.EmailVerificationRequest
import com.umc.domain.usecase.auth.PostEmailVerificationCompleteUseCase
import com.umc.domain.usecase.auth.PostEmailVerificationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SocialSignUpViewModel @Inject constructor(
    private val postEmailVerificationUseCase: PostEmailVerificationUseCase,
    private val postEmailVerificationCompleteUseCase: PostEmailVerificationCompleteUseCase,
) : BaseViewModel<SocialSignUpState, SocialSignUpEvent>(
    SocialSignUpState(),
) {

    /** oAuthVerificationToken 은 SocialSignUpRoute 진입 시 nav argument로 전달받아 주입 */
    fun setOAuthVerificationToken(token: String) {
        updateState { copy(oAuthVerificationToken = token) }
    }

    /**
     * 이메일 변경 시 verifyType을 NONE으로 초기화.
     * ERROR 상태였더라도 이메일을 수정하면 인증이 초기화됨
     */
    fun onEmailChanged(email: String) {
        updateState { copy(email = email, verifyType = EmailVerifyType.NONE) }
    }

    fun onCodeChanged(code: String) {
        updateState { copy(code = code) }
    }

    /**
     * 이메일 인증 코드 발송 요청.
     * 이메일 형식이 유효하지 않거나 서버 오류 시 verifyType이 ERROR로 전환되어 에러 UI가 표시됨.
     * 성공 시 반환된 emailVerificationId를 저장하고 인증번호 입력 필드가 노출됨
     */
    fun onClickVerify() = viewModelScope.launch {
        if (uiState.value.email.isValidEmail()) {
            // 회원가입 흐름이므로 purpose 는 REGISTER 로 고정 (기본값과 동일하지만 명시)
            val request = EmailVerificationRequest(
                email = uiState.value.email,
                purpose = EmailVerifyPurpose.REGISTER
            )
            startLoading()
            resultResponse(
                response = postEmailVerificationUseCase(request),
                successCallback = {
                    updateState {
                        copy(
                            // 서버가 숫자가 아닌 값을 내려줘도 크래시하지 않도록 방어 (기본값 -1)
                            emailVerificationId = it.toIntOrNull() ?: -1,
                            verifyType = EmailVerifyType.REQUEST
                        )
                    }
                    emitEvent(SocialSignUpEvent.ShowVerifyToast)
                },
                errorCallback = { failState ->
                    // 서버 에러 메시지를 그대로 토스트로 노출
                    errorEmailVerify()
                    emitEvent(SocialSignUpEvent.ShowErrorToast(failState.message))
                }
            )
        } else {
            errorEmailVerify()
        }
    }

    /** 인증 코드 확인 요청. 성공 시 verifyType이 VERIFY로 전환되어 이메일 입력·코드 입력 필드가 잠김 */
    fun onClickConfirm() = viewModelScope.launch {
        val request = EmailVerificationCompleteRequest(
            emailVerificationId = uiState.value.emailVerificationId,
            verificationCode = uiState.value.code
        )
        resultResponse(
            response = postEmailVerificationCompleteUseCase(request),
            successCallback = {
                updateState {
                    copy(
                        emailVerificationToken = it,
                        verifyType = EmailVerifyType.VERIFY
                    )
                }
                emitEvent(SocialSignUpEvent.ShowVerifyCompleteToast)
            },
            errorCallback = { failState ->
                emitEvent(SocialSignUpEvent.ShowErrorToast(failState.message))
            }
        )
    }

    /** 인증 완료 후 개인정보 입력 단계로 이동. 회원가입 API 호출에 필요한 토큰들을 함께 전달 */
    fun onClickNext() {
        emitEvent(
            SocialSignUpEvent.MoveToNextEvent(
                oAuthVerificationToken = uiState.value.oAuthVerificationToken,
                emailVerificationToken = uiState.value.emailVerificationToken,
            )
        )
    }

    private fun errorEmailVerify() {
        updateState { copy(verifyType = EmailVerifyType.ERROR) }
    }
}

data class SocialSignUpState(
    val oAuthVerificationToken: String = "",
    val email: String = "",
    val code: String = "",
    val emailVerificationId: Int = -1,
    val emailVerificationToken: String = "",
    val verifyType: EmailVerifyType = EmailVerifyType.NONE,
) : UiState {
    val enableNextButton: Boolean
        get() = verifyType == EmailVerifyType.VERIFY
}

sealed interface SocialSignUpEvent : UiEvent {

    data class MoveToNextEvent(
        val oAuthVerificationToken: String,
        val emailVerificationToken: String,
    ) : SocialSignUpEvent

    object ShowVerifyToast : SocialSignUpEvent

    object ShowVerifyCompleteToast : SocialSignUpEvent

    data class ShowErrorToast(val message: String) : SocialSignUpEvent
}
