package com.umc.presentation.ui.act.dialog

import android.util.Log
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BottomSheetRewardEtcViewModel @Inject constructor(

) : BaseViewModel<BottomSheetRewardEtcUiState, BottomSheetRewardEtcEvent>(
    BottomSheetRewardEtcUiState()
)
{

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
        Log.d("log_reward", "상점: ${uiState.value.reward}, 벌점: ${uiState.value.punish}")
        Log.d("Log_reward", "설명: ${uiState.value.description}")
    }
    
}


data class BottomSheetRewardEtcUiState(
    val description: String = "",
    val reward: Int = 0,
    val punish: Int = 0,
): UiState {
    val submitOk: Boolean = reward != 0 || punish != 0
}

sealed interface BottomSheetRewardEtcEvent: UiEvent {

}

