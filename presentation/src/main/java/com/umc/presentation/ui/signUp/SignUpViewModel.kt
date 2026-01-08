package com.umc.presentation.ui.signUp

import androidx.lifecycle.viewModelScope
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import com.umc.presentation.util.ULog
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

    fun onClick() {
        updateState {
            uiState.value
        }
    }

    fun onNameChanged(name: String) {
        updateState { copy(name = name) }
    }

    fun onNicknameChanged(nickname: String) {
        updateState { copy(nickname = nickname) }
    }

    fun onEmailChanged(email: String) {
        updateState { copy(email = email) }
    }

    fun onCodeChanged(code: String) {
        updateState { copy(code = code) }
    }

}

data class SignUpState(
    var name: String = "",
    var nickname: String = "",
    var email: String = "",
    var code: String = "",
) : UiState {
    val enableNextButton: Boolean
        get() = name.isNotEmpty() && nickname.isNotEmpty() && email.isNotEmpty()
}

sealed class SignUpEvent : UiEvent {
    object MoveToMainEvent : SignUpEvent()

    object MoveToLoginEvent : SignUpEvent()
}
