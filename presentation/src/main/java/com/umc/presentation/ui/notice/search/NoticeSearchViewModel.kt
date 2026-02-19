package com.umc.presentation.ui.notice.search

import android.view.inputmethod.EditorInfo
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NoticeSearchViewModel @Inject constructor(
    // 더미 데이터를 사용하므로 UseCase 주입 없음
) : BaseViewModel<NoticeSearchUiState, NoticeSearchEvent>(
    NoticeSearchUiState(),
) {
    // 인메모리 최근 검색어 목록 (앱 재시작 시 초기화)
    private val recentSearches = mutableListOf("데모데이", "OT", "세션")

    init {
        updateState { copy(recentSearchList = recentSearches.toList()) }
    }

    fun onClickBack() {
        emitEvent(NoticeSearchEvent.MoveToBack)
    }

    fun onImeAction(actionId: Int, text: String): Boolean {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            addRecentSearch(text)
            emitEvent(NoticeSearchEvent.MoveToSearchResult(text))
            return true
        }
        return false
    }

    fun selectRecentSearch(keyword: String) {
        addRecentSearch(keyword)
        updateState { copy(query = keyword) }
        emitEvent(NoticeSearchEvent.MoveToSearchResult(keyword))
    }

    fun deleteRecentSearch(keyword: String) {
        recentSearches.remove(keyword)
        updateState { copy(recentSearchList = recentSearches.toList()) }
    }

    fun deleteAllRecentSearch() {
        recentSearches.clear()
        updateState { copy(recentSearchList = emptyList()) }
    }

    private fun addRecentSearch(keyword: String) {
        if (keyword.isBlank()) return
        recentSearches.remove(keyword) // 중복 제거 후 맨 앞에 추가
        recentSearches.add(0, keyword)
        updateState { copy(recentSearchList = recentSearches.toList()) }
    }
}

data class NoticeSearchUiState(
    val query: String = "",
    val recentSearchList: List<String> = emptyList(),
    val selectedGisu: Long = 0
) : UiState

sealed interface NoticeSearchEvent : UiEvent {
    data class MoveToSearchResult(val search: String): NoticeSearchEvent
    data object MoveToBack: NoticeSearchEvent
}
