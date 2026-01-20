package com.umc.presentation.ui.mypage.suggest

import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class SuggestViewModel @Inject
constructor() : BaseViewModel<SuggestFragmentUiState, SuggestFragmentEvent>(
    SuggestFragmentUiState()){
}



data class SuggestFragmentUiState(
    val dummy: String = "",
) : UiState

sealed interface SuggestFragmentEvent : UiEvent {
    object DummyEvent : SuggestFragmentEvent
}