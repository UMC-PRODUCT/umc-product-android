package com.umc.presentation

import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel
    @Inject
    constructor() : BaseViewModel<MainActivityUiState, MainActivityEvent>(
            MainActivityUiState(),
        ) {
        fun dummyFun() {
            updateState {
                copy(
                    dummy = "~~~",
                )
            }
        }

        fun emitFun() {
            emitEvent(MainActivityEvent.DummyEvent)
        }
    }

data class MainActivityUiState(
    val dummy: String = "",
) : UiState

sealed class MainActivityEvent : UiEvent {
    object DummyEvent : MainActivityEvent()
}
