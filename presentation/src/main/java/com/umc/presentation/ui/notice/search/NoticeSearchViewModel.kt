package com.umc.presentation.ui.notice.search

import android.view.inputmethod.EditorInfo
import androidx.lifecycle.viewModelScope
import com.umc.domain.usecase.appDataStore.recent.AddRecentSearchNoticeUseCase
import com.umc.domain.usecase.appDataStore.recent.ClearRecentSearchNoticeUseCase
import com.umc.domain.usecase.appDataStore.recent.GetRecentSearchNoticeUseCase
import com.umc.domain.usecase.appDataStore.recent.RemoveRecentSearchNoticeUseCase
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoticeSearchViewModel @Inject constructor(
    private val getRecentSearchNoticeUseCase: GetRecentSearchNoticeUseCase,
    private val addRecentSearchNoticeUseCase: AddRecentSearchNoticeUseCase,
    private val removeRecentSearchNoticeUseCase: RemoveRecentSearchNoticeUseCase,
    private val clearRecentSearchNoticeUseCase: ClearRecentSearchNoticeUseCase,
) : BaseViewModel<NoticeSearchUiState, NoticeSearchEvent>(
    NoticeSearchUiState(),
) {
    init {
        viewModelScope.launch {
            getRecentSearchNoticeUseCase().collect {
                updateState {
                    copy(recentSearchList = it)
                }
            }
        }
    }

    fun onClickBack() {
        emitEvent(NoticeSearchEvent.MoveToBack)
    }

    fun onImeAction(actionId: Int, text: String): Boolean {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            viewModelScope.launch {
                addRecentSearchNoticeUseCase(text)
            }
            emitEvent(NoticeSearchEvent.MoveToSearchResult(text))
            return true
        }
        return false
    }

    fun selectRecentSearch(keyword: String) {
        viewModelScope.launch {
            addRecentSearchNoticeUseCase(keyword)
        }
        updateState {
            copy(
                query = keyword,
            )
        }
        emitEvent(NoticeSearchEvent.MoveToSearchResult(keyword))
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
