package com.umc.presentation.ui.mypage.suggest

import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class SuggestWriteViewModel @Inject
constructor() : BaseViewModel<SuggestWriteFragmentUiState, SuggestWriteFragmentEvent>(
    SuggestWriteFragmentUiState()){
}



data class SuggestWriteFragmentUiState(
    val dummy: String = "",
) : UiState

sealed interface SuggestWriteFragmentEvent : UiEvent {
    object DummyEvent : SuggestWriteFragmentEvent
}