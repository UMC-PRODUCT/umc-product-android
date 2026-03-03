package com.umc.presentation.ui.signUp.fail.code

import androidx.lifecycle.viewModelScope
import com.umc.domain.model.request.challenger.ChallengerRecordMemberRequest
import com.umc.domain.usecase.challenger.AddChallengerRecordMemberUseCase
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpFailCodeViewModel @Inject constructor(
    private val addChallengerRecordMemberUseCase: AddChallengerRecordMemberUseCase,
) : BaseViewModel<SignUpFailCodeState, SignUpFailCodeEvent>(
    SignUpFailCodeState(),
) {
    fun onClickBack() {
        emitEvent(SignUpFailCodeEvent.MoveToBack)
    }

    fun onCodeChanged(code: String) {
        updateState { copy(code = code) }
    }

    fun register() = viewModelScope.launch {
        val request = ChallengerRecordMemberRequest(
            code = uiState.value.code,
        )

        resultResponse(
            response = addChallengerRecordMemberUseCase(request),
            successCallback = {
                emitEvent(SignUpFailCodeEvent.MoveToHome)
            },
            errorCallback = { failState ->
                emitEvent(SignUpFailCodeEvent.ShowErrorToast(failState.message))
            }
        )
    }
}

data class SignUpFailCodeState(
    val code: String = "",
) : UiState

sealed interface SignUpFailCodeEvent : UiEvent {
    object MoveToBack : SignUpFailCodeEvent
    object MoveToHome : SignUpFailCodeEvent
    data class ShowErrorToast(val message: String) : SignUpFailCodeEvent
}
