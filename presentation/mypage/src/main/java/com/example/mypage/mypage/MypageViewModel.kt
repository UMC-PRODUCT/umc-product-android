package com.example.mypage.mypage

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.mypage.nearby.NearbyViewModel
import com.kakao.sdk.auth.TokenManagerProvider
import com.umc.component.base.BaseViewModel
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.umc.domain.model.UserInfo
import com.umc.domain.model.enums.LoginType
import com.umc.domain.model.enums.TermsType
import com.umc.domain.model.enums.UserChallengerRole
import com.umc.domain.model.enums.UserPart
import com.umc.domain.model.home.getGisuSummaryList
import com.umc.domain.model.request.challenger.ChallengerRecordMemberRequest
import com.umc.domain.usecase.appDataStore.ClearAllDataUseCase
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

@HiltViewModel
class MypageViewModel @Inject constructor(
    private val getMyProfileUseCase: GetMyProfileUseCase, //내 프로필 정보 가져오기
    private val clearAllDataUseCase : ClearAllDataUseCase, //모든 정보 삭제하기
    private val deleteUserUseCase: DeleteUserUseCase, //회원 탈퇴
    private val getTermsByTypeUseCase: GetTermsByTypeUseCase, //타입으로 약관 가져오기
    private val getMyOAuthUseCase: GetMyOAuthUseCase, //내 OAuth 가져오기
    private val addChallengerRecordMemberUseCase: AddChallengerRecordMemberUseCase, //챌린저 코드 추가
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
                            blogUrl = userInfo.profile.blog
                        )
                    }

                },
                errorCallback = {
                    /**TODO. 에러 토스트 메시지 등을 전송**/
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
                        copy(linkedPlatforms = platforms.toImmutableList())
                    }

                },
                errorCallback = {}
            )
        }
    }

    fun navigateToGithub(){
        emitEvent(MypageEvent.NavigateToGithub)
    }
    fun navigateToLinkedin(){
        emitEvent(MypageEvent.NavigateToLinkedin)

    }
    fun navigateToBlog(){
        emitEvent(MypageEvent.NavigateToBlog)
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

    fun navigateToSocialSetting(){
        viewModelScope.launch {
            clearAllDataUseCase()
            emitEvent(MypageEvent.NavigateToSocialSetting)
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
                errorCallback = {}
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
                errorCallback = {}
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
                errorCallback = {
                    /**TODO. 에러 토스트 메시지 등을 전송**/
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
        viewModelScope.launch {
            val request = ChallengerRecordMemberRequest(
                code = uiState.value.code,
            )

            startLoading()
            resultResponse(
                response = addChallengerRecordMemberUseCase(request),
                successCallback = {
                    clearCode()
                    //유저 정보 업데이트를 위한 호출
                    viewModelScope.launch {
                        resultResponse(
                            response = getMyProfileUseCase(),
                            successCallback = {
                                emitEvent(MypageEvent.ConfirmAddCode)
                            },
                            errorCallback = {
                                emitEvent(MypageEvent.ConfirmAddCode)
                            }
                        )
                    }
                },
                errorCallback = { failState ->
                    emitEvent(MypageEvent.FailAddCode(failState.message))
                }
            )
        }
    }


    fun logout(){
        viewModelScope.launch {
            // 로그아웃 시 dataStore의 모든 데이터 삭제
            clearAllDataUseCase()
            emitEvent(MypageEvent.Logout)
        }
    }

    fun navigateToOnboard(){
        emitEvent(MypageEvent.MoveToOnBoardPage)
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


    ) : UiState {
    //둘 다 연동되어 있으면(사이즈가 2 이상이면) 카드 숨기기
    val isSocialCardVisible: Boolean
        get() = linkedPlatforms.size < 2

    //카카오가 있으면 구글을, 없으면(구글이 있거나 둘 다 없으면) 카카오
    val targetPlatform: LoginType
        get() = if (linkedPlatforms.contains(LoginType.KAKAO)) LoginType.GOOGLE else LoginType.KAKAO
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

    object NavigateToSocialSetting : MypageEvent //소셜 연동

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
    data class FailAddCode(val message: String): MypageEvent


}
