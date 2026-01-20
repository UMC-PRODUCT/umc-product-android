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

    fun setAnomy(isOn: Boolean) {
        updateState { copy(isAnomy = isOn)
        }
    }
}



data class SuggestWriteFragmentUiState(
    val isAnomy : Boolean = true,




) : UiState

sealed interface SuggestWriteFragmentEvent : UiEvent {
    object DummyEvent : SuggestWriteFragmentEvent
}