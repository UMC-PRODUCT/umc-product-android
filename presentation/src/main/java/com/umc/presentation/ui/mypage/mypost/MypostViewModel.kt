package com.umc.presentation.ui.mypage.mypost

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
        ContentItem(
            category = CommunityCategoryType.HOBBY,
            region = "인천",
            contentType = ContentType.ALL,
            recruitType = RecruitType.RECRUIT,
            title = "동작구에 분위기 있는 카페 있나요?",
            username = "어헛차2호",
            writeTime = "1시간 전",
            likes = 2,
            comments = 2,
            content = "궁금합니다.",
            userPart = UserPart.WEB,
        ),
        ContentItem(
            category = CommunityCategoryType.SUGGESTION,
            region = "인천",
            contentType = ContentType.QUESTION,
            recruitType = RecruitType.RECRUIT,
            title = "이번 IOS 스터디 일정 바꾸는 게 어떤가요?",
            username = "사람",
            writeTime = "2016.01.19",
            likes = 200,
            comments = 123,
            content = "제가 여행 일정이 있어서 바꾸는 게 좋을 거 같아요.",
            userPart = UserPart.IOS,
        ),
        ContentItem(
            category = CommunityCategoryType.LIGHTNING,
            region = "인천",
            contentType = ContentType.QUESTION,
            recruitType = RecruitType.END,
            title = "인천 보드게임 동아리 2차 번개 모집",
            username = "사람",
            writeTime = "2016.01.19",
            likes = 10,
            comments = 1,
            content = "2026.01.30 예정. 너만 오면 ㄱㄱ",
            userPart = UserPart.SPRING_BOOT,
        ),
    ),
): UiState

sealed interface MypostFragmentEvent : UiEvent {
    object ClickBackPressed : MypostFragmentEvent
}
