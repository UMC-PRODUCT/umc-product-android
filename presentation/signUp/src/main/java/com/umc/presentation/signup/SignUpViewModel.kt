package com.umc.presentation.signup

import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import com.umc.component.base.BaseViewModel
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.umc.component.util.ULog
import com.umc.domain.model.JwtToken
import com.umc.domain.model.enums.EmailVerifyType
import com.umc.domain.model.enums.TermsType
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
import com.umc.domain.usecase.terms.GetTermsByTypeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * 회원가입 화면의 ViewModel.
 *
 * 회원가입 플로우 요약:
 * 1. 이메일 인증 요청 → 2. 인증 코드 확인 → 3. 회원 등록 → 4. 토큰 저장 → 5. FCM 토큰 등록
 *
 * oAuthVerificationToken 은 로그인 화면에서 소셜 로그인 후 서버로부터 받은 토큰으로,
 * 회원 등록 API 호출 시 반드시 포함해야 함. SignUpRoute 진입 시 setOAuthVerificationToken()으로 주입.
 *
 * 약관 동의는 서버에서 약관 ID를 조회한 뒤 자동으로 PRIVACY·SERVICE 두 항목에 동의 처리함.
 * 별도의 약관 동의 UI 없이 가입 완료 버튼 클릭만으로 처리되는 구조임에 유의.
 */
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

    /** 학교 목록을 서버에서 조회해 상태에 저장. 화면 진입 시 1회 자동 호출됨 */
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
                emitEvent(SignUpEvent.ShowVerifyCompleteToast)
            },
            errorCallback = {
                emitEvent(SignUpEvent.ShowVerifyErrorToast)
            }
        )
    }

    fun onNameChanged(name: String) {
        updateState { copy(name = name) }
    }

    fun onNicknameChanged(nickname: String) {
        updateState { copy(nickname = nickname) }
    }

    /**
     * 이메일 변경 시 verifyType을 NONE으로 초기화.
     * 이미 인증 요청(REQUEST)·완료(VERIFY) 상태였더라도 이메일을 수정하면 인증이 초기화됨
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
     * 성공 시 반환된 emailVerificationId를 저장하고 코드 입력 필드로 포커스 이동 이벤트를 발행
     */
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
                    emitEvent(SignUpEvent.ShowVerifyToast)
                    emitEvent(SignUpEvent.FocusVerifyCodeField)
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
        updateState { copy(verifyType = EmailVerifyType.ERROR) }
    }

    fun onClickSchool() {
        emitEvent(SignUpEvent.ShowSchoolBottomSheet)
    }

    fun isValidEmail(): Boolean {
        val regex = Regex(
            pattern = "[a-zA-Z0-9+._%\\-]{1,256}@[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}(\\.[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25})+",
            option = RegexOption.IGNORE_CASE
        )
        return uiState.value.email.isNotBlank() && uiState.value.email.matches(regex)
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

            startLoading()
            resultResponse(
                response = registerUseCase(request),
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

    /** oAuthVerificationToken 은 SignUpRoute 진입 시 nav argument로 전달받아 주입 */
    fun setOAuthVerificationToken(token: String) {
        updateState { copy(oAuthVerificationToken = token) }
    }
}

/**
 * 회원가입 화면 UI 상태.
 *
 * @property emailVerificationId 이메일 인증 요청 후 서버에서 발급한 ID. 인증 코드 확인 시 사용
 * @property emailVerificationToken 인증 코드 확인 성공 후 서버에서 발급한 토큰. 회원 등록 시 사용
 * @property oAuthVerificationToken 소셜 로그인 후 발급된 토큰. 회원 등록 시 사용
 * @property verifyType 이메일 인증 단계 (NONE → REQUEST → VERIFY 또는 ERROR)
 * @property enableNextButton 모든 필수 항목 입력 및 이메일 인증 완료 시 true
 */
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
    object ShowVerifyToast : SignUpEvent
    object ShowVerifyCompleteToast : SignUpEvent
    object ShowVerifyErrorToast : SignUpEvent
    /** 인증 코드 발송 성공 시 코드 입력 필드로 포커스를 이동시키기 위한 이벤트 */
    object FocusVerifyCodeField : SignUpEvent
    data class ShowRegisterErrorDialog(val message: String) : SignUpEvent
}
