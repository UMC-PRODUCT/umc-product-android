package com.umc.presentation.ui.mypage.mypost

import com.umc.domain.model.enums.MycontentType
import com.umc.domain.model.mypage.MyContentItem
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class MypostViewModel @Inject
constructor() : BaseViewModel<MypostFragmentUiState, MypostFragmentEvent>(
    MypostFragmentUiState()){
}


data class MypostFragmentUiState(
    val tmpData: List<MyContentItem> = listOf(
        MyContentItem(
            category = "만남",
            region = "서울",
            status = MycontentType.RECRUIT,
            title = "이거는 제목이에요!!!!!!!!!!!!!!!!",
            username = "어헛차",
            writeTime = "방금 전",
            likes = "0",
            comments = "1"
        ),
        MyContentItem(
            category = "스터디",
            region = "인천",
            status = MycontentType.RECRUIT,
            title = "이거는 제목이에요!!!!!!!!!!!!!!!!",
            username = "어헛차2호",
            writeTime = "1시간 전",
            likes = "2",
            comments = "2"
        ),
        MyContentItem(
            category = "밥",
            region = "인천",
            status = MycontentType.END,
            title = "이거는 제목이에요!!!!!!!!!!!!!!!!",
            username = "사람",
            writeTime = "2016.01.19",
            likes = "200",
            comments = "123"
        ),
    ),

    ) : UiState

sealed interface MypostFragmentEvent : UiEvent {
    object DummyEvent : MypostFragmentEvent
}
