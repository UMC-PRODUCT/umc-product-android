package com.umc.presentation.ui.mypage

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.umc.domain.model.UserInfo
import com.umc.domain.model.enums.LoginType
import com.umc.domain.model.enums.TermsType
import com.umc.domain.model.enums.UserChallengerRole
import com.umc.domain.model.enums.UserPart
import com.umc.domain.model.home.getGisuSummaryList
import com.umc.domain.usecase.appDataStore.ClearAllDataUseCase
import com.umc.domain.usecase.authentication.GetMyOAuthUseCase
import com.umc.domain.usecase.member.DeleteUserUseCase
import com.umc.domain.usecase.member.GetMyProfileUseCase
import com.umc.domain.usecase.terms.GetTermsByTypeUseCase
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
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
) : BaseViewModel<MypageFragmentUiState, MypageFragmentEvent>(
    MypageFragmentUiState()){


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
        emitEvent(MypageFragmentEvent.NavigateToGithub)
    }
    fun navigateToLinkedin(){
        emitEvent(MypageFragmentEvent.NavigateToLinkedin)

    }
    fun navigateToBlog(){
        emitEvent(MypageFragmentEvent.NavigateToBlog)
    }

    fun navigateToEditProfile(){
        emitEvent(MypageFragmentEvent.NavigateToEditProfile)
    }

    fun navigateToMypost(){
        emitEvent(MypageFragmentEvent.NavigateToMypost)
    }
    fun navigateToMyComment(){
        emitEvent(MypageFragmentEvent.NavigateToMyComment)
    }
    fun navigateToScrap(){
        emitEvent(MypageFragmentEvent.NavigateToScrap)
    }

    fun navigateToAddActivity(){
        emitEvent(MypageFragmentEvent.NavigateToAddActivity)
    }


    fun navigateToAssistUmc(){
        emitEvent(MypageFragmentEvent.NavigateToAssistUmc(uiState.value.kakaoInquireChannelId))
    }


    fun navigateToSettingNotice(){
        emitEvent(MypageFragmentEvent.NavigateToSettingNotice)
    }

    fun navigateToSettingLocation(){
        emitEvent(MypageFragmentEvent.NavigateToSettingLocation)
    }

    fun navigateToSocialSetting(){
        viewModelScope.launch {
            clearAllDataUseCase()
            emitEvent(MypageFragmentEvent.NavigateToSocialSetting)
        }

    }

    //개인정보처리 방침
    fun navigateToPersonalInformation(){
        viewModelScope.launch {
            resultResponse(
                response = getTermsByTypeUseCase(TermsType.PRIVACY),
                successCallback = { term ->
                    emitEvent(MypageFragmentEvent.NavigateToPersonalInformation(term.link))
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
                    emitEvent(MypageFragmentEvent.NavigateToUseManual(term.link))
                },
                errorCallback = {}
            )
        }


    }
    
    fun navigateToWebsiteUmc(){
        emitEvent(MypageFragmentEvent.NavigateToWebstieUmc)
    }

    fun navigateToInstagramUmc(){
        emitEvent(MypageFragmentEvent.NavigateToInstagramUmc)
    }

    fun navigateToOnBoardPage(){
        emitEvent(MypageFragmentEvent.MoveToOnBoardPage)
    }



    //유저 삭제 다이얼로그 호출
    fun showDeleteUserDialog(){
        //1. 삭제 이벤트를 전송
        emitEvent(MypageFragmentEvent.DeleteUser)
    }

    //2. 유저 삭제 로직(여기서 실징 수행)
    fun deleteUser(){
        viewModelScope.launch {
            resultResponse(
                response = deleteUserUseCase(),
                successCallback = {
                    viewModelScope.launch {
                        // 회원 탈퇴 성공 시 dataStore의 모든 데이터 삭제
                        clearAllDataUseCase()
                        emitEvent(MypageFragmentEvent.MoveToOnBoardPage)
                    }
                },
                errorCallback = {
                    /**TODO. 에러 토스트 메시지 등을 전송**/ 
                }
            )
        }
    }



    fun logout(){
        viewModelScope.launch {
            // 로그아웃 시 dataStore의 모든 데이터 삭제
            clearAllDataUseCase()
            emitEvent(MypageFragmentEvent.Logout)
        }
    }




}




data class MypageFragmentUiState(
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
    
    
) : UiState {
    //둘 다 연동되어 있으면(사이즈가 2 이상이면) 카드 숨기기
    val isSocialCardVisible: Boolean
        get() = linkedPlatforms.size < 2

    //카카오가 있으면 구글을, 없으면(구글이 있거나 둘 다 없으면) 카카오
    val targetPlatform: LoginType
        get() = if (linkedPlatforms.contains(LoginType.KAKAO)) LoginType.GOOGLE else LoginType.KAKAO
}

sealed interface MypageFragmentEvent : UiEvent {
    //이동하기
    object NavigateToGithub : MypageFragmentEvent //깃허브 링크
    object NavigateToBlog : MypageFragmentEvent //블로그 링크
    object NavigateToLinkedin : MypageFragmentEvent //리으드인 링크

    object NavigateToEditProfile : MypageFragmentEvent //프로필 수정
    object NavigateToMypost : MypageFragmentEvent //내가 쓴 글
    object NavigateToMyComment : MypageFragmentEvent //내가 쓴 댓글
    object NavigateToScrap : MypageFragmentEvent //스크랩

    object NavigateToAddActivity : MypageFragmentEvent //활동 추가
    
    data class NavigateToAssistUmc(val channelId: String) : MypageFragmentEvent // UMC 문의

    object NavigateToSettingNotice : MypageFragmentEvent //알림 설정
    object NavigateToSettingLocation : MypageFragmentEvent //위치 설정

    object NavigateToSocialSetting : MypageFragmentEvent //소셜 연동

    data class NavigateToPersonalInformation(val privacyTerms : String) : MypageFragmentEvent //개인정보
    data class NavigateToUseManual(val manualTerms : String) : MypageFragmentEvent //이용 약관
    
    //외부 채널 이동
    object NavigateToWebstieUmc : MypageFragmentEvent // UMC 웹사이트
    object NavigateToInstagramUmc : MypageFragmentEvent // UMC 인스타그램



    //로그아웃
    object Logout : MypageFragmentEvent

    //회원 탈퇴
    object DeleteUser : MypageFragmentEvent

    //처음으로 이동
    object MoveToOnBoardPage : MypageFragmentEvent


}
