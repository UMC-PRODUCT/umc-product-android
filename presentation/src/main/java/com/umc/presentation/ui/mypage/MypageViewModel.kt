package com.umc.presentation.ui.mypage

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.umc.domain.model.UserInfo
import com.umc.domain.model.enums.HomeViewMode
import com.umc.domain.model.enums.LoginType
import com.umc.domain.repository.AppDataStoreRepository
import com.umc.domain.usecase.appDataStore.GetUserInfoUseCase
import com.umc.domain.usecase.appDataStore.GetUserOutLinkUseCase
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MypageViewModel @Inject constructor(
    private val getUserOutLinkUseCase: GetUserOutLinkUseCase,
    private val getUserInfoUseCase: GetUserInfoUseCase,
) : BaseViewModel<MypageFragmentUiState, MypageFragmentEvent>(
    MypageFragmentUiState()){


    //초기 상태
    init {
        viewModelScope.launch {
           getUserOutLinkUseCase().collect { outLink ->
                updateState {
                    copy(
                        githubUrl = outLink.github,
                        linkedinUrl = outLink.linkedin,
                        blogUrl = outLink.blog
                    )
                }
            }
        }
        viewModelScope.launch {
            getUserInfoUseCase().collect { userInfo ->
                Log.d("log_home", "DataStore로부터 읽어옴: $userInfo")
                updateState {
                    copy(
                        userInfo = userInfo,
                    )
                }

            }
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

    fun navigateToSuggetion(){
        emitEvent(MypageFragmentEvent.NavigateToSuggetion)
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
        emitEvent(MypageFragmentEvent.NavigateToAssistUmc)
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

    fun navigateToPersonalInformation(){
        emitEvent(MypageFragmentEvent.NavigateToPersonalInformation)
    }
    
    fun navigateToUseManual(){
        emitEvent(MypageFragmentEvent.NavigateToUseManual)
    }
    
    fun navigateToWebsiteUmc(){
        emitEvent(MypageFragmentEvent.NavigateToWebstieUmc)
    }

    fun navigateToInstagramUmc(){
        emitEvent(MypageFragmentEvent.NavigateToInstagramUmc)
    }


    fun deleteUser(){
        emitEvent(MypageFragmentEvent.DeleteUser)
    }

    fun logout(){
        emitEvent(MypageFragmentEvent.Logout)
    }




}




data class MypageFragmentUiState(
    // 현재 카카오 구글 로그인 2개로 비교하니 카카오를 기준으로 view 세팅
    val userInfo: UserInfo = UserInfo(),
    val loginType: LoginType = LoginType.KAKAO,
    
    // 현재 직책
    val myCareer : List<String> = listOf("8기 Android 챌린저", "9기 Android 중앙 파트장", "9기 칸 맞추기 기다란 텍스트"),
    val myRecentCarrer : String = "9기 Android 중앙 파트장",

    // 링크 데이터
    val githubUrl : String = "",
    val blogUrl : String = "",
    val linkedinUrl : String = "",

    // UMC 외부 링크
    val websiteUMC : String = "https://umc.makeus.in",
    val instagramUMC : String = "https://www.instagram.com/uni_makeus_challenge/",
    
) : UiState

sealed interface MypageFragmentEvent : UiEvent {
    //이동하기
    object NavigateToGithub : MypageFragmentEvent //깃허브 링크
    object NavigateToBlog : MypageFragmentEvent //블로그 링크
    object NavigateToLinkedin : MypageFragmentEvent //리으드인 링크

    object NavigateToEditProfile : MypageFragmentEvent //프로필 수정
    object NavigateToSuggetion : MypageFragmentEvent //중앙 건의함
    object NavigateToMypost : MypageFragmentEvent //내가 쓴 글
    object NavigateToMyComment : MypageFragmentEvent //내가 쓴 댓글
    object NavigateToScrap : MypageFragmentEvent //스크랩
    
    object NavigateToAssistUmc : MypageFragmentEvent // UMC 문의

    object NavigateToSettingNotice : MypageFragmentEvent //알림 설정
    object NavigateToSettingLocation : MypageFragmentEvent //위치 설정

    object NavigateToSocialSetting : MypageFragmentEvent //소셜 연동

    object NavigateToPersonalInformation : MypageFragmentEvent //개인정보
    object NavigateToUseManual : MypageFragmentEvent //이용 약관
    
    //외부 채널 이동
    object NavigateToWebstieUmc : MypageFragmentEvent // UMC 웹사이트
    object NavigateToInstagramUmc : MypageFragmentEvent // UMC 인스타그램

    //로그아웃
    object Logout : MypageFragmentEvent

    //회원 탈퇴
    object DeleteUser : MypageFragmentEvent

}
