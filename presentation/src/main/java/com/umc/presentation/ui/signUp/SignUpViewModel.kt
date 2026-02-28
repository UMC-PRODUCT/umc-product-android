package com.umc.presentation.ui.signUp

import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import com.umc.domain.model.JwtToken
import com.umc.domain.model.enums.EmailVerifyType
import com.umc.domain.model.enums.TermsType
import com.umc.domain.usecase.terms.GetTermsByTypeUseCase
import com.umc.domain.model.request.EmailVerificationCompleteRequest
import com.umc.domain.model.request.EmailVerificationRequest
import com.umc.domain.model.request.member.RegisterRequest
import com.umc.domain.model.request.member.TermsAgreement
import com.umc.domain.model.school.SchoolInfo
import com.umc.domain.usecase.appDataStore.SaveTokenUseCase
import com.umc.domain.usecase.auth.PostEmailVerificationCompleteUseCase
import com.umc.domain.usecase.auth.PostEmailVerificationUseCase
import com.umc.domain.usecase.member.RegisterUseCase
import com.umc.domain.usecase.notification.RegisterFcmTokenUseCase
import com.umc.domain.usecase.school.GetAllSchoolUseCase
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import com.umc.presentation.util.Const
import com.umc.presentation.util.ULog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val getAllSchoolUseCase: GetAllSchoolUseCase,
    private val postEmailVerificationUseCase: PostEmailVerificationUseCase,
    private val postEmailVerificationCompleteUseCase: PostEmailVerificationCompleteUseCase,
    private val registerUseCase: RegisterUseCase,
    private val saveTokenUseCase: SaveTokenUseCase,
    private val getTermsByTypeUseCase: GetTermsByTypeUseCase,
    private val registerFcmTokenUseCase: RegisterFcmTokenUseCase,
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
        startLoading()
        fetchPrivacyTermsAndProceed()
    }

    private fun fetchPrivacyTermsAndProceed() = viewModelScope.launch {
        resultResponse(
            response = getTermsByTypeUseCase(TermsType.PRIVACY),
            successCallback = { privacyTerms ->
                fetchServiceTermsAndRegister(privacyTerms.id.toInt())
            },
            errorCallback = {
                ULog.d("개인정보 약관 조회 실패")
            }
        )
    }

    private fun fetchServiceTermsAndRegister(privacyTermsId: Int) {
        viewModelScope.launch {
            resultResponse(
                response = getTermsByTypeUseCase(TermsType.SERVICE),
                successCallback = { serviceTerms ->
                    executeRegister(privacyTermsId, serviceTerms.id.toInt())
                },
                errorCallback = {
                    ULog.d("서비스 약관 조회 실패")
                }
            )
        }
    }

    private fun executeRegister(privacyTermsId: Int, serviceTermsId: Int) {
        viewModelScope.launch {
            val request = RegisterRequest(
                oAuthVerificationToken = uiState.value.oAuthVerificationToken,
                name = uiState.value.name,
                nickname = uiState.value.nickname,
                emailVerificationToken = uiState.value.emailVerificationToken,
                schoolId = uiState.value.school.schoolId,
                profileImageId = null,
                termsAgreements = listOf(
                    TermsAgreement(termsId = privacyTermsId, isAgreed = true),
                    TermsAgreement(termsId = serviceTermsId, isAgreed = true)
                )
            )

            resultResponse(
                response = registerUseCase(request),
                successCallback = {
                    updateToken(it)
                },
                errorCallback = {
                    ULog.d("회원가입 에러 로그")
                }
            )
        }
    }

    private fun updateToken(token: JwtToken) = viewModelScope.launch {
        resultResponse(
            response = saveTokenUseCase(token),
            successCallback = {
                registerFcmToken()
            }
        )
    }

    private fun registerFcmToken() = viewModelScope.launch {
        try {
            val fcmToken = FirebaseMessaging.getInstance().token.await()
            resultResponse(
                response = registerFcmTokenUseCase(fcmToken),
                successCallback = {
                    emitEvent(SignUpEvent.MoveToPermissionEvent)
                },
                errorCallback = {
                    emitEvent(SignUpEvent.MoveToPermissionEvent)
                }
            )
        } catch (e: Exception) {
            ULog.d("FCM 토큰 획득 실패: ${e.message}")
            emitEvent(SignUpEvent.MoveToPermissionEvent)
        }
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
    object MoveToPermissionEvent : SignUpEvent
    object MoveToBack : SignUpEvent
    object ShowSchoolBottomSheet : SignUpEvent
}
