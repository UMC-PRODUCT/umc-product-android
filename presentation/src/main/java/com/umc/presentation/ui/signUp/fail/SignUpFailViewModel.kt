package com.umc.presentation.ui.signUp.fail

import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SignUpFailViewModel
@Inject
constructor() : BaseViewModel<UiState, SignUpFailEvent>(
    UiState.Default,
) {
    fun onClickBack() {
        emitEvent(SignUpFailEvent.MoveToBack)
    }

    fun onClickHomePage() {
        emitEvent(SignUpFailEvent.MoveToHomePage)
    }
}

sealed interface SignUpFailEvent : UiEvent {
    object MoveToBack : SignUpFailEvent
    object MoveToHomePage : SignUpFailEvent
}
