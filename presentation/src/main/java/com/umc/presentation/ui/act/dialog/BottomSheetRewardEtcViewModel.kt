package com.umc.presentation.ui.act.dialog

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.umc.domain.model.enums.RewardType
import com.umc.domain.usecase.challenger.AddChallengerPointUseCase
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BottomSheetRewardEtcViewModel @Inject constructor(
    private val addChallengerPointUseCase: AddChallengerPointUseCase
) : BaseViewModel<BottomSheetRewardEtcUiState, BottomSheetRewardEtcEvent>(
    BottomSheetRewardEtcUiState()
)
{
    //챌린저 아이디 저장
    fun setChallengerId(challengerId: Long){
        updateState { copy(challengerId = challengerId) }
    }

    //상점 -1
    fun minusReward() {
        updateState {
            copy(
                reward = maxOf(reward - 1, 0)
            )
        }
    }
    
    //상점 +1
    fun plusReward(){
        updateState {
            copy(
                reward = reward + 1
            )
        }
    }
    
    //벌점 -1
    fun minusPunish() {
        updateState {
            copy(
                punish = maxOf(punish - 1, 0)
            )
        }
    }
    
    //벌점 + 1
    fun plusPunish(){
        updateState {
            copy(
                punish = punish + 1
            )
        }
    }

    //textfield 변경
    fun onTextChanged(text: String) {
        updateState { copy(description = text) }
    }

    //상벌점 등록
    fun submitReward(){
        val challengerId = uiState.value.challengerId
        val pointType = RewardType.CUSTOM.name
        val pointValue = uiState.value.reward - uiState.value.punish
        val description = uiState.value.description

        viewModelScope.launch {
            resultResponse(
                response = addChallengerPointUseCase(challengerId, pointType, pointValue, description),
                successCallback = {
                    emitEvent(BottomSheetRewardEtcEvent.SendSuccess)
                },
                errorCallback = { error ->
                    emitEvent(BottomSheetRewardEtcEvent.SendFail(error.message))
                }
            )
        }
    }
    
}


data class BottomSheetRewardEtcUiState(
    val challengerId : Long = -1L,

    val description: String = "",
    val reward: Int = 0,
    val punish: Int = 0,
): UiState {
    val submitOk: Boolean = reward != 0 || punish != 0
}

sealed interface BottomSheetRewardEtcEvent: UiEvent {
    object SendSuccess : BottomSheetRewardEtcEvent
    data class SendFail(val message: String) : BottomSheetRewardEtcEvent
}

