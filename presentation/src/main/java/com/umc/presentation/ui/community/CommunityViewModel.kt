package com.umc.presentation.ui.community

import com.umc.domain.model.enums.CategoryType
import com.umc.domain.model.enums.CommunityCategoryType
import com.umc.domain.model.enums.ContentType
import com.umc.domain.model.enums.LoginType
import com.umc.domain.model.enums.RecruitType
import com.umc.domain.model.enums.UserPart
import com.umc.domain.model.mypage.ContentItem
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class CommunityViewModel @Inject
constructor() : BaseViewModel<CommunityFragmentUiState, CommunityFragmentEvent>(
    CommunityFragmentUiState()
) {

    //모집 중 스위치 누를 때마다 상태 변화하고 필터링
    /**
    fun setRecruit(isRecruit: Boolean) {
        updateState { copy(isRecruit = isRecruit) }
        filterContents()
    }
    **/

    //탭 바꿀 때마다 탭 변화하고 필터링
    fun setNowTab(whichTab: ContentType) {
        updateState { copy(whichTab = whichTab) }
        filterContents()
    }

    // 게시글 필터링 로직
    fun filterContents() {
        updateState {
            val filtered = allContents.filter { item ->
                // 탭 기준 필터링 (ContentType 필드 활용)
                val matchesTab = when (whichTab) {
                    ContentType.ALL -> true
                    ContentType.QUESTION -> item.contentType == ContentType.QUESTION //질문 관련
                    /**TODO. 이 탭은 별도의 필터 방식이 필요 - 일단은 true로**/
                    ContentType.TOP -> true
                }

                /**
                // 모집 중 스위치 기준 필터링
                val matchesRecruit = if (isRecruit) {
                    item.recruitType == RecruitType.RECRUIT // 모집 중인 것만
                } else {
                    true // 전체 보기
                }
                **/

                //최종적으로 겹치는 것만 고르기
                matchesTab
            }

            // 최종 계산된 리스트를 nowContents에 할당
            copy(nowContents = filtered)
        }
    }
    
    //이동 로직
    fun navigateWrite(){
        emitEvent(CommunityFragmentEvent.NavigateWrite)
    }

    fun navigateSearch(){
        emitEvent(CommunityFragmentEvent.NavigateSearch)
    }


    
    

}


data class CommunityFragmentUiState(

    // 게시글 필터링 용도
    // val isRecruit: Boolean = false, //얘는 switch 여부
    val whichTab: ContentType = ContentType.ALL, //얘는 tabLayout 선택 여부


    // 임시 게시글
    val allContents: List<ContentItem> = listOf(
        ContentItem(
            category = CommunityCategoryType.QUESTION,
            region = "서울",
            contentType = ContentType.ALL,
            recruitType = RecruitType.END,
            title = "이거는 제목이에요!!!!!!!!!!!!!!!!",
            username = "어헛차",
            writeTime = "방금 전",
            likes = 0,
            comments = 1,
            content = "이거는 본문 내용이에요!!!",
            userPart = UserPart.ANDROID,
        ),
        ContentItem(
            category = CommunityCategoryType.HOBBY,
            region = "인천",
            contentType = ContentType.ALL,
            recruitType = RecruitType.RECRUIT,
            title = "이거는 제목이에요!!!!!!!!!!!!!!!!",
            username = "어헛차2호",
            writeTime = "1시간 전",
            likes = 2,
            comments = 2,
            content = "이거는 본문 내용이에요!!!",
            userPart = UserPart.WEB,
        ),
        ContentItem(
            category = CommunityCategoryType.SUGGESTION,
            region = "인천",
            contentType = ContentType.QUESTION,
            recruitType = RecruitType.RECRUIT,
            title = "이거는 제목이에요!!!!!!!!!!!!!!!!",
            username = "사람",
            writeTime = "2016.01.19",
            likes = 200,
            comments = 123,
            content = "이거는 본문 내용이에요!!!",
            userPart = UserPart.IOS,
        ),
        ContentItem(
            category = CommunityCategoryType.LIGHTNING,
            region = "인천",
            contentType = ContentType.QUESTION,
            recruitType = RecruitType.END,
            title = "밥먹고개발하고쉬고개발하고게임하고개발하고자고개발하고나는개발이너무너무너무좋아헤헤헤헤헤헿",
            username = "사람",
            writeTime = "2016.01.19",
            likes = 10,
            comments = 1,
            content = "본문내용을늘려야하는데무슨내용을적어야잘적었다는소문이날까?내용을늘리기위해서는아무내용이나넣어야겠다.이쯤되면길어지겠지?",
            userPart = UserPart.SPRING_BOOT,
        ),
    ),

    val nowContents: List<ContentItem> = listOf(),


    ) : UiState

sealed interface CommunityFragmentEvent : UiEvent {
    object NavigateWrite : CommunityFragmentEvent
    object NavigateSearch : CommunityFragmentEvent




}