package com.umc.presentation.login.findpassword

import androidx.lifecycle.viewModelScope
import com.umc.component.base.BaseViewModel
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.umc.domain.model.enums.EmailVerifyPurpose
import com.umc.domain.model.enums.EmailVerifyType
import com.umc.domain.model.request.EmailVerificationCompleteRequest
import com.umc.domain.model.request.EmailVerificationRequest
import com.umc.domain.model.request.PasswordResetRequest
import com.umc.domain.usecase.auth.PatchPasswordResetUseCase
import com.umc.domain.usecase.auth.PostEmailVerificationCompleteUseCase
import com.umc.domain.usecase.auth.PostEmailVerificationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

// 비밀번호 최소 길이
private const val PASSWORD_MIN_LENGTH = 8

@HiltViewModel
class FindPasswordViewModel @Inject constructor(
    private val postEmailVerificationUseCase: PostEmailVerificationUseCase,
    private val postEmailVerificationCompleteUseCase: PostEmailVerificationCompleteUseCase,
    private val patchPasswordResetUseCase: PatchPasswordResetUseCase,
) : BaseViewModel<FindPasswordState, FindPasswordEvent>(
    FindPasswordState(),
) {

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

    fun onPasswordChanged(password: String) {
        updateState { copy(password = password) }
    }

    fun onPasswordCheckChanged(passwordCheck: String) {
        updateState { copy(passwordCheck = passwordCheck) }
    }

    /**
     * 이메일 인증 코드 발송 요청. 비밀번호 초기화 흐름이므로 purpose 는 PASSWORD_RESET.
     * 이메일 형식이 유효하지 않거나 서버 오류 시 verifyType이 ERROR로 전환되어 에러 UI가 표시됨
     */
    fun onClickVerify() = viewModelScope.launch {
        if (isValidEmail()) {
            val request = EmailVerificationRequest(
                email = uiState.value.email,
                purpose = EmailVerifyPurpose.PASSWORD_RESET
            )
            startLoading()
            resultResponse(
                response = postEmailVerificationUseCase(request),
                successCallback = {
                    updateState {
                        copy(
                            emailVerificationId = it.toInt(),
                            verifyType = EmailVerifyType.REQUEST
                        )
                    }
                    emitEvent(FindPasswordEvent.ShowVerifyToast)
                },
                errorCallback = { failState ->
                    // 서버 에러 메시지를 그대로 토스트로 노출
                    errorEmailVerify()
                    emitEvent(FindPasswordEvent.ShowErrorToast(failState.message))
                }
            )
        } else {
            errorEmailVerify()
        }
    }

    /** 인증 코드 확인 요청. 성공 시 verifyType이 VERIFY로 전환되어 새 비밀번호 입력 섹션이 노출됨 */
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
                emitEvent(FindPasswordEvent.ShowVerifyCompleteToast)
            },
            errorCallback = { failState ->
                emitEvent(FindPasswordEvent.ShowErrorToast(failState.message))
            }
        )
    }

    // 비밀번호 변경 요청. 성공 시 완료 화면으로 전환
    fun onClickComplete() = viewModelScope.launch {
        val request = PasswordResetRequest(
            emailVerificationToken = uiState.value.emailVerificationToken,
            newPassword = uiState.value.password,
        )
        startLoading()
        resultResponse(
            response = patchPasswordResetUseCase(request),
            successCallback = {
                updateState { copy(isCompleted = true) }
            },
            errorCallback = { failState ->
                emitEvent(FindPasswordEvent.ShowErrorToast(failState.message))
            }
        )
    }

    private fun errorEmailVerify() {
        updateState { copy(verifyType = EmailVerifyType.ERROR) }
    }

    private fun isValidEmail(): Boolean {
        val regex = Regex(
            pattern = "[a-zA-Z0-9+._%\\-]{1,256}@[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}(\\.[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25})+",
            option = RegexOption.IGNORE_CASE
        )
        return uiState.value.email.isNotBlank() && uiState.value.email.matches(regex)
    }
}

data class FindPasswordState(
    val email: String = "",
    val code: String = "",
    val emailVerificationId: Int = -1,
    val emailVerificationToken: String = "",
    val verifyType: EmailVerifyType = EmailVerifyType.NONE,
    val password: String = "",
    val passwordCheck: String = "",
    val isCompleted: Boolean = false,
) : UiState {
    val enableCompleteButton: Boolean
        get() = verifyType == EmailVerifyType.VERIFY
                && password.length >= PASSWORD_MIN_LENGTH
                && password == passwordCheck
}

sealed interface FindPasswordEvent : UiEvent {

    object ShowVerifyToast : FindPasswordEvent

    object ShowVerifyCompleteToast : FindPasswordEvent

    data class ShowErrorToast(val message: String) : FindPasswordEvent
}
