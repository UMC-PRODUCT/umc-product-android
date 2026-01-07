package com.umc.presentation.ui.signUp

import androidx.lifecycle.viewModelScope
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

class SignUpViewModel
@Inject
constructor() : BaseViewModel<SignUpState, SignUpEvent>(
    SignUpState(),
) {
    init {
        initFun()
    }

    private fun initFun() {
        viewModelScope.launch {
            delay(3.seconds)
            emitEvent(SignUpEvent.MoveToLoginEvent)
        }
    }
}

data class SignUpState(
    var name: String = "",
) : UiState

sealed class SignUpEvent : UiEvent {
    object MoveToMainEvent : SignUpEvent()
    object MoveToLoginEvent : SignUpEvent()
}
