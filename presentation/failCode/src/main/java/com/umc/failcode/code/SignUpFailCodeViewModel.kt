package com.umc.failcode.code

import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import com.umc.component.base.BaseViewModel
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.umc.component.util.ULog
import com.umc.domain.model.request.challenger.ChallengerRecordMemberRequest
import com.umc.domain.usecase.challenger.AddChallengerRecordMemberUseCase
import com.umc.domain.usecase.notification.RegisterFcmTokenUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class SignUpFailCodeViewModel @Inject constructor(
    private val addChallengerRecordMemberUseCase: AddChallengerRecordMemberUseCase,
    private val registerFcmTokenUseCase: RegisterFcmTokenUseCase,
) : BaseViewModel<SignUpFailCodeState, SignUpFailCodeEvent>(
    SignUpFailCodeState(),
) {

    fun onClickBack() {
        emitEvent(SignUpFailCodeEvent.MoveToBack)
    }

    fun onCodeChanged(code: String) {
        updateState { copy(code = code) }
    }

    /**
     * 인증코드로 챌린저 기록 등록. 성공 시 홈으로, 실패 시 에러 메시지 다이얼로그 표시.
     * 요청 진행 중에는 다시 호출되지 않아 연타해도 요청이 한 번만 나간다
     */
    fun register() = viewModelScope.launch {
        if (uiState.value.isSubmitting) return@launch
        updateState { copy(isSubmitting = true) }
        val request = ChallengerRecordMemberRequest(code = uiState.value.code)
        resultResponse(
            response = addChallengerRecordMemberUseCase(request),
            // 성공 시에는 홈으로 이동하므로 제출 중 상태를 유지한다
            successCallback = { registerFcmToken() },
            errorCallback = { failState ->
                updateState { copy(isSubmitting = false) }
                emitEvent(SignUpFailCodeEvent.ShowErrorDialog(failState.message))
            }
        )
    }

    /**
     * 챌린저 기록 없이 로그인한 계정은 로그인 화면에서 FCM 토큰을 등록하지 못하고 여기로 온다.
     * 코드 등록을 마친 뒤 홈에 들어가기 전에 등록한다. 등록 실패가 홈 진입을 막지는 않는다
     */
    private fun registerFcmToken() = viewModelScope.launch {
        try {
            val fcmToken = FirebaseMessaging.getInstance().token.await()
            resultResponse(
                response = registerFcmTokenUseCase(fcmToken),
                successCallback = { emitEvent(SignUpFailCodeEvent.MoveToHome) },
                errorCallback = { emitEvent(SignUpFailCodeEvent.MoveToHome) },
            )
        } catch (e: Exception) {
            ULog.d("FCM 토큰 획득 또는 등록 실패: ${e.message}")
            emitEvent(SignUpFailCodeEvent.MoveToHome)
        }
    }
}

data class SignUpFailCodeState(
    val code: String = "",
    val isSubmitting: Boolean = false,
) : UiState

sealed interface SignUpFailCodeEvent : UiEvent {
    data object MoveToBack : SignUpFailCodeEvent
    data object MoveToHome : SignUpFailCodeEvent
    data class ShowErrorDialog(val message: String) : SignUpFailCodeEvent
}
