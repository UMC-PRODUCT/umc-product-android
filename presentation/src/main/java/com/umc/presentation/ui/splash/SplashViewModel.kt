package com.umc.presentation.ui.splash

import androidx.lifecycle.viewModelScope
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

class SplashViewModel
    @Inject
    constructor() : BaseViewModel<SplashUiState, SplashEvent>(
            SplashUiState(),
        ) {
        init {
            initFun()
        }

        private fun initFun() {
            viewModelScope.launch {
                delay(3.seconds)
                emitEvent(SplashEvent.MoveToLoginEvent)
            }
        }
    }

data class SplashUiState(
    val dummy: String = "",
) : UiState

sealed class SplashEvent : UiEvent {
    object MoveToMainEvent : SplashEvent()

    object MoveToLoginEvent : SplashEvent()
}
