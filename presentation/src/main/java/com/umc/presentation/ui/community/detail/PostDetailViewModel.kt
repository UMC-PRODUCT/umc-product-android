package com.umc.presentation.ui.community.detail

import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class PostDetailViewModel @Inject
constructor() : BaseViewModel<PostDetailFragmentUiState, PostDetailFragmentEvent>(
    PostDetailFragmentUiState()
) {


    //각 데이터를 조합

}


data class PostDetailFragmentUiState(

    val dummy: Boolean = false,


    ) : UiState

sealed interface PostDetailFragmentEvent : UiEvent {
    object DummyEvent : PostDetailFragmentEvent

}