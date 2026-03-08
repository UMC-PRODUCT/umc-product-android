package com.umc.presentation.ui.mypage.dialog

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.umc.domain.model.home.ParticipantItem
import com.umc.domain.model.request.challenger.ChallengerRecordMemberRequest
import com.umc.domain.usecase.challenger.AddChallengerRecordMemberUseCase
import com.umc.domain.usecase.member.GetMyProfileUseCase
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class BottomSheetAddCodeViewModel @Inject constructor(
    private val addChallengerRecordMemberUseCase: AddChallengerRecordMemberUseCase,
    private val getMyProfileUseCase: GetMyProfileUseCase, //내 프로필 정보 가져오기
) : BaseViewModel<BottomSheetAddCodeUiState, BottomSheetAddCodeEvent>(
    BottomSheetAddCodeUiState()
) {

    fun onCodeChanged(code: String) {
        updateState { copy(code = code) }
    }

    fun register() {
        viewModelScope.launch {
            val request = ChallengerRecordMemberRequest(
                code = uiState.value.code,
            )
            Log.d("log_mypage", "register: $request")
            resultResponse(
                response = addChallengerRecordMemberUseCase(request),
                successCallback = {
                    //유저 정보 업데이트를 위한 호출
                    viewModelScope.launch {
                        resultResponse(
                            response = getMyProfileUseCase(),
                            successCallback = {
                                emitEvent(BottomSheetAddCodeEvent.Confirm)
                            },
                            errorCallback = {
                                emitEvent(BottomSheetAddCodeEvent.Confirm)
                            }
                        )
                    }
                },
                errorCallback = { failState ->
                    emitEvent(BottomSheetAddCodeEvent.ShowToast(failState.message))
                }
            )
        }
    }

}


data class BottomSheetAddCodeUiState(
    val code: String = "",

    ) : UiState

sealed interface BottomSheetAddCodeEvent : UiEvent {
    object Confirm : BottomSheetAddCodeEvent
    data class ShowToast(val message: String) : BottomSheetAddCodeEvent
}