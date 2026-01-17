package com.umc.presentation.ui.mypage.profile

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
    object DummyEvent : ProfileFragmentEvent
}