package com.umc.presentation.ui.mypage.mycomment

import com.umc.domain.model.enums.MyContentType
import com.umc.domain.model.mypage.MyContentItem
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
    val tmpData: List<MyContentItem> = listOf(
        MyContentItem(
            category = "만남",
            region = "서울",
            status = MyContentType.RECRUIT,
            title = "이거는 제목이에요!!!!!!!!!!!!!!!!",
            username = "어헛차",
            writeTime = "방금 전",
            likes = "0",
            comments = "1",
            isSoft = false
        ),
        MyContentItem(
            category = "스터디",
            region = "인천",
            status = MyContentType.RECRUIT,
            title = "이거는 제목이에요!!!!!!!!!!!!!!!!",
            username = "어헛차2호",
            writeTime = "1시간 전",
            likes = "2",
            comments = "2",
            isSoft = true
        ),
        MyContentItem(
            category = "밥",
            region = "인천",
            status = MyContentType.END,
            title = "이거는 제목이에요!!!!!!!!!!!!!!!!",
            username = "사람",
            writeTime = "2016.01.19",
            likes = "200",
            comments = "123",
            isSoft = false
        ),
    ),
) : UiState

sealed interface MycommentFragmentEvent : UiEvent {
    object ClickBackPressed : MycommentFragmentEvent

}
