package com.umc.presentation.ui.mypage.profile

import android.net.Uri
import com.umc.domain.model.enums.LoginType
import com.umc.domain.model.mypage.UserActiveItem
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class ProfileViewModel @Inject
constructor() : BaseViewModel<ProfileFragmentUiState, ProfileFragmentEvent>(
    ProfileFragmentUiState()){

    //프로필 이미지 수정했을 때 이벤트
    fun onClickProfileImage(){
        emitEvent(ProfileFragmentEvent.ClickProfileImage)
    }

    //viewModel에서 갤러리->이미지 가져온 후 처리 이벤트
    fun updateProfileImage(uri: Uri){
        //이미지 업데이트하고 이벤트 쏘기
        updateState { copy(profileImageUri = uri) }
    }

    //완료 누르고 뒤로 가기
    fun onClickComplete(){
        emitEvent(ProfileFragmentEvent.ClickComplete)
    }

    //그냥 뒤로 가기
    fun onClickBackPressed(){
        emitEvent(ProfileFragmentEvent.ClickBackPressed)
    }



}


data class ProfileFragmentUiState(
    //유저 정보 (고정)
    val loginType: LoginType = LoginType.KAKAO,
    val nickname: String = "어헛차/박유수",
    val mySchool: String = "숭실대학교",
    val myPart: String = "9기/Android",

    //유저 정보 (가변)
    val githubLink: String = "",
    val linkedinLink: String = "",
    val blogLink: String = "",
    val profileImageUri : Uri? = null,

    //임시 정보
    val myActiveHistory: List<UserActiveItem> = listOf(
        UserActiveItem(
            generation = "8기",
            partName = "Android Part",
            position = "챌린저"),
        UserActiveItem(
            generation = "9기",
            partName = "Android Part",
            position = "파트장"),
        UserActiveItem(
            generation = "9기",
            partName = "Android Part",
            position = "파트장"),
    ),

) : UiState

sealed interface ProfileFragmentEvent : UiEvent {
    
    //갤러리 터치 이벤트
    object ClickProfileImage : ProfileFragmentEvent

    //이미지 업데이트 이벤트
    data class UpdateProfileImage(val uri: Uri) : ProfileFragmentEvent

    //완료 버튼을 눌렀을 때
    object ClickComplete : ProfileFragmentEvent

    //그냥 뒤로 가기
    object ClickBackPressed : ProfileFragmentEvent
}