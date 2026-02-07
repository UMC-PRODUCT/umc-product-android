package com.umc.presentation.ui.community

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.umc.domain.model.enums.CategoryType
import com.umc.domain.model.enums.CommunityCategoryType
import com.umc.domain.model.enums.ContentType
import com.umc.domain.model.enums.LoginType
import com.umc.domain.model.enums.RecruitType
import com.umc.domain.model.enums.UserPart
import com.umc.domain.model.community.ContentItem
import com.umc.domain.usecase.community.GetCommunityPostUseCase
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class CommunityViewModel @Inject
constructor(
    private val getCommunityPostUseCase: GetCommunityPostUseCase,
) : BaseViewModel<CommunityFragmentUiState, CommunityFragmentEvent>(
    CommunityFragmentUiState()
) {


    //탭 바꿀 때마다 탭 변화하고 필터링
    fun setNowTab(whichTab: ContentType) {
        updateState { copy(whichTab = whichTab) }
        fetchPosts(isRefresh = true)
    }
    

    //게시글 가져오기(isRefesh = 다 지우고 새로 가져오기)
    fun fetchPosts(isRefresh: Boolean = false) {
        val state = uiState.value

        // 브레이크 로직: 로딩 중이거나, 새로고침이 아닌데 이미 마지막 페이지면 실행 안 함
        if (state.isPageLoading || (!isRefresh && state.isLastPage)) return

        // 호출 전 상태 업데이트
        if (isRefresh) {
            updateState { copy(isPageLoading = true, currentPage = 0, isLastPage = false) }
        } else {
            updateState { copy(isPageLoading = true) }
        }

        viewModelScope.launch {
            val isIng = state.whichTab == ContentType.LIGHTNING // 번개 탭은 ing=true
            val pageToFetch = if (isRefresh) 0 else state.currentPage

            resultResponse(
                response = getCommunityPostUseCase(isIng, "ALL", pageToFetch, 20),
                successCallback = { pageModel ->
                    // 1. 질문 탭이면 클라이언트에서 한 번 더 필터링
                    val rawPosts = pageModel.posts
                    val filteredPosts = if (state.whichTab == ContentType.QUESTION) {
                        rawPosts.filter { it.category == CommunityCategoryType.QUESTION }
                    } else {
                        rawPosts
                    }

                    updateState { copy(
                        // 2. 새로고침이면 리스트 교체, 아니면 기존 리스트에 누적
                        nowContents = if (isRefresh) filteredPosts else nowContents + filteredPosts,
                        currentPage = pageToFetch + 1, //다음 탭을 표시
                        isPageLoading = false,
                        isLastPage = !pageModel.hasNext // 다음 페이지 유무 업데이트
                    ) }
                },
                errorCallback = {
                    updateState { copy(isPageLoading = false) }
                }
            )
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
    val whichTab: ContentType = ContentType.ALL, //얘는 tabLayout 선택 여부

    // 현재 탭에 맞는 게시글들
    val nowContents: List<ContentItem> = listOf(),

    // 무한 스크롤 및 로딩 제어
    val currentPage: Int = 0,           // 현재 페이지 인덱스 (0부터 시작)
    val isPageLoading: Boolean = false,  // 중복 호출 방지용 로딩 플래그
    val isLastPage: Boolean = false      // 서버 응답의 hasNext 기반 마지막 여부


    ) : UiState

sealed interface CommunityFragmentEvent : UiEvent {
    object NavigateWrite : CommunityFragmentEvent
    object NavigateSearch : CommunityFragmentEvent




}