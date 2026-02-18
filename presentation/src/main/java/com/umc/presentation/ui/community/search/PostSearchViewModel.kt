package com.umc.presentation.ui.community.search

import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.lifecycle.viewModelScope
import com.umc.domain.model.enums.CommunityCategoryType
import com.umc.domain.model.enums.ContentType
import com.umc.domain.model.enums.RecruitType
import com.umc.domain.model.enums.SearchMode
import com.umc.domain.model.enums.UserPart
import com.umc.domain.model.community.ContentItem
import com.umc.domain.usecase.appDataStore.recent.AddRecentSearchPostUseCase
import com.umc.domain.usecase.appDataStore.recent.ClearRecentSearchPostUseCase
import com.umc.domain.usecase.appDataStore.recent.GetRecentSearchPlaceUseCase
import com.umc.domain.usecase.appDataStore.recent.GetRecentSearchPostUseCase
import com.umc.domain.usecase.appDataStore.recent.RemoveRecentSearchPostUseCase
import com.umc.domain.usecase.community.SearchCommunityPostUseCase
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import com.umc.presentation.ui.home.PlanDetailFragmentEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class PostSearchViewModel @Inject
constructor(
    private val searchCommunityPostUseCase: SearchCommunityPostUseCase, //서버에서 검색 로직
    private val getRecentSearchPostUseCase: GetRecentSearchPostUseCase, //최근 검색 리스트 가져오기
    private val addRecentSearchPostUseCase: AddRecentSearchPostUseCase, //최근 검색 리스트 추가
    private val removeRecentSearchPostUseCase: RemoveRecentSearchPostUseCase, //최근 검색 리스트 삭제
    private val clearRecentSearchPostUseCase: ClearRecentSearchPostUseCase //최근 검색 리스트 전체 삭제

) : BaseViewModel<PostSearchFragmentUiState, PostSearchFragmentEvent>(
    PostSearchFragmentUiState()
) {

    // 초기화 : 기존 검색 리스트 호출
    init {
        viewModelScope.launch {
            getRecentSearchPostUseCase().collect {
                updateState {
                    copy(recentSearches = it)
                }
            }
        }
    }

    // 검색 창 엔터 관련 메서드 (검색 결과 시 처리)
    fun onImeAction(actionId: Int, text: String): Boolean {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {

            //기록 추가
            viewModelScope.launch {
                addRecentSearchPostUseCase(text)
            }
            
            updateState {
                copy(
                    mode = SearchMode.RESULT,
                )
            }
            // 텍스트필드 바꾸기
            emitEvent(PostSearchFragmentEvent.ShowSearchResult(uiState.value.query))
            // 검색 로직 수행
            searchPosts(text, isRefresh = true)
            
            return true
        }
        return false
    }

    //검색 로직처리 (CommunityFragment의 fetchPosts 차용)
    fun searchPosts(query: String, isRefresh: Boolean = false) {
        val state = uiState.value

        if (query.isBlank()) return
        // 로딩 중이거나, 마지막 페이지면 중단
        if (state.isPageLoading || (!isRefresh && state.isLastPage)) return

        // 상태 초기화
        updateState {
            copy(
                isPageLoading = true,
                currentPage = if (isRefresh) 0 else currentPage,
                isLastPage = if (isRefresh) false else isLastPage,
                mode = SearchMode.RESULT
            )
        }
        
        viewModelScope.launch {
            //현재 페이지
            val pageToFetch = uiState.value.currentPage

            resultResponse(
                response = searchCommunityPostUseCase(query, pageToFetch, 20),
                successCallback = { pageModel ->
                    Log.d("log_community", "$pageModel")
                    updateState {
                        copy(
                            // 새로고침이면 리스트 교체, 아니면 기존 리스트에 누적
                            searchResults = if (isRefresh) pageModel.posts else searchResults + pageModel.posts,
                            currentPage = pageToFetch + 1,
                            isPageLoading = false,
                            isLastPage = !pageModel.hasNext // 서버에서 준 마지막 페이지 여부
                        )
                    }
                },
                errorCallback = {
                    updateState { copy(isPageLoading = false) }
                }
            )
        }
    }

    // 실시간 텍스트 변경 처리 + 뷰모드도 같이 설정
    fun onQueryChanged(query: String) {
        updateState {
            //만약 이미 결과 있으면 이전 기록은 보이게 하기보이기
            val newMode = if (query.isBlank()) SearchMode.EMPTY
            else if (mode == SearchMode.RESULT && searchResults.isNotEmpty()) SearchMode.RESULT
            else SearchMode.TYPING

            copy(query = query,
                mode = newMode
            )
        }
    }

    //뷰모드 변경하기
    fun onChangeViewMode(mode: SearchMode){
        updateState {
            copy(mode = mode)
        }
    }

    // 최근 검색어 선택 시 쿼리 변경 및 결과 모드 처리
    fun selectRecentSearch(keyword: String) {
        // 클릭한 단어를 다시 저장하여 최상단으로 올림
        viewModelScope.launch {
            addRecentSearchPostUseCase(keyword)
        }

        updateState {
            copy(
                query = keyword,
                mode = SearchMode.RESULT
            )
        }
        // 검색 로직 실행 (엔터 친 것과 동일)
        emitEvent(PostSearchFragmentEvent.ShowSearchResult(uiState.value.query))
        searchPosts(keyword, isRefresh = true)
    }

    // 최근 검색어 리스트에서 X 클릭 시 -> 해당 키워드 제거
    fun deleteRecentSearch(keyword: String) {
        viewModelScope.launch {
            removeRecentSearchPostUseCase(keyword)
        }
    }

    // 최근 검색어 키워드 전체 삭제
    fun deleteAllRecentSearch(){
        viewModelScope.launch {
            clearRecentSearchPostUseCase()
        }

    }

    


    fun onClickBackPressed(){
        emitEvent(PostSearchFragmentEvent.MoveBackPressedEvent)
    }

}


data class PostSearchFragmentUiState(

    //현재 검색창에 입력한 내용
    val query : String = "",
    //뷰모드
    val mode : SearchMode = SearchMode.EMPTY,
    //최근 검색 내용(appdatastore에서 사용)
    val recentSearches: List<String> = listOf(),


    //검색 결과
    val searchResults: List<ContentItem> = emptyList(),

    // 무한 스크롤 및 로딩 제어
    val currentPage: Int = 0,           // 현재 페이지 인덱스 (0부터 시작)
    val isPageLoading: Boolean = false,  // 중복 호출 방지용 로딩 플래그
    val isLastPage: Boolean = false      // 서버 응답의 hasNext 기반 마지막 여부

    ) : UiState

sealed interface PostSearchFragmentEvent : UiEvent {
    data class ShowSearchResult(val query : String) : PostSearchFragmentEvent

    object MoveBackPressedEvent : PostSearchFragmentEvent

}