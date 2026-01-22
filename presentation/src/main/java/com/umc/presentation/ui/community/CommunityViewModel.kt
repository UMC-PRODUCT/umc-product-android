package com.umc.presentation.ui.community

import com.umc.domain.model.enums.CommunityType
import com.umc.domain.model.enums.LoginType
import com.umc.domain.model.enums.MyContentType
import com.umc.domain.model.mypage.MyContentItem
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
    fun setRecruit(isRecruit: Boolean) {
        updateState { copy(isRecruit = isRecruit) }
        filterContents()
    }

    //탭 바꿀 때마다 탭 변화하고 필터링
    fun setNowTab(whichTab: CommunityType) {
        updateState { copy(whichTab = whichTab) }
        filterContents()
    }

    // 게시글 필터링 로직
    fun filterContents() {
        updateState {
            val filtered = allContents.filter { item ->
                // 탭 기준 필터링 (isSoft 필드 활용)
                val matchesTab = when (whichTab) {
                    CommunityType.ALL -> true
                    CommunityType.SOFT -> item.isSoft    // 지식 관련
                    CommunityType.HARD -> !item.isSoft   // 지식 외
                    /**TODO. 이 탭은 별도의 필터 방식이 필요 - 일단은 true로**/
                    CommunityType.TOP -> true
                }

                // 모집 중 스위치 기준 필터링
                val matchesRecruit = if (isRecruit) {
                    item.status == MyContentType.RECRUIT // 모집 중인 것만
                } else {
                    true // 전체 보기
                }

                //최종적으로 겹치는 것만 고르기
                matchesTab && matchesRecruit
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
    val isRecruit: Boolean = false,
    val whichTab: CommunityType = CommunityType.ALL,


    // 임시 게시글
    val allContents: List<MyContentItem> = listOf(
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
        MyContentItem(
            category = "스터디",
            region = "인천",
            status = MyContentType.END,
            title = "밥먹고개발하고쉬고개발하고게임하고개발하고자고개발하고나는개발이너무너무너무좋아헤헤헤헤헤헿",
            username = "사람",
            writeTime = "2016.01.19",
            likes = "10",
            comments = "110",
            isSoft = true
        ),
    ),

    val nowContents: List<MyContentItem> = listOf(),


    ) : UiState

sealed interface CommunityFragmentEvent : UiEvent {
    object NavigateWrite : CommunityFragmentEvent
    object NavigateSearch : CommunityFragmentEvent




}