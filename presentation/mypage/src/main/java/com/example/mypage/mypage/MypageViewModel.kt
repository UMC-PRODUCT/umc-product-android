package com.example.mypage.mypage

import android.util.Log
import androidx.lifecycle.viewModelScope
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

    // 서버에서 내 정보 가져오기
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
                    settingUserInfoToUI(userInfo)

                },
                errorCallback = {
                    /**TODO. 에러 토스트 메시지 등을 전송**/
                }
            )
        }
    }


    //UserInfo를 받아았을 때 이를 파싱해서 UI 요소로 분할하는 함수
    fun settingUserInfoToUI(userInfo: UserInfo){
        // 기수별 정보가 담긴 것.
        val gisuSummaryList = userInfo.getGisuSummaryList()

        // 최신기수를 가져오기
        val latestGisu = gisuSummaryList.maxByOrNull { it.gisu }

        latestGisu?.let { summary ->
            //권위 or 챌린저에서 1개 선택
            val representativeItem = summary.fromRoles.firstOrNull() ?: summary.fromRecords.firstOrNull()

            val positionString = representativeItem?.let { item ->
                //파트명 변환 (UserPart Enum 활용, 없으면 빈 문자열)
                val partLabel = runCatching { UserPart.valueOf(item.responsiblePart ?: "").label }
                    .getOrNull()?.let { "$it " } ?: ""

                //직함명 변환 (displayName이 null이면 원본 role 사용)
                val roleEnum = UserChallengerRole.from(item.role)
                val roleLabel = roleEnum.displayName ?: item.role

                //최종 포맷: "N기 Part Role"
                "${summary.gisu}기 $partLabel$roleLabel"
            } ?: "${summary.gisu}기 챌린저" // 예외 상황 대비 기본값

            updateState {
                copy(
                    myRecentCarrer = positionString
                )
            }

        }

    }

    //소셜 정보(OAuth 받아오기)
    fun getUserOAuth(){
        viewModelScope.launch {
            resultResponse(
                response = getMyOAuthUseCase(),
                successCallback = { myOAuth ->
                    val platforms = myOAuth.map { LoginType.valueOf(it.provider) }
                    updateState {
                        copy(linkedPlatforms = platforms)
                    }

                },
                errorCallback = {}
            )
        }
    }

    //usecase를 통해 appdatastore에 저장된 내용 날리기
    fun deleteAllData(){
        viewModelScope.launch {
            clearAllDataUseCase()
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

    fun navigateToEditProfile(){
        emitEvent(MypageEvent.NavigateToEditProfile)
    }

    fun navigateToMypost(){
        emitEvent(MypageEvent.NavigateToMypost)
    }
    fun navigateToMyComment(){
        emitEvent(MypageEvent.NavigateToMyComment)
    }
    fun navigateToScrap(){
        emitEvent(MypageEvent.NavigateToScrap)
    }

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



    //유저 삭제 다이얼로그 호출
    fun showDeleteUserDialog(){
        //1. 삭제 이벤트를 전송
        emitEvent(MypageEvent.DeleteUser)
    }


    //카카오 및 구글 토큰 get(회원 탈퇴)
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

    //2. 유저 삭제 로직(여기서 실징 수행)
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

    //코드 추가 다이얼로그에서 바꿀 때
    fun onCodeChanged(code: String) {
        updateState { copy(code = code) }
    }

    //챌린저 코드를 추가할 떄 서버에게 보내고 유저 정보 업데이트 로직
    fun addChallengerCode() {
        viewModelScope.launch {
            val request = ChallengerRecordMemberRequest(
                code = uiState.value.code,
            )

            startLoading()
            resultResponse(
                response = addChallengerRecordMemberUseCase(request),
                successCallback = {
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




}




data class MypageUiState(
    // 현재 카카오 구글 로그인 2개로 비교하니 카카오를 기준으로 view 세팅
    val userInfo: UserInfo = UserInfo(),
    val linkedPlatforms: List<LoginType> = emptyList(),

    // 현재 직책
    val myRecentCarrer : String = "",

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
    object NavigateToMypost : MypageEvent //내가 쓴 글
    object NavigateToMyComment : MypageEvent //내가 쓴 댓글
    object NavigateToScrap : MypageEvent //스크랩

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
