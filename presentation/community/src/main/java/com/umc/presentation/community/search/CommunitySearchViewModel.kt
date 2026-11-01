package com.umc.presentation.community.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umc.component.theme.AppStrings
import com.umc.domain.model.community.CommunityThread
import com.umc.domain.model.community.CommunityThreadCategory
import com.umc.domain.model.community.CommunityThreadRole
import com.umc.domain.model.community.canEditThread
import com.umc.domain.usecase.community.GetCommunityThreadsUseCase
import com.umc.presentation.community.model.CommunityCategory
import com.umc.presentation.community.model.CommunityThreadUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


/**
 * 커뮤니티 스레드 검색 화면의 상태와 검색 로직을 관리하는 ViewModel
 *
 * 주요 기능
 * - 검색어 입력 및 초기화
 * - 스레드 검색 API 호출
 * - 최근 검색어 추가 및 삭제
 * - 검색 결과 상태 관리
 * - 스레드 상세 화면 이동 이벤트 전달
 */
@HiltViewModel
class CommunitySearchViewModel @Inject constructor(
    private val getCommunityThreadsUseCase: GetCommunityThreadsUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(
        CommunitySearchState()
    )


    val state: StateFlow<CommunitySearchState> =
        _state.asStateFlow()

    private val _event = Channel<CommunitySearchEvent>()
    val event = _event.receiveAsFlow()
    private var searchGeneration = 0L

    fun onAction(
        action: CommunitySearchAction,
    ) {
        when (action) {
            CommunitySearchAction.OnLoadMore -> loadMore()
            is CommunitySearchAction.OnQueryChanged -> {
                updateQuery(
                    query = action.query,
                )
            }

            CommunitySearchAction.OnSearchClick -> {
                search()
            }

            CommunitySearchAction.OnRetryClick -> {
                search()
            }

            CommunitySearchAction.OnClearQueryClick -> {
                clearQuery()
            }

            CommunitySearchAction.OnCancelClick,
            CommunitySearchAction.OnBackClick,
                -> {
                sendEvent(
                    CommunitySearchEvent.NavigateBack
                )
            }

            is CommunitySearchAction.OnRecentSearchClick -> {
                searchRecentKeyword(
                    query = action.query,
                )
            }

            is CommunitySearchAction.OnDeleteRecentSearchClick -> {
                deleteRecentSearch(
                    query = action.query,
                )
            }

            CommunitySearchAction.OnClearAllRecentSearchesClick -> {
                clearAllRecentSearches()
            }

            is CommunitySearchAction.OnThreadClick -> {
                sendEvent(
                    CommunitySearchEvent.NavigateToThreadDetail(
                        threadId = action.threadId,
                    )
                )
            }
        }
    }

    private fun updateQuery(
        query: String,
    ) {
        searchGeneration++
        _state.update {
            it.copy(
                query = query,
                hasSearched = false,
                searchResults = persistentListOf(),
                errorMessage = null,
                isLoading = false,
                isLoadingMore = false,
                nextOffset = null,
                loadMoreError = null,
            )
        }
    }

    private fun search() {
        val query = _state.value.query.trim()

        if (query.isBlank()) {
            return
        }

        requestSearch(
            query = query,
        )
    }

    private fun searchRecentKeyword(
        query: String,
    ) {
        val trimmedQuery = query.trim()

        if (trimmedQuery.isBlank()) {
            return
        }

        updateQuery(trimmedQuery)

        requestSearch(
            query = trimmedQuery,
        )
    }

    private fun requestSearch(
        query: String,
    ) {
        val generation = ++searchGeneration

        viewModelScope.launch {
            if (generation != searchGeneration) return@launch
            _state.update {
                it.copy(
                    query = query,
                    isLoading = true,
                    hasSearched = true,
                    searchResults = if (it.query == query) it.searchResults else persistentListOf(),
                    errorMessage = null,
                    isLoadingMore = false,
                    nextOffset = if (it.query == query) it.nextOffset else null,
                    loadMoreError = null,
                )
            }

            getCommunityThreadsUseCase(
                filter = "all",
                query = query,
                offset = 0,
                limit = SEARCH_PAGE_SIZE,
            ).mapCatching { page -> page to validatedSearchOffset(page.nextOffset, 0) }
                .onSuccess { (page, nextOffset) ->
                if (generation != searchGeneration) return@onSuccess
                val searchResults = buildList {
                    addAll(
                        page.pinnedThreads.map { thread ->
                            thread.toSearchUiModel(
                                forcePinned = true,
                            )
                        }
                    )

                    addAll(
                        page.threads.map { thread ->
                            thread.toSearchUiModel(
                                forcePinned = false,
                            )
                        }
                    )
                }.distinctBy { thread ->
                    thread.id
                }

                _state.update { currentState ->
                    currentState.copy(
                        query = query,
                        searchResults = searchResults.toImmutableList(),
                        recentSearches = addRecentSearch(
                            recentSearches =
                                currentState.recentSearches,
                            query = query,
                        ),
                        hasSearched = true,
                        isLoading = false,
                        errorMessage = null,
                        nextOffset = nextOffset,
                    )
                }
            }.onFailure { throwable ->
                if (generation != searchGeneration) return@onFailure
                _state.update {
                    it.copy(
                        hasSearched = true,
                        isLoading = false,
                        errorMessage = throwable.message
                            ?: AppStrings.COMMUNITY_SEARCH_RESULTS_LOAD_ERROR,
                    )
                }
            }
        }
    }

    private fun clearQuery() {
        searchGeneration++
        _state.update {
            it.copy(
                query = "",
                searchResults = persistentListOf(),
                hasSearched = false,
                isLoading = false,
                errorMessage = null,
                isLoadingMore = false,
                nextOffset = null,
                loadMoreError = null,
            )
        }
    }

    private fun loadMore() {
        val current = _state.value
        val offset = current.nextOffset ?: return
        if (current.isLoading || current.isLoadingMore || !current.hasSearched) return
        val generation = searchGeneration
        _state.update { it.copy(isLoadingMore = true, loadMoreError = null) }
        viewModelScope.launch {
            getCommunityThreadsUseCase("all", current.query, offset, SEARCH_PAGE_SIZE)
                .mapCatching { page -> page to validatedSearchOffset(page.nextOffset, offset) }
                .onSuccess { (page, nextOffset) ->
                    if (generation != searchGeneration) return@onSuccess
                    val incoming = page.pinnedThreads.map { it.toSearchUiModel(true) } +
                        page.threads.map { it.toSearchUiModel(false) }
                    _state.update {
                        it.copy(
                            searchResults = (it.searchResults + incoming).distinctBy { row -> row.id }.toImmutableList(),
                            nextOffset = nextOffset,
                            isLoadingMore = false,
                        )
                    }
                }.onFailure {
                    if (generation != searchGeneration) return@onFailure
                    _state.update { it.copy(isLoadingMore = false, loadMoreError = AppStrings.COMMUNITY_SEARCH_MORE_LOAD_ERROR) }
                }
        }
    }

    private fun deleteRecentSearch(
        query: String,
    ) {
        _state.update { currentState ->
            currentState.copy(
                recentSearches =
                    currentState.recentSearches.filterNot {
                        it == query
                    }.toImmutableList(),
            )
        }
    }

    private fun clearAllRecentSearches() {
        _state.update {
            it.copy(
                recentSearches = persistentListOf(),
            )
        }
    }

    private fun addRecentSearch(
        recentSearches: ImmutableList<String>,
        query: String,
    ): ImmutableList<String> {
        return buildList {
            add(query)

            addAll(
                recentSearches.filterNot { recentSearch ->
                    recentSearch == query
                }
            )
        }.take(MAX_RECENT_SEARCH_COUNT).toImmutableList()
    }

    private fun sendEvent(
        event: CommunitySearchEvent,
    ) {
        viewModelScope.launch {
            _event.send(event)
        }
    }

    companion object {
        private const val MAX_RECENT_SEARCH_COUNT = 10
        private const val SEARCH_PAGE_SIZE = 20
    }
}

private fun CommunityThread.toSearchUiModel(
    forcePinned: Boolean,
): CommunityThreadUiModel {
    return CommunityThreadUiModel(
        id = threadId,
        title = title,

        // 스레드 특징이 아니라 마지막 메시지만 표시
        contentPreview = lastMessage?.preview
            ?.takeIf { preview ->
                preview.isNotBlank()
            }
            .orEmpty(),

        category = category.toSearchUiCategory(),
        icon = icon,

        // 마지막 메시지가 있으면 해당 메시지 시간 사용
        dayText = lastMessage?.createdAt
            ?.toSearchDayText()
            ?: updatedAt.toSearchDayText(),

        memberCount = memberCount,
        unreadCount = unreadCount,
        maxMembers = maxMembers,
        isPinned = forcePinned || isPinned,
        isNotificationEnabled = !isMuted,
        isMine = myRole == CommunityThreadRole.OWNER,
        canEdit = myRole.canEditThread(),
        isJoined = isJoined,
    )
}

private fun CommunityThreadCategory.toSearchUiCategory():
        CommunityCategory {
    return when (this) {
        CommunityThreadCategory.STUDY -> {
            CommunityCategory.STUDY
        }

        CommunityThreadCategory.QNA -> {
            CommunityCategory.QNA
        }

        CommunityThreadCategory.PROJECT -> {
            CommunityCategory.PROJECT
        }

        CommunityThreadCategory.FREE,
        CommunityThreadCategory.UNKNOWN,
            -> {
            CommunityCategory.FREE
        }
    }
}

private fun String.toSearchDayText(): String {
    return runCatching {
        OffsetDateTime.parse(this).format(
            DateTimeFormatter.ofPattern("MM.dd")
        )
    }.getOrDefault("")
}
