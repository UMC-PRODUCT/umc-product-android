package com.umc.presentation.community.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umc.domain.model.community.CommunityThread
import com.umc.domain.model.community.CommunityThreadCategory
import com.umc.domain.model.community.CommunityThreadRole
import com.umc.domain.usecase.community.GetCommunityThreadsUseCase
import com.umc.presentation.community.model.CommunityCategory
import com.umc.presentation.community.model.CommunityThreadUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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

    fun onAction(
        action: CommunitySearchAction,
    ) {
        when (action) {
            is CommunitySearchAction.OnQueryChanged -> {
                updateQuery(
                    query = action.query,
                )
            }

            CommunitySearchAction.OnSearchClick -> {
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
        _state.update {
            it.copy(
                query = query,
                hasSearched = false,
                searchResults = emptyList(),
                errorMessage = null,
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

        _state.update {
            it.copy(
                query = trimmedQuery,
            )
        }

        requestSearch(
            query = trimmedQuery,
        )
    }

    private fun requestSearch(
        query: String,
    ) {
        if (_state.value.isLoading) {
            return
        }

        viewModelScope.launch {
            _state.update {
                it.copy(
                    query = query,
                    isLoading = true,
                    hasSearched = true,
                    searchResults = emptyList(),
                    errorMessage = null,
                )
            }

            getCommunityThreadsUseCase(
                filter = "all",
                query = query,
                offset = 0,
                limit = SEARCH_PAGE_SIZE,
            ).onSuccess { page ->
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
                        searchResults = searchResults,
                        recentSearches = addRecentSearch(
                            recentSearches =
                                currentState.recentSearches,
                            query = query,
                        ),
                        hasSearched = true,
                        isLoading = false,
                        errorMessage = null,
                    )
                }
            }.onFailure { throwable ->
                _state.update {
                    it.copy(
                        searchResults = emptyList(),
                        hasSearched = true,
                        isLoading = false,
                        errorMessage = throwable.message
                            ?: "검색 결과를 불러오지 못했어요.",
                    )
                }
            }
        }
    }

    private fun clearQuery() {
        _state.update {
            it.copy(
                query = "",
                searchResults = emptyList(),
                hasSearched = false,
                isLoading = false,
                errorMessage = null,
            )
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
                    },
            )
        }
    }

    private fun clearAllRecentSearches() {
        _state.update {
            it.copy(
                recentSearches = emptyList(),
            )
        }
    }

    private fun addRecentSearch(
        recentSearches: List<String>,
        query: String,
    ): List<String> {
        return buildList {
            add(query)

            addAll(
                recentSearches.filterNot { recentSearch ->
                    recentSearch == query
                }
            )
        }.take(MAX_RECENT_SEARCH_COUNT)
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