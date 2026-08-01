package com.umc.presentation.community.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umc.presentation.community.model.CommunityCategory
import com.umc.presentation.community.model.CommunityThreadUiModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CommunitySearchViewModel : ViewModel() {

    private val _state = MutableStateFlow(
        CommunitySearchState(
            recentSearches = listOf(
                "중앙",
                "과제",
            ),
        )
    )
    val state: StateFlow<CommunitySearchState> = _state.asStateFlow()

    private val _event = Channel<CommunitySearchEvent>()
    val event = _event.receiveAsFlow()

    fun onAction(action: CommunitySearchAction) {
        when (action) {
            is CommunitySearchAction.OnQueryChanged -> {
                updateQuery(action.query)
            }

            CommunitySearchAction.OnSearchClick -> {
                search()
            }

            CommunitySearchAction.OnClearQueryClick -> {
                clearQuery()
            }

            CommunitySearchAction.OnCancelClick -> {
                sendEvent(CommunitySearchEvent.NavigateBack)
            }

            CommunitySearchAction.OnBackClick -> {
                sendEvent(CommunitySearchEvent.NavigateBack)
            }

            is CommunitySearchAction.OnRecentSearchClick -> {
                searchRecentKeyword(action.query)
            }

            is CommunitySearchAction.OnDeleteRecentSearchClick -> {
                deleteRecentSearch(action.query)
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

    private fun updateQuery(query: String) {
        _state.update {
            it.copy(
                query = query,
                hasSearched = false,
                searchResults = emptyList(),
            )
        }
    }

    private fun search() {
        val query = _state.value.query.trim()

        if (query.isBlank()) {
            return
        }

        val searchResults = dummyThreads.filter { thread ->
            thread.title.contains(
                other = query,
                ignoreCase = true,
            ) || thread.contentPreview.contains(
                other = query,
                ignoreCase = true,
            )
        }

        _state.update { currentState ->
            currentState.copy(
                query = query,
                searchResults = searchResults,
                recentSearches = addRecentSearch(
                    recentSearches = currentState.recentSearches,
                    query = query,
                ),
                hasSearched = true,
                isLoading = false,
            )
        }
    }

    private fun searchRecentKeyword(query: String) {
        val searchResults = dummyThreads.filter { thread ->
            thread.title.contains(
                other = query,
                ignoreCase = true,
            ) || thread.contentPreview.contains(
                other = query,
                ignoreCase = true,
            )
        }

        _state.update { currentState ->
            currentState.copy(
                query = query,
                searchResults = searchResults,
                recentSearches = addRecentSearch(
                    recentSearches = currentState.recentSearches,
                    query = query,
                ),
                hasSearched = true,
                isLoading = false,
            )
        }
    }

    private fun clearQuery() {
        _state.update {
            it.copy(
                query = "",
                searchResults = emptyList(),
                hasSearched = false,
                isLoading = false,
            )
        }
    }

    private fun deleteRecentSearch(query: String) {
        _state.update { currentState ->
            currentState.copy(
                recentSearches = currentState.recentSearches.filterNot {
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

    private fun sendEvent(event: CommunitySearchEvent) {
        viewModelScope.launch {
            _event.send(event)
        }
    }

    companion object {
        private const val MAX_RECENT_SEARCH_COUNT = 10
    }

    private val dummyThreads = listOf(
        CommunityThreadUiModel(
            id = "1",
            title = "iOS 3주차 과제 인증방",
            contentPreview = "다들 과제 인증 완료했나요? 오늘 자정까지...",
            category = CommunityCategory.STUDY,
            icon = "",
            dayText = "화요일",
            memberCount = 5,
            unreadCount = 4,
            maxMembers = 8,
            isPinned = true,
        ),
        CommunityThreadUiModel(
            id = "2",
            title = "정기 모임 일정 안내",
            contentPreview = "이번 주 중앙 정기 모임 일정을 안내합니다.",
            category = CommunityCategory.PROJECT,
            icon = "",
            dayText = "화요일",
            memberCount = 8,
            unreadCount = 3,
            maxMembers = 10,
            isPinned = true,
        ),
        CommunityThreadUiModel(
            id = "3",
            title = "OT 장소 변경 안내",
            contentPreview = "OT 장소가 변경되어 안내드립니다.",
            category = CommunityCategory.PROJECT,
            icon = "",
            dayText = "화요일",
            memberCount = 6,
            unreadCount = 0,
            maxMembers = 10,
        ),
        CommunityThreadUiModel(
            id = "4",
            title = "리액트 상태관리 질문 있어요",
            contentPreview = "상태관리 과제를 하다가 궁금한 점이 생겼어요.",
            category = CommunityCategory.QNA,
            icon = "",
            dayText = "화요일",
            memberCount = 4,
            unreadCount = 3,
            maxMembers = 8,
        ),
        CommunityThreadUiModel(
            id = "5",
            title = "이번 주 회식 어때요?",
            contentPreview = "이번 주 과제 끝나고 다 같이 회식해요.",
            category = CommunityCategory.FREE,
            icon = "",
            dayText = "화요일",
            memberCount = 7,
            unreadCount = 3,
            maxMembers = 10,
        ),
    )
}