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
import com.umc.presentation.notice.loadUniqueNoticePage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.Job
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

    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            getRecentSearchNoticeUseCase().collect {
                updateState { copy(recentSearchList = it.toImmutableList()) }
            }
        }
        viewModelScope.launch {
            appDataStoreRepository.getReadNoticeIds().collect { readIds ->
                updateState { copy(readNoticeIds = readIds.toImmutableSet()) }
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
        val state = uiState.value
        if (state.gisuId == gisuId && state.noticeTab == noticeTab &&
            state.chapterId == chapterId && state.schoolId == schoolId && state.part == part
        ) return

        updateState {
            copy(
                gisuId = gisuId,
                noticeTab = noticeTab,
                chapterId = chapterId,
                schoolId = schoolId,
                part = part,
            )
        }
        if (state.isResultMode) searchNotices(isRefresh = true)
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
        searchJob?.cancel()
        updateState { copy(isResultMode = false, isPageLoading = false) }
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

    private fun searchNotices(isRefresh: Boolean) {
        val state = uiState.value
        if (!state.isResultMode || (!isRefresh && (state.isPageLoading || state.isLastPage))) return
        if (isRefresh) searchJob?.cancel()

        // gisuId는 서버 필수값이라 아직 주입 전이면 요청하지 않는다.
        val hasSearchContext = state.gisuId > 0L
        updateState {
            copy(
                resultList = if (isRefresh) persistentListOf() else resultList,
                currentPage = if (isRefresh) 0 else currentPage,
                isLastPage = if (isRefresh) false else isLastPage,
                isPageLoading = hasSearchContext,
                errorMessage = null,
                isNextPageError = false,
            )
        }
        if (!hasSearchContext) return

        searchJob = viewModelScope.launch { fetchNoticeSearch(state, isRefresh) }
    }

    private suspend fun fetchNoticeSearch(state: NoticeSearchUiState, isRefresh: Boolean) {
        val pageLoad = loadUniqueNoticePage(
            existing = if (isRefresh) emptyList() else state.resultList,
            firstPage = if (isRefresh) 0 else state.currentPage,
        ) { page ->
            searchNoticeListUseCase(
                keyword = state.query,
                gisuId = state.gisuId,
                noticeTab = state.noticeTab,
                chapterId = state.chapterId,
                schoolId = state.schoolId,
                part = state.part,
                page = page,
                size = 20,
            )
        }
        resultResponse(
            response = pageLoad.response,
            successCallback = { noticeSearch ->
                updateState {
                    copy(
                        resultList = noticeSearch.content.toImmutableList(),
                        currentPage = pageLoad.pageNumber + 1,
                        isPageLoading = false,
                        isLastPage = !noticeSearch.hasNext,
                        isNextPageError = false,
                    )
                }
            },
            errorCallback = { fail ->
                // 실패를 삼키면 0건인지 오류인지 구분할 수 없어 "검색이 안 된다"로 보인다.
                // 다음 페이지 실패는 목록 끝에 다시 시도 버튼을 띄워 직접 다시 부르게 한다
                updateState {
                    copy(
                        currentPage = pageLoad.pageNumber,
                        isPageLoading = false,
                        errorMessage = fail.message,
                        isNextPageError = !isRefresh || pageLoad.pageNumber > 0,
                    )
                }
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
    val recentSearchList: ImmutableList<String> = persistentListOf(),
    val isResultMode: Boolean = false,
    val resultList: ImmutableList<NoticeSummary> = persistentListOf(),
    val currentPage: Int = 0,
    val isPageLoading: Boolean = false,
    val isLastPage: Boolean = false,
    /** 다음 페이지 요청이 실패했는지. true면 목록 끝에 다시 시도 버튼을 보인다 */
    val isNextPageError: Boolean = false,
    val readNoticeIds: ImmutableSet<Long> = persistentSetOf(),
) : UiState

sealed interface NoticeSearchEvent : UiEvent {

    data class MoveToDetailEvent(val noticeId: Long) : NoticeSearchEvent
}
