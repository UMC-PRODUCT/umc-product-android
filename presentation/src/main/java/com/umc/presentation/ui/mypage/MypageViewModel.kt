package com.umc.presentation.ui.mypage

import androidx.lifecycle.viewModelScope
import com.umc.domain.model.ChallengerRecord
import com.umc.domain.model.ProfileInfo
import com.umc.domain.model.UserInfo
import com.umc.domain.model.UserRole
import com.umc.domain.model.enums.LoginType
import com.umc.domain.model.enums.TermsType
import com.umc.domain.model.enums.UserChallengerRole
import com.umc.domain.model.enums.UserPart
import com.umc.domain.model.home.getGisuSummaryList
import com.umc.domain.usecase.appDataStore.ClearAllDataUseCase
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
) : BaseViewModel<MypageFragmentUiState, MypageFragmentEvent>(
    MypageFragmentUiState()){


    //초기 상태
    init {
        viewModelScope.launch {
            //유저 정보 가져오기
            //getUserInfo()

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
        emitEvent(MypageFragmentEvent.NavigateToSocialSetting)
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
        emitEvent(MypageFragmentEvent.Logout)
    }




}




data class MypageFragmentUiState(
    // 현재 카카오 구글 로그인 2개로 비교하니 카카오를 기준으로 view 세팅
    val userInfo: UserInfo = UserInfo(
        id = 12345L,
        name = "박유수",
        nickname = "어헛차",
        email = "uhutcha@ssu.ac.kr", // 숭실대 이메일 예시
        schoolId = 7130L,
        schoolName = "숭실대학교",
        profileImageLink = "", // 요청하신 대로 빈 값 처리
        status = "ACTIVE",
        roles = emptyList(),
        challengerRecords = listOf(
            ChallengerRecord(
                101,
                12345,
                9,
                9,
                1,
                "9기 안드로이드",
                "ANDROID",
                "ACTIVE",
                emptyList(),
                "박유수",
                "어헛차",
                "uhutcha@ssu.ac.kr",
                7130,
                "숭실대학교",
                "",
                "ACTIVE"
            ),
            ChallengerRecord(201, 12345, 8, 8, 2, "8기 안드로이드", "ANDROID", "COMPLETED", emptyList(), "박유수", "어헛차", "uhutcha@ssu.ac.kr", 7130, "숭실대학교", "", "ACTIVE")
        ),
        profile = ProfileInfo(
            id = 1,
            linkedIn = "https://linkedin.com/in/park-yu-su",
            instagram = "@uhutcha_dev",
            github = "https://github.com/Park-yu-su",
            blog = "https://blog.naver.com/PostList.naver?blogId=uhutcha_7130",
            personal = "https://parkyusu.me"
        )
    ),
    val loginType: LoginType = LoginType.KAKAO,
    
    // 현재 직책
    val myRecentCarrer : String = "9기 Android 중앙 파트장",

    // 링크 데이터
    val githubUrl : String = "https://github.com/Park-yu-su",
    val blogUrl : String = "https://blog.naver.com/PostList.naver?blogId=uhutcha_7130",
    val linkedinUrl : String = "",

    // UMC 외부 링크
    val websiteUMC : String = "https://umc.it.kr",
    val instagramUMC : String = "https://www.instagram.com/uni_makeus_challenge/",
    val kakaoInquireChannelId : String = "_xjqxcln", //카카오 문의 채널
    
    
) : UiState

sealed interface MypageFragmentEvent : UiEvent {
    //이동하기
    object NavigateToGithub : MypageFragmentEvent //깃허브 링크
    object NavigateToBlog : MypageFragmentEvent //블로그 링크
    object NavigateToLinkedin : MypageFragmentEvent //리으드인 링크

    object NavigateToEditProfile : MypageFragmentEvent //프로필 수정
    object NavigateToMypost : MypageFragmentEvent //내가 쓴 글
    object NavigateToMyComment : MypageFragmentEvent //내가 쓴 댓글
    object NavigateToScrap : MypageFragmentEvent //스크랩
    
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
