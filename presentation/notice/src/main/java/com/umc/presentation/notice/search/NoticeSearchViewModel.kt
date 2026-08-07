package com.umc.presentation.notice.search

import androidx.lifecycle.viewModelScope
import com.umc.component.base.BaseViewModel
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.umc.domain.model.notice.NoticeSummary
import com.umc.domain.repository.AppDataStoreRepository
import com.umc.domain.usecase.appDataStore.recent.AddRecentSearchNoticeUseCase
import com.umc.domain.usecase.appDataStore.recent.ClearRecentSearchNoticeUseCase
import com.umc.domain.usecase.appDataStore.recent.GetRecentSearchNoticeUseCase
import com.umc.domain.usecase.appDataStore.recent.RemoveRecentSearchNoticeUseCase
import com.umc.domain.usecase.notice.SearchNoticeListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoticeSearchViewModel @Inject constructor(
    private val getRecentSearchNoticeUseCase: GetRecentSearchNoticeUseCase,
    private val addRecentSearchNoticeUseCase: AddRecentSearchNoticeUseCase,
    private val removeRecentSearchNoticeUseCase: RemoveRecentSearchNoticeUseCase,
    private val clearRecentSearchNoticeUseCase: ClearRecentSearchNoticeUseCase,
    private val searchNoticeListUseCase: SearchNoticeListUseCase,
    private val appDataStoreRepository: AppDataStoreRepository,
) : BaseViewModel<NoticeSearchUiState, NoticeSearchEvent>(
    NoticeSearchUiState(),
) {

    init {
        viewModelScope.launch {
            getRecentSearchNoticeUseCase().collect {
                updateState { copy(recentSearchList = it) }
            }
        }
        viewModelScope.launch {
            appDataStoreRepository.getReadNoticeIds().collect { readIds ->
                updateState { copy(readNoticeIds = readIds) }
            }
        }
    }

    /** 검색 대상 기수는 공지 화면에서 nav argument로 전달받아 주입 */
    fun setGisuId(gisuId: Long) {
        updateState { copy(gisuId = gisuId) }
    }

    fun onQueryChanged(query: String) {
        updateState { copy(query = query) }
    }

    /** 검색 실행. 최근 검색어에 저장 후 결과 모드로 전환 */
    fun onSearch(keyword: String = uiState.value.query) {
        if (keyword.isBlank()) return

        viewModelScope.launch {
            addRecentSearchNoticeUseCase(keyword)
        }
        updateState { copy(query = keyword, isResultMode = true) }
        searchNotices(isRefresh = true)
    }

    /** 최근 검색어 선택 시 해당 키워드로 바로 검색 */
    fun selectRecentSearch(keyword: String) {
        onSearch(keyword)
    }

    fun deleteRecentSearch(keyword: String) {
        viewModelScope.launch {
            removeRecentSearchNoticeUseCase(keyword)
        }
    }

    fun deleteAllRecentSearch() {
        viewModelScope.launch {
            clearRecentSearchNoticeUseCase()
        }
    }

    /** 결과 화면에서 뒤로가기 시 검색어 입력 모드로 복귀 */
    fun backToSearchMode() {
        updateState { copy(isResultMode = false) }
    }

    /** 공지 클릭 시 읽음 처리 후 상세로 이동 */
    fun onClickNotice(noticeId: Long) {
        viewModelScope.launch {
            appDataStoreRepository.addReadNoticeId(noticeId)
        }
        emitEvent(NoticeSearchEvent.MoveToDetailEvent(noticeId))
    }

    fun loadNextPage() {
        if (!uiState.value.isPageLoading && !uiState.value.isLastPage) {
            searchNotices(isRefresh = false)
        }
    }

    private fun searchNotices(isRefresh: Boolean) = viewModelScope.launch {
        val state = uiState.value

        if (state.isPageLoading || (!isRefresh && state.isLastPage)) return@launch

        updateState { copy(isPageLoading = true) }

        val pageToFetch = if (isRefresh) 0 else state.currentPage

        resultResponse(
            response = searchNoticeListUseCase(
                keyword = state.query,
                gisuId = state.gisuId,
                page = pageToFetch,
                size = 20
            ),
            successCallback = { noticeSearch ->
                updateState {
                    copy(
                        resultList = if (isRefresh) noticeSearch.content else resultList + noticeSearch.content,
                        currentPage = pageToFetch + 1,
                        isPageLoading = false,
                        isLastPage = !noticeSearch.hasNext,
                    )
                }
            },
            errorCallback = {
                updateState { copy(isPageLoading = false) }
            }
        )
    }
}

data class NoticeSearchUiState(
    val gisuId: Long = 0,
    val query: String = "",
    val recentSearchList: List<String> = emptyList(),
    val isResultMode: Boolean = false,
    val resultList: List<NoticeSummary> = emptyList(),
    val currentPage: Int = 0,
    val isPageLoading: Boolean = false,
    val isLastPage: Boolean = false,
    val readNoticeIds: Set<Long> = emptySet(),
) : UiState

sealed interface NoticeSearchEvent : UiEvent {

    data class MoveToDetailEvent(val noticeId: Long) : NoticeSearchEvent
}
