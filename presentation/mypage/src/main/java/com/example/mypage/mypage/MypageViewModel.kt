package com.example.mypage.mypage

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.mypage.nearby.NearbyViewModel
import com.kakao.sdk.auth.TokenManagerProvider
import com.umc.component.fcm.FcmTokenSynchronizer
import com.umc.component.base.BaseViewModel
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.umc.component.theme.AppStrings
import com.umc.domain.model.UserInfo
import com.umc.domain.model.enums.LoginType
import com.umc.domain.model.enums.TermsType
import com.umc.domain.model.enums.UserChallengerRole
import com.umc.domain.model.enums.UserPart
import com.umc.domain.model.home.getGisuSummaryList
import com.umc.domain.model.request.challenger.ChallengerRecordMemberRequest
import com.umc.domain.usecase.appDataStore.ClearAllDataUseCase
import com.umc.domain.usecase.PostLoginUseCase
import com.umc.domain.usecase.authentication.AddMyOAuthUseCase
import com.umc.domain.usecase.authentication.GetMyOAuthUseCase
import com.umc.domain.usecase.challenger.AddChallengerRecordMemberUseCase
import com.umc.domain.usecase.member.DeleteUserUseCase
import com.umc.domain.usecase.member.GetMyProfileUseCase
import com.umc.domain.usecase.terms.GetTermsByTypeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import javax.inject.Inject

// 서버가 이미 사용된 챌린저 기록 추가 코드에 내려주는 에러 코드
internal const val CODE_ALREADY_USED = "CHALLENGER-0012"

@HiltViewModel
class MypageViewModel @Inject constructor(
    private val tokenSynchronizer: FcmTokenSynchronizer,
    private val getMyProfileUseCase: GetMyProfileUseCase, //내 프로필 정보 가져오기
    private val clearAllDataUseCase : ClearAllDataUseCase, //모든 정보 삭제하기
    private val deleteUserUseCase: DeleteUserUseCase, //회원 탈퇴
    private val getTermsByTypeUseCase: GetTermsByTypeUseCase, //타입으로 약관 가져오기
    private val getMyOAuthUseCase: GetMyOAuthUseCase, //내 OAuth 가져오기
    private val addChallengerRecordMemberUseCase: AddChallengerRecordMemberUseCase, //챌린저 코드 추가
    private val postLoginUseCase: PostLoginUseCase, //소셜 연동용 소셜 토큰 검증
    private val addMyOAuthUseCase: AddMyOAuthUseCase, //소셜 계정 연동 추가
) : BaseViewModel<MypageUiState, MypageEvent>(
    MypageUiState()){


    //초기 상태
    init {
        viewModelScope.launch {
            //유저 정보 가져오기
            getUserInfo()

            //내 소셜 정보 가져오기
            getUserOAuth()
        }
    }

    /**
     * 서버에서 내 프로필 데이터를 조회하고 깃허브, 링크드인, 블로그 URL을 UI State에 갱신하는 메서드
     */
    fun getUserInfo() {
        viewModelScope.launch {
            resultResponse(
                response = getMyProfileUseCase(),
                successCallback = { userInfo ->
                    updateState {
                        copy(
                            userInfo = userInfo,
                            githubUrl = userInfo.profile.github,
                            linkedinUrl = userInfo.profile.linkedIn,
                            blogUrl = userInfo.profile.blog,
                            isProfileLoadFailed = false
                        )
                    }

                },
                errorCallback = {
                    // 조회 실패와 '링크 미등록'을 구분할 수 있도록 실패 상태를 남긴다.
                    updateState { copy(isProfileLoadFailed = true) }
                }
            )
        }
    }


    /**
     * 연동되어 있는 소셜 로그인 플랫폼(KAKAO, GOOGLE 등) 목록을 조회하는 메서드
     */
    fun getUserOAuth(){
        viewModelScope.launch {
            resultResponse(
                response = getMyOAuthUseCase(),
                successCallback = { myOAuth ->
                    val platforms = myOAuth.map { LoginType.valueOf(it.provider) }
                    updateState {
                        copy(
                            linkedPlatforms = platforms.toImmutableList(),
                            isLinkedPlatformsLoaded = true
                        )
                    }

                },
                errorCallback = {}
            )
        }
    }

    fun navigateToGithub(){
        openOutLink(MypageEvent.NavigateToGithub)
    }
    fun navigateToLinkedin(){
        openOutLink(MypageEvent.NavigateToLinkedin)
    }
    fun navigateToBlog(){
        openOutLink(MypageEvent.NavigateToBlog)
    }

    /**
     * 외부 링크(깃허브, 링크드인, 블로그) 이동 이벤트를 보내는 메서드
     *
     * 프로필 조회가 실패한 상태라면 링크가 비어 있는 것이 아니라 아직 못 불러온 것이므로,
     * '링크 미등록' 안내 대신 불러오기 실패 안내를 보내고 프로필을 다시 조회한다.
     */
    private fun openOutLink(event: MypageEvent) {
        if (uiState.value.isProfileLoadFailed) {
            emitEvent(MypageEvent.ProfileLoadFailed)
            getUserInfo()
        } else {
            emitEvent(event)
        }
    }

    /**
     * 챌린저 기록 추가(코드 입력) 바텀시트를 여는 메서드
     */
    fun navigateToAddActivity(){
        emitEvent(MypageEvent.NavigateToAddActivity)
    }


    fun navigateToAssistUmc(){
        emitEvent(MypageEvent.NavigateToAssistUmc(uiState.value.kakaoInquireChannelId))
    }


    fun navigateToSettingNotice(){
        emitEvent(MypageEvent.NavigateToSettingNotice)
    }

    fun navigateToSettingLocation(){
        emitEvent(MypageEvent.NavigateToSettingLocation)
    }

    /**
     * 소셜 계정(카카오, 구글)을 현재 계정에 연동하는 메서드
     *
     * 소셜 토큰으로 로그인 API를 호출해 연동용 검증 토큰(oAuthVerificationToken)을 받고, 그 토큰으로 연동을 추가한다.
     * 검증 토큰이 없다면 그 소셜 계정은 이미 다른 회원으로 가입된 것이라 연동할 수 없다.
     * 이때 함께 내려온 로그인 토큰은 저장하지 않고 버려서 현재 로그인 상태를 유지한다.
     */
    fun linkSocial(token: String, type: LoginType) {
        // 연타·중복 요청 방지
        if (uiState.value.isLinkingSocial) return
        updateState { copy(isLinkingSocial = true) }

        viewModelScope.launch {
            resultResponse(
                response = postLoginUseCase(type, token),
                successCallback = { jwt ->
                    if (jwt.oAuthVerificationToken.isNotEmpty()) {
                        addMyOAuth(jwt.oAuthVerificationToken)
                    } else {
                        updateState { copy(isLinkingSocial = false) }
                        emitEvent(MypageEvent.SocialLinkFailed(AppStrings.MYPAGE_SOCIAL_LINK_ALREADY_REGISTERED))
                    }
                },
                errorCallback = { failState ->
                    updateState { copy(isLinkingSocial = false) }
                    emitEvent(
                        MypageEvent.SocialLinkFailed(
                            failState.message.ifBlank { AppStrings.MYPAGE_SOCIAL_LINK_FAIL }
                        )
                    )
                }
            )
        }
    }

    private fun addMyOAuth(oAuthVerificationToken: String) {
        viewModelScope.launch {
            resultResponse(
                response = addMyOAuthUseCase(oAuthVerificationToken),
                successCallback = { added ->
                    // 응답의 연동 목록을 기존 목록과 합쳐 바로 반영한다(재조회가 실패해도 카드가 남지 않게)
                    val merged = (uiState.value.linkedPlatforms +
                        added.mapNotNull { runCatching { LoginType.valueOf(it.provider) }.getOrNull() })
                        .distinct()
                    updateState {
                        copy(
                            isLinkingSocial = false,
                            linkedPlatforms = merged.toImmutableList(),
                            isLinkedPlatformsLoaded = true
                        )
                    }
                    // 서버 기준으로 다시 맞추는 재조회는 보조로만 둔다.
                    getUserOAuth()
                    emitEvent(MypageEvent.SocialLinked)
                },
                errorCallback = { failState ->
                    updateState { copy(isLinkingSocial = false) }
                    emitEvent(
                        MypageEvent.SocialLinkFailed(
                            failState.message.ifBlank { AppStrings.MYPAGE_SOCIAL_LINK_FAIL }
                        )
                    )
                }
            )
        }
    }


    //개인정보처리 방침
    fun navigateToPersonalInformation(){
        viewModelScope.launch {
            resultResponse(
                response = getTermsByTypeUseCase(TermsType.PRIVACY),
                successCallback = { term ->
                    emitEvent(MypageEvent.NavigateToPersonalInformation(term.link))
                },
                errorCallback = {
                    emitEvent(MypageEvent.TermsLoadFailed(TermsType.PRIVACY))
                }
            )
        }


    }

    //이용 약관
    fun navigateToUseManual(){
        viewModelScope.launch {
            resultResponse(
                response = getTermsByTypeUseCase(TermsType.SERVICE),
                successCallback = { term ->
                    emitEvent(MypageEvent.NavigateToUseManual(term.link))
                },
                errorCallback = {
                    emitEvent(MypageEvent.TermsLoadFailed(TermsType.SERVICE))
                }
            )
        }


    }

    fun navigateToWebsiteUmc(){
        emitEvent(MypageEvent.NavigateToWebstieUmc)
    }

    fun navigateToInstagramUmc(){
        emitEvent(MypageEvent.NavigateToInstagramUmc)
    }

    fun navigateToOnBoardPage(){
        emitEvent(MypageEvent.MoveToOnBoardPage)
    }

    fun navigateToBack(){
        emitEvent(MypageEvent.NavigateToBack)
    }



    //유저 삭제 다이얼로그 호출
    fun showDeleteUserDialog(){
        //1. 삭제 이벤트를 전송
        emitEvent(MypageEvent.DeleteUser)
    }


    /**
     * 소셜 인증 토큰 정보(카카오, 구글)를 수신하여 UI State에 설정하는 메서드
     */
    fun getKakaoAndGoogleToken(googleToken: String){
        //카카오 토큰
        TokenManagerProvider.instance.manager.getToken()?.let { token ->
            updateState {
                copy(kakaoToken = token.accessToken)
            }
        }

        updateState {
            copy(googleToken = googleToken)
        }

    }

    /**
     * 서버에 회원 탈퇴 요청을 보내고 로컬 DataStore 초기화 후 로그인 화면으로 이동하는 메서드
     */
    fun deleteUser(){

        viewModelScope.launch {
            resultResponse(
                response = deleteUserUseCase(uiState.value.kakaoToken, uiState.value.googleToken),
                successCallback = {
                    viewModelScope.launch {
                        // 회원 탈퇴 성공 시 dataStore의 모든 데이터 삭제
                        clearAllDataUseCase()
                        emitEvent(MypageEvent.MoveToOnBoardPage)
                    }
                },
                errorCallback = { failState ->
                    // 표준 응답이 아닌 실패(게이트웨이 HTML 등)는 message 가 비거나 영문일 수 있어 기본 문구로 보정한다.
                    emitEvent(
                        MypageEvent.ShowErrorToast(
                            failState.message.ifBlank { AppStrings.MYPAGE_DELETE_USER_FAIL }
                        )
                    )
                }
            )
        }
    }

    /**
     * 챌린저 활동 코드 입력 텍스트 변경 처리 메서드
     */
    fun onCodeChanged(code: String) {
        updateState { copy(code = code) }
    }

    /**
     * 입력한 코드를 비우는 메서드
     *
     * 시트는 닫아도 ViewModel 은 살아있어서, 지우지 않으면 다시 열었을 때 이전 입력이 그대로 남는다.
     */
    fun clearCode() {
        updateState { copy(code = "") }
    }

    /**
     * 챌린저 활동 코드를 검증하여 내 계정에 기록을 추가하는 메서드
     *
     * 이미 다른 기수의 챌린저인 회원도 쓸 수 있다. 서버가 코드에 담긴 기수·파트·지부로 기록을
     * 새로 붙여주므로, 성공하면 프로필을 다시 조회해 화면에 반영한다.
     */
    fun addChallengerCode() {
        // 요청 진행 중에는 다시 보내지 않는다(연타하면 같은 코드가 중복 전송되거나 입력이 비워진 빈 코드가 전송된다).
        if (uiState.value.isAddingCode) return
        updateState { copy(isAddingCode = true) }

        viewModelScope.launch {
            val request = ChallengerRecordMemberRequest(
                code = uiState.value.code,
            )

            resultResponse(
                response = addChallengerRecordMemberUseCase(request),
                successCallback = {
                    clearCode()
                    //유저 정보 업데이트를 위한 호출
                    viewModelScope.launch {
                        resultResponse(
                            response = getMyProfileUseCase(),
                            successCallback = {
                                updateState { copy(isAddingCode = false) }
                                emitEvent(MypageEvent.ConfirmAddCode)
                            },
                            errorCallback = {
                                updateState { copy(isAddingCode = false) }
                                emitEvent(MypageEvent.ConfirmAddCode)
                            }
                        )
                    }
                },
                errorCallback = { failState ->
                    updateState { copy(isAddingCode = false) }
                    emitEvent(MypageEvent.FailAddCode(failState.code, failState.message))
                }
            )
        }
    }


    //로그아웃 다이얼로그 호출
    fun showLogoutDialog(){
        emitEvent(MypageEvent.Logout)
    }

    //로그아웃 확인 후 실제 로그아웃 처리
    fun logout(){
        viewModelScope.launch {
            tokenSynchronizer.logout { clearAllDataUseCase() }
            emitEvent(MypageEvent.MoveToOnBoardPage)
        }
    }


}




data class MypageUiState(
    // 현재 카카오 구글 로그인 2개로 비교하니 카카오를 기준으로 view 세팅
    val userInfo: UserInfo = UserInfo(),
    val linkedPlatforms: ImmutableList<LoginType> = persistentListOf(),

    // 링크 데이터
    val githubUrl : String = "",
    val blogUrl : String = "",
    val linkedinUrl : String = "",

    // UMC 외부 링크
    val websiteUMC : String = "https://umc.it.kr",
    val instagramUMC : String = "https://www.instagram.com/uni_makeus_challenge/",
    val kakaoInquireChannelId : String = "_MDxhqX", //카카오 문의 채널

    //구글 토큰
    val googleToken : String = "",
    val kakaoToken : String = "",

    //활동 추가 코드
    val code : String = "",
    val isAddingCode : Boolean = false, //코드 추가 요청 진행 중

    //프로필 조회 실패 여부(링크가 비어 있는 것과 구분하기 위함)
    val isProfileLoadFailed : Boolean = false,

    //소셜 연동 요청 진행 중
    val isLinkingSocial : Boolean = false,

    //연동된 플랫폼 목록을 한 번이라도 불러왔는지(불러오기 전에는 소셜 연동 카드를 그리지 않는다)
    val isLinkedPlatformsLoaded : Boolean = false,

    ) : UiState {
    //앱에서 연동할 수 있는 플랫폼(카카오, 구글) 중 아직 연동되지 않은 첫 번째(카카오 우선). 연동 목록을 불러오기 전에는 null. 서버 목록에 APPLE 등이 섞여 있어도 영향받지 않는다.
    val targetPlatform: LoginType?
        get() = if (!isLinkedPlatformsLoaded) null
        else listOf(LoginType.KAKAO, LoginType.GOOGLE).firstOrNull { it !in linkedPlatforms }

    //연동 목록을 불러온 뒤, 연동할 플랫폼이 남아 있을 때만 카드 보이기(둘 다 연동되어 있으면 숨김)
    val isSocialCardVisible: Boolean
        get() = targetPlatform != null
}

sealed interface MypageEvent : UiEvent {
    //이동하기
    object NavigateToGithub : MypageEvent //깃허브 링크
    object NavigateToBlog : MypageEvent //블로그 링크
    object NavigateToLinkedin : MypageEvent //리으드인 링크

    object NavigateToEditProfile : MypageEvent //프로필 수정


    object NavigateToAddActivity : MypageEvent //활동 추가

    data class NavigateToAssistUmc(val channelId: String) : MypageEvent // UMC 문의

    object NavigateToSettingNotice : MypageEvent //알림 설정
    object NavigateToSettingLocation : MypageEvent //위치 설정

    object SocialLinked : MypageEvent //소셜 연동 성공
    data class SocialLinkFailed(val message: String) : MypageEvent //소셜 연동 실패

    object ProfileLoadFailed : MypageEvent //프로필 조회 실패 상태에서 링크 누름
    data class TermsLoadFailed(val type: TermsType) : MypageEvent //약관 조회 실패

    data class NavigateToPersonalInformation(val privacyTerms : String) : MypageEvent //개인정보
    data class NavigateToUseManual(val manualTerms : String) : MypageEvent //이용 약관

    //외부 채널 이동
    object NavigateToWebstieUmc : MypageEvent // UMC 웹사이트
    object NavigateToInstagramUmc : MypageEvent // UMC 인스타그램

    //뒤로 가기
    object NavigateToBack : MypageEvent

    //로그아웃
    object Logout : MypageEvent

    //회원 탈퇴
    object DeleteUser : MypageEvent

    //처음으로 이동
    object MoveToOnBoardPage : MypageEvent



    //챌린저 코드 다이얼로그 전용
    object ConfirmAddCode : MypageEvent
    data class FailAddCode(val code: String, val message: String): MypageEvent

    //일반 에러 토스트
    data class ShowErrorToast(val message: String): MypageEvent


}
