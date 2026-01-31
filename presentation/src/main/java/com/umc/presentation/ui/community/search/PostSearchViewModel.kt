package com.umc.presentation.ui.community.search

import android.view.inputmethod.EditorInfo
import android.widget.Toast
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class PostSearchViewModel @Inject
constructor() : BaseViewModel<PostSearchFragmentUiState, PostSearchFragmentEvent>(
    PostSearchFragmentUiState()
) {


    // 검색 창 엔터 관련 메서드
    fun onImeAction(actionId: Int, text: String): Boolean {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            emitEvent(PostSearchFragmentEvent.ShowSearchResult)
            return true
        }
        return false
    }


}


data class PostSearchFragmentUiState(

    val dummy: Boolean = false,


    ) : UiState

sealed interface PostSearchFragmentEvent : UiEvent {
    object ShowSearchResult : PostSearchFragmentEvent

}