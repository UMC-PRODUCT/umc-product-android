package com.umc.presentation.signup

import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import com.umc.component.base.BaseViewModel
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.umc.component.util.ULog
import com.umc.domain.model.JwtToken
import com.umc.domain.model.enums.SignUpType
import com.umc.domain.model.enums.TermsType
import com.umc.domain.model.request.member.RegisterEmailRequest
import com.umc.domain.model.request.member.RegisterRequest
import com.umc.domain.model.request.member.TermsAgreement
import com.umc.domain.model.school.SchoolInfo
import com.umc.domain.usecase.appDataStore.SaveTokenUseCase
import com.umc.domain.usecase.member.RegisterEmailUseCase
import com.umc.domain.usecase.member.RegisterUseCase
import com.umc.domain.usecase.notification.RegisterFcmTokenUseCase
import com.umc.domain.usecase.school.GetAllSchoolUseCase
import com.umc.domain.usecase.terms.GetTermsByTypeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val getAllSchoolUseCase: GetAllSchoolUseCase,
    private val registerUseCase: RegisterUseCase,
    private val registerEmailUseCase: RegisterEmailUseCase,
    private val saveTokenUseCase: SaveTokenUseCase,
    private val getTermsByTypeUseCase: GetTermsByTypeUseCase,
    private val registerFcmTokenUseCase: RegisterFcmTokenUseCase,
) : BaseViewModel<SignUpState, SignUpEvent>(
    SignUpState(),
) {

    init {
        getAllSchool()
    }

    /** 학교 목록을 서버에서 조회해 상태에 저장. 화면 진입 시 1회 자동 호출됨 */
    private fun getAllSchool() = viewModelScope.launch {
        resultResponse(
            response = getAllSchoolUseCase(),
            successCallback = {
                updateState { copy(schoolList = it) }
            }
        )
    }

    /**
     * 회원가입 진입 경로별 인자 주입. SignUpRoute 진입 시 nav argument로 전달받음.
     * - SOCIAL: oAuthVerificationToken + emailVerificationToken
     * - EMAIL: emailVerificationToken + rawPassword
     */
    fun setArguments(
        signUpType: SignUpType,
        oAuthVerificationToken: String,
        emailVerificationToken: String,
        rawPassword: String,
    ) {
        updateState {
            copy(
                signUpType = signUpType,
                oAuthVerificationToken = oAuthVerificationToken,
                emailVerificationToken = emailVerificationToken,
                rawPassword = rawPassword,
            )
        }
    }

    fun onClickBack() {
        emitEvent(SignUpEvent.MoveToBack)
    }

    fun onNameChanged(name: String) {
        updateState { copy(name = name) }
    }

    fun onNicknameChanged(nickname: String) {
        updateState { copy(nickname = nickname) }
    }

    fun onClickSchool() {
        emitEvent(SignUpEvent.ShowSchoolBottomSheet)
    }

    /** 학교 바텀시트에서 선택된 학교를 상태에 반영 */
    fun updateSelectSchool(school: SchoolInfo) {
        updateState { copy(school = school) }
    }

    /**
     * 회원 등록 진입점. PRIVACY → SERVICE 약관 ID를 순차 조회한 뒤 등록 API를 호출함.
     * 약관 조회가 두 번의 별도 API 호출로 이루어지는 이유는 서버가 약관 ID를 동적으로 관리하기 때문
     */
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

    /** 진입 경로(signUpType)에 따라 소셜/이메일 회원가입 API를 분기 호출 */
    private fun executeRegister(privacyTermsId: Int, serviceTermsId: Int) {
        viewModelScope.launch {
            val termsAgreements = listOf(
                TermsAgreement(termsId = privacyTermsId, isAgreed = true),
                TermsAgreement(termsId = serviceTermsId, isAgreed = true)
            )

            startLoading()

            val response = when (uiState.value.signUpType) {
                SignUpType.SOCIAL -> registerUseCase(
                    RegisterRequest(
                        oAuthVerificationToken = uiState.value.oAuthVerificationToken,
                        name = uiState.value.name,
                        nickname = uiState.value.nickname,
                        emailVerificationToken = uiState.value.emailVerificationToken,
                        schoolId = uiState.value.school.schoolId,
                        profileImageId = null,
                        termsAgreements = termsAgreements,
                    )
                )

                SignUpType.EMAIL -> registerEmailUseCase(
                    RegisterEmailRequest(
                        rawPassword = uiState.value.rawPassword,
                        name = uiState.value.name,
                        nickname = uiState.value.nickname,
                        emailVerificationToken = uiState.value.emailVerificationToken,
                        schoolId = uiState.value.school.schoolId,
                        termsAgreements = termsAgreements,
                    )
                )
            }

            resultResponse(
                response = response,
                successCallback = { updateToken(it) },
                errorCallback = { failState ->
                    emitEvent(SignUpEvent.ShowRegisterErrorDialog(failState.message))
                }
            )
        }
    }

    private fun updateToken(token: JwtToken) = viewModelScope.launch {
        resultResponse(
            response = saveTokenUseCase(token),
            successCallback = { registerFcmToken() }
        )
    }

    /**
     * FCM 토큰 등록. 실패하거나 토큰 획득 자체에서 예외가 발생해도 권한 요청 화면으로 이동.
     * 푸시 알림 기능은 FCM 등록 실패 시 일부 동작하지 않을 수 있음
     */
    private fun registerFcmToken() = viewModelScope.launch {
        try {
            val fcmToken = FirebaseMessaging.getInstance().token.await()
            resultResponse(
                response = registerFcmTokenUseCase(fcmToken),
                successCallback = { emitEvent(SignUpEvent.MoveToPermissionEvent) },
                errorCallback = { emitEvent(SignUpEvent.MoveToPermissionEvent) }
            )
        } catch (e: Exception) {
            ULog.d("FCM 토큰 획득 실패: ${e.message}")
            emitEvent(SignUpEvent.MoveToPermissionEvent)
        }
    }
}

data class SignUpState(
    val signUpType: SignUpType = SignUpType.SOCIAL,
    val oAuthVerificationToken: String = "",
    val emailVerificationToken: String = "",
    val rawPassword: String = "",
    val name: String = "",
    val nickname: String = "",
    val school: SchoolInfo = SchoolInfo(),
    val schoolList: List<SchoolInfo> = emptyList()
) : UiState {
    val enableNextButton: Boolean
        get() = name.isNotEmpty() && nickname.isNotEmpty() && school.schoolId != -1
}

sealed interface SignUpEvent : UiEvent {
    object MoveToPermissionEvent : SignUpEvent
    object MoveToBack : SignUpEvent
    object ShowSchoolBottomSheet : SignUpEvent
    data class ShowRegisterErrorDialog(val message: String) : SignUpEvent
}
