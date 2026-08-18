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

    /**
     * 검색 조건은 목록 화면에서 nav argument로 전달받아 주입한다.
     * 명세상 검색은 "필터 조건이 전체 조회와 동일하게 적용"되므로 탭·소속·파트를 함께 보내야
     * 운영진 공지 화면에서 들어온 검색이 챌린저 공지만 뒤지는 문제가 생기지 않는다
     */
    fun setSearchContext(
        gisuId: Long,
        noticeTab: String,
        chapterId: Long?,
        schoolId: Long?,
        part: String?,
    ) {
        updateState {
            copy(
                gisuId = gisuId,
                noticeTab = noticeTab,
                chapterId = chapterId,
                schoolId = schoolId,
                part = part,
            )
        }
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
        // 이전 검색 결과가 남아 있으면 검색어를 바꿔도 그대로 보여 오해를 준다
        updateState {
            copy(
                query = keyword,
                isResultMode = true,
                resultList = emptyList(),
                currentPage = 0,
                isLastPage = false,
                errorMessage = null,
            )
        }
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
        // gisuId는 서버 필수값이라 아직 주입 전이면 요청하지 않는다 (400 방지)
        if (state.gisuId <= 0L) return@launch

        updateState { copy(isPageLoading = true, errorMessage = null) }

        val pageToFetch = if (isRefresh) 0 else state.currentPage

        resultResponse(
            response = searchNoticeListUseCase(
                keyword = state.query,
                gisuId = state.gisuId,
                noticeTab = state.noticeTab,
                chapterId = state.chapterId,
                schoolId = state.schoolId,
                part = state.part,
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
            errorCallback = { fail ->
                // 실패를 삼키면 0건인지 오류인지 구분할 수 없어 "검색이 안 된다"로 보인다
                updateState { copy(isPageLoading = false, errorMessage = fail.message) }
            }
        )
    }
}

data class NoticeSearchUiState(
    val gisuId: Long = 0,
    val noticeTab: String = "CHALLENGER",
    val chapterId: Long? = null,
    val schoolId: Long? = null,
    val part: String? = null,
    val errorMessage: String? = null,
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
