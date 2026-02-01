package com.umc.presentation.ui.notice.search

import android.view.inputmethod.EditorInfo
import com.umc.domain.model.enums.NoticeCategory
import com.umc.domain.model.notice.Notice
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NoticeSearchViewModel
@Inject
constructor() : BaseViewModel<NoticeSearchUiState, NoticeSearchEvent>(
    NoticeSearchUiState(),
) {
    init {
        updateSearchList(getDummy())
    }

    fun onClickBack() {
        emitEvent(NoticeSearchEvent.MoveToBack)
    }

    private fun updateSearchList(list: List<String>) {
        updateState {
            copy(
                recentSearchList = list
            )
        }
    }

    private fun getDummy(): List<String> {
        return listOf(
            "더미1", "더미2", "더미3", "더미4"
        )
    }

    fun onImeAction(actionId: Int, text: String): Boolean {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            emitEvent(NoticeSearchEvent.MoveToSearchResult(text))
            return true
        }
        return false
    }
}

data class NoticeSearchUiState(
    val recentSearchList: List<String> = emptyList()
) : UiState

sealed interface NoticeSearchEvent : UiEvent {
    data class MoveToSearchResult(val search: String): NoticeSearchEvent
    data object MoveToBack: NoticeSearchEvent
}
