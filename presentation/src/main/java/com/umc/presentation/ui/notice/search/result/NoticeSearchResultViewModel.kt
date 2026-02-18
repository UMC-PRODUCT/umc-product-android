package com.umc.presentation.ui.notice.search.result

import android.view.inputmethod.EditorInfo
import androidx.lifecycle.viewModelScope
import com.umc.domain.model.notice.NoticeSummary
import com.umc.domain.usecase.notice.SearchNoticeListUseCase
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import com.umc.presentation.ui.notice.search.NoticeSearchEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoticeSearchResultViewModel
@Inject
constructor(
    private val searchNoticeListUseCase: SearchNoticeListUseCase
) : BaseViewModel<NoticeSearchResultUiState, NoticeSearchResultEvent>(
    NoticeSearchResultUiState(),
) {

    fun initSearch(query: String, gisuId: Long) {
        updateState {
            copy(searchText = query, selectedGisu = gisuId)
        }
        searchNotices(query, isRefresh = true)
    }

    fun searchNotices(query: String, isRefresh: Boolean = false) {
        val state = uiState.value

        if (query.isBlank()) return
        // 로딩 중이거나, 마지막 페이지면 중단
        if (state.isPageLoading || (!isRefresh && state.isLastPage)) return

        // 상태 초기화
        updateState {
            copy(
                isPageLoading = true,
                currentPage = if (isRefresh) 0 else currentPage,
                isLastPage = if (isRefresh) false else isLastPage
            )
        }

        viewModelScope.launch {
            val pageToFetch = uiState.value.currentPage
            val activeGisu = state.selectedGisu

            resultResponse(
                response = searchNoticeListUseCase(
                    keyword = query,
                    gisuId = activeGisu,
                    page = pageToFetch,
                    size = 20
                ),
                successCallback = { noticeSearch ->
                    updateState {
                        copy(
                            // 새로고침이면 리스트 교체, 아니면 기존 리스트에 누적
                            noticeList = if (isRefresh) noticeSearch.content else noticeList + noticeSearch.content,
                            currentPage = pageToFetch + 1,
                            isPageLoading = false,
                            isLastPage = !noticeSearch.hasNext
                        )
                    }
                },
                errorCallback = {
                    updateState { copy(isPageLoading = false) }
                }
            )
        }
    }

    fun onImeAction(actionId: Int, text: String): Boolean {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            viewModelScope.launch {
                searchNotices(text, true)
            }
            return true
        }
        return false
    }

    fun loadNextPage() {
        if (!uiState.value.isPageLoading && !uiState.value.isLastPage) {
            searchNotices(uiState.value.searchText, isRefresh = false)
        }
    }

    fun onClickBack() {
        emitEvent(NoticeSearchResultEvent.MoveToBack)
    }

    fun onClickNotice(noticeId: Long) {
        emitEvent(NoticeSearchResultEvent.MoveToNoticeDetail(noticeId))
    }
}

data class NoticeSearchResultUiState(
    val noticeList: List<NoticeSummary> = emptyList(),
    val searchText: String = "",
    val currentPage: Int = 0,
    val isPageLoading: Boolean = false,
    val isLastPage: Boolean = false,
    val selectedGisu: Long = 0
) : UiState

sealed interface NoticeSearchResultEvent : UiEvent {
    data object MoveToBack : NoticeSearchResultEvent
    data class MoveToNoticeDetail(val noticeId: Long) : NoticeSearchResultEvent
}
