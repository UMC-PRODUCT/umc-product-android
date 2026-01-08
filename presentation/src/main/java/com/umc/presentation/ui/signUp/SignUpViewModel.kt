package com.umc.presentation.ui.signUp

import androidx.lifecycle.viewModelScope
import com.umc.domain.model.enums.EmailVerifyType
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import com.umc.presentation.util.Const
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

class SignUpViewModel
@Inject
constructor() : BaseViewModel<SignUpState, SignUpEvent>(
    SignUpState(),
) {
    init {
        initFun()
    }

    private fun initFun() {
        viewModelScope.launch {
            delay(3.seconds)
            emitEvent(SignUpEvent.MoveToLoginEvent)
        }
    }

    fun onClickConfirm() {
        // TODO 서버 연결 필요
        updateState {
            copy(
                verifyType = EmailVerifyType.VERIFY
            )
        }
    }

    fun onNameChanged(name: String) {
        updateState { copy(name = name) }
    }

    fun onNicknameChanged(nickname: String) {
        updateState { copy(nickname = nickname) }
    }

    fun onEmailChanged(email: String) {
        updateState { copy(email = email, verifyType = EmailVerifyType.NONE) }
    }

    fun onCodeChanged(code: String) {
        updateState { copy(code = code) }
    }

    fun onClickVerify() {
        // TODO 서버 요청
        if (isValidEmail()) {
            updateState {
                copy(
                    verifyType = EmailVerifyType.REQUEST
                )
            }
        } else {
            updateState {
                copy(
                    verifyType = EmailVerifyType.ERROR
                )
            }
        }
    }

    fun isValidEmail(): Boolean {
        val regex = Regex(
            pattern = Const.URegex.EMAIL_REGEX,
            option = RegexOption.IGNORE_CASE
        )
        val isValid = uiState.value.email.isNotBlank() && uiState.value.email.matches(regex)
        return isValid
    }

}

data class SignUpState(
    val name: String = "",
    val nickname: String = "",
    val email: String = "",
    val code: String = "",
    val verifyType: EmailVerifyType = EmailVerifyType.NONE,
    val school: String = "",
) : UiState {
    val enableNextButton: Boolean
        get() = name.isNotEmpty() && nickname.isNotEmpty() && email.isNotEmpty()
                && school.isNotEmpty() && verifyType == EmailVerifyType.VERIFY
}

sealed class SignUpEvent : UiEvent {
    object MoveToMainEvent : SignUpEvent()

    object MoveToLoginEvent : SignUpEvent()
}
