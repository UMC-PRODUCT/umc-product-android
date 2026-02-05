package com.umc.presentation.ui.mypage.mycomment

import com.umc.domain.model.enums.CategoryType
import com.umc.domain.model.enums.CommunityCategoryType
import com.umc.domain.model.enums.ContentType
import com.umc.domain.model.enums.RecruitType
import com.umc.domain.model.enums.UserPart
import com.umc.domain.model.mypage.ContentItem
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import com.umc.presentation.ui.mypage.profile.ProfileFragmentEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class MycommentViewModel @Inject
constructor() : BaseViewModel<MycommentFragmentUiState, MycommentFragmentEvent>(
    MycommentFragmentUiState()){

    fun onClickBackPressed(){
        emitEvent(MycommentFragmentEvent.ClickBackPressed)
    }

}


data class MycommentFragmentUiState(
    val tmpData: List<ContentItem> = listOf(
        ContentItem(
            category = CommunityCategoryType.QUESTION,
            region = "서울",
            contentType = ContentType.ALL,
            recruitType = RecruitType.END,
            title = "BottomSheet에 대해 질문이 있습니다.",
            username = "어헛차",
            writeTime = "방금 전",
            likes = 0,
            comments = 1,
            content = "하단 bottom이 넘치는 문제가 있는데, wrap_content로 어떻게 막나요?",
            userPart = UserPart.ANDROID,
        ),

    ),
) : UiState

sealed interface MycommentFragmentEvent : UiEvent {
    object ClickBackPressed : MycommentFragmentEvent

}
