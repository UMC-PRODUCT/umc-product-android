package com.umc.presentation.ui.community.search

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


}


data class PostSearchFragmentUiState(

    val dummy: Boolean = false,


    ) : UiState

sealed interface PostSearchFragmentEvent : UiEvent {
    object DummyEvent : PostSearchFragmentEvent

}