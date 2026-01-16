package com.umc.presentation.ui.mypage

import com.umc.domain.model.enums.HomeViewMode
import com.umc.domain.model.enums.LoginType
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MypageViewModel @Inject
constructor() : BaseViewModel<MypageFragmentUiState, MypageFragmentEvent>(
    MypageFragmentUiState()){

    fun goGithub(){
        emitEvent(MypageFragmentEvent.goGithub)
    }
    fun goLinkedin(){
        emitEvent(MypageFragmentEvent.goLinkedin)

    }
    fun goBlog(){
        emitEvent(MypageFragmentEvent.goBlog)
    }

    fun goEditProfile(){
        emitEvent(MypageFragmentEvent.goEditProfile)
    }

    fun goSuggetion(){
        emitEvent(MypageFragmentEvent.goSuggetion)
    }

    fun goMypost(){
        emitEvent(MypageFragmentEvent.goMypost)
    }
    fun goMyComment(){
        emitEvent(MypageFragmentEvent.goMyComment)
    }
    fun goScrap(){
        emitEvent(MypageFragmentEvent.goScrap)
    }


    fun logout(){
        emitEvent(MypageFragmentEvent.logout)
    }




}




data class MypageFragmentUiState(
    // 현재 카카오 구글 로그인 2개로 비교하니 카카오를 기준으로 view 세팅
    val loginType: LoginType = LoginType.KAKAO,
    
    // 현재 직책
    val myCareer : List<String> = listOf("8기 Android 챌린저", "9기 Android 중앙 파트장", "9기 칸 맞추기 기다란 텍스트"),

    // 알람을 송수신할건지
    val isAlarmOn : Boolean = false,
    
) : UiState

sealed interface MypageFragmentEvent : UiEvent {
    //이동하기
    object goGithub : MypageFragmentEvent //깃허브 링크
    object goBlog : MypageFragmentEvent //블로그 링크
    object goLinkedin : MypageFragmentEvent //리으드인 링크

    object goEditProfile : MypageFragmentEvent //프로필 수정
    object goSuggetion : MypageFragmentEvent //중앙 건의함
    object goMypost : MypageFragmentEvent //내가 쓴 글
    object goMyComment : MypageFragmentEvent //내가 쓴 댓글
    object goScrap : MypageFragmentEvent //스크랩


    //로그아웃
    object logout : MypageFragmentEvent

}
