package com.umc.presentation.ui.signUp

import androidx.lifecycle.viewModelScope
import com.umc.domain.model.enums.EmailVerifyType
import com.umc.domain.model.school.SchoolInfo
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
    private val getAllSchoolUseCase: GetAllSchoolUseCase
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


    fun onClickConfirm() {
        // TODO 서버 연결 필요
        ULog.d(uiState.value.toString())
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
}

data class SignUpState(
    val name: String = "",
    val nickname: String = "",
    val email: String = "",
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
