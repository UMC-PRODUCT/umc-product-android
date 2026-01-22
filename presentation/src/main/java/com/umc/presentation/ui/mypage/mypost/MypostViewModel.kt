package com.umc.presentation.ui.mypage.mypost

import com.umc.domain.model.enums.CategoryType
import com.umc.domain.model.enums.ContentType
import com.umc.domain.model.enums.RecruitType
import com.umc.domain.model.mypage.ContentItem
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import com.umc.presentation.ui.mypage.profile.ProfileFragmentEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject



@HiltViewModel
class MypostViewModel @Inject
constructor() : BaseViewModel<MypostFragmentUiState, MypostFragmentEvent>(
    MypostFragmentUiState()){


    fun onClickBackPressed(){
        emitEvent(MypostFragmentEvent.ClickBackPressed)
    }


}


data class MypostFragmentUiState(
    val tmpData: List<ContentItem> = listOf(
        ContentItem(
            category = CategoryType.MEETING,
            region = "서울",
            contentType = ContentType.ALL,
            recruitType = RecruitType.END,
            title = "이거는 제목이에요!!!!!!!!!!!!!!!!",
            username = "어헛차",
            writeTime = "방금 전",
            likes = "0",
            comments = "1",
        ),
        ContentItem(
            category = CategoryType.STUDY,
            region = "인천",
            contentType = ContentType.ALL,
            recruitType = RecruitType.RECRUIT,
            title = "이거는 제목이에요!!!!!!!!!!!!!!!!",
            username = "어헛차2호",
            writeTime = "1시간 전",
            likes = "2",
            comments = "2",
        ),
        ContentItem(
            category = CategoryType.WORKSHOP,
            region = "인천",
            contentType = ContentType.QUESTION,
            recruitType = RecruitType.RECRUIT,
            title = "이거는 제목이에요!!!!!!!!!!!!!!!!",
            username = "사람",
            writeTime = "2016.01.19",
            likes = "200",
            comments = "123",
        ),
        ContentItem(
            category = CategoryType.HACKATHON,
            region = "인천",
            contentType = ContentType.QUESTION,
            recruitType = RecruitType.END,
            title = "밥먹고개발하고쉬고개발하고게임하고개발하고자고개발하고나는개발이너무너무너무좋아헤헤헤헤헤헿",
            username = "사람",
            writeTime = "2016.01.19",
            likes = "10",
            comments = "110",
        ),
    )
): UiState

sealed interface MypostFragmentEvent : UiEvent {
    object ClickBackPressed : MypostFragmentEvent
}
