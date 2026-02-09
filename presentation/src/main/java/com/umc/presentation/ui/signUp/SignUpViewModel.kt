package com.umc.presentation.ui.signUp

import androidx.lifecycle.viewModelScope
import com.umc.domain.model.enums.EmailVerifyType
import com.umc.domain.model.request.EmailVerificationCompleteRequest
import com.umc.domain.model.request.EmailVerificationRequest
import com.umc.domain.model.request.member.RegisterRequest
import com.umc.domain.model.school.SchoolInfo
import com.umc.domain.usecase.auth.PostEmailVerificationCompleteUseCase
import com.umc.domain.usecase.auth.PostEmailVerificationUseCase
import com.umc.domain.usecase.member.RegisterUseCase
import com.umc.domain.usecase.school.GetAllSchoolUseCase
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import com.umc.presentation.util.Const
import com.umc.presentation.util.ULog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val getAllSchoolUseCase: GetAllSchoolUseCase,
    private val postEmailVerificationUseCase: PostEmailVerificationUseCase,
    private val postEmailVerificationCompleteUseCase: PostEmailVerificationCompleteUseCase,
    private val registerUseCase: RegisterUseCase
) : BaseViewModel<SignUpState, SignUpEvent>(
    SignUpState(),
) {

    init {
        getAllSchool()
    }

    private fun getAllSchool() = viewModelScope.launch {
        resultResponse(
            response = getAllSchoolUseCase(),
            successCallback = {
                updateState { copy(schoolList = it) }
            }
        )
    }

    fun onClickBack() {
        emitEvent(SignUpEvent.MoveToBack)
    }


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
            },
            errorCallback = {
                //TODO Toast같은거 띄워줘야 할 듯
            }
        )
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

    fun onClickVerify() = viewModelScope.launch {
        if (isValidEmail()) {
            val request = EmailVerificationRequest(email = uiState.value.email)
            resultResponse(
                response = postEmailVerificationUseCase(request),
                successCallback = {
                    updateState {
                        copy(
                            emailVerificationId = it.toInt(),
                            verifyType = EmailVerifyType.REQUEST
                        )
                    }
                },
                errorCallback = {
                    errorEmailVerify()
                }
            )
        } else {
            errorEmailVerify()
        }
    }

    private fun errorEmailVerify() {
        updateState {
            copy(
                verifyType = EmailVerifyType.ERROR
            )
        }
    }

    fun onClickSchool() {
        emitEvent(SignUpEvent.ShowSchoolBottomSheet)
    }

    fun isValidEmail(): Boolean {
        val regex = Regex(
            pattern = Const.URegex.EMAIL_REGEX,
            option = RegexOption.IGNORE_CASE
        )
        val isValid = uiState.value.email.isNotBlank() && uiState.value.email.matches(regex)
        return isValid
    }

    fun updateSelectSchool(school: SchoolInfo) {
        updateState { copy(school = school) }
    }

    fun register() = viewModelScope.launch {
        val request = RegisterRequest(
            oAuthVerificationToken = uiState.value.oAuthVerificationToken,
            name = uiState.value.name,
            nickname = uiState.value.nickname,
            emailVerificationToken = uiState.value.emailVerificationToken,
            schoolId = uiState.value.school.schoolId,
            profileImageId = null,
        )

        resultResponse(
            response = registerUseCase(request),
            successCallback = {
                emitEvent(SignUpEvent.MoveToMainEvent)
            },
            errorCallback = {
                //TODO Toast?
                ULog.d("에러 로그")
            }
        )
    }

    fun setOAuthVerificationToken(token: String) {
        updateState { copy(oAuthVerificationToken = token) }
    }
}

data class SignUpState(
    val name: String = "",
    val nickname: String = "",
    val email: String = "",
    val oAuthVerificationToken: String = "",
    val emailVerificationId: Int = -1,
    val emailVerificationToken: String = "",
    val code: String = "",
    val verifyType: EmailVerifyType = EmailVerifyType.NONE,
    val school: SchoolInfo = SchoolInfo(),
    val schoolList: List<SchoolInfo> = emptyList()
) : UiState {
    val enableNextButton: Boolean
        get() = name.isNotEmpty() && nickname.isNotEmpty() && email.isNotEmpty()
                && school.schoolId != -1 && verifyType == EmailVerifyType.VERIFY
}

sealed interface SignUpEvent : UiEvent {
    object MoveToMainEvent : SignUpEvent
    object MoveToLoginEvent : SignUpEvent
    object MoveToBack : SignUpEvent
    object ShowSchoolBottomSheet : SignUpEvent
}
