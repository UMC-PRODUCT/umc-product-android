package com.umc.presentation.ui.community.write

import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PostWriteViewModel @Inject
constructor() : BaseViewModel<PostWriteFragmentUiState, PostWriteFragmentEvent>(
    PostWriteFragmentUiState()
) {
}

data class PostWriteFragmentUiState(

    val dummy: Boolean = false,


    ) : UiState

sealed interface PostWriteFragmentEvent : UiEvent {
    object DummyEvent : PostWriteFragmentEvent

}