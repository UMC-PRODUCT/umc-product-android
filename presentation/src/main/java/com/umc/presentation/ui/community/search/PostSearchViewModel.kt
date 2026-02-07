package com.umc.presentation.ui.community.search

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
    private val getRecentSearchPostUseCase: GetRecentSearchPostUseCase,
    private val addRecentSearchPostUseCase: AddRecentSearchPostUseCase,
    private val removeRecentSearchPostUseCase: RemoveRecentSearchPostUseCase,
    private val clearRecentSearchPostUseCase: ClearRecentSearchPostUseCase

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
            /**TODO 서버에서 처리한 결과가 들어가기**/
            emitEvent(PostSearchFragmentEvent.ShowSearchResult(uiState.value.query))
            return true
        }
        return false
    }

    // 실시간 텍스트 변경 처리
    fun onQueryChanged(query: String) {
        updateState {
            copy(query = query)
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

    //뷰모드 변경하기
    fun onChangeViewMode(mode: SearchMode){
        updateState {
            copy(mode = mode)
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
    //최근 검색 내용
    val recentSearches: List<String> = listOf(),
    //검색 결과
    val searchResults: List<ContentItem> = emptyList(),

    ) : UiState

sealed interface PostSearchFragmentEvent : UiEvent {
    data class ShowSearchResult(val query : String) : PostSearchFragmentEvent

    object MoveBackPressedEvent : PostSearchFragmentEvent

}