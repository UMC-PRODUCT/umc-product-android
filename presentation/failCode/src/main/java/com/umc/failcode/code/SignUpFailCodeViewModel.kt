package com.umc.failcode.code

import androidx.lifecycle.viewModelScope
import com.umc.component.base.BaseViewModel
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.umc.domain.model.request.challenger.ChallengerRecordMemberRequest
import com.umc.domain.usecase.challenger.AddChallengerRecordMemberUseCase
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

    /** 인증코드로 챌린저 기록 등록. 성공 시 홈으로, 실패 시 에러 메시지 다이얼로그 표시 */
    fun register() = viewModelScope.launch {
        val request = ChallengerRecordMemberRequest(code = uiState.value.code)
        resultResponse(
            response = addChallengerRecordMemberUseCase(request),
            successCallback = { emitEvent(SignUpFailCodeEvent.MoveToHome) },
            errorCallback = { failState ->
                emitEvent(SignUpFailCodeEvent.ShowErrorDialog(failState.message))
            }
        )
    }
}

data class SignUpFailCodeState(
    val code: String = "",
) : UiState

sealed interface SignUpFailCodeEvent : UiEvent {
    data object MoveToBack : SignUpFailCodeEvent
    data object MoveToHome : SignUpFailCodeEvent
    data class ShowErrorDialog(val message: String) : SignUpFailCodeEvent
}
