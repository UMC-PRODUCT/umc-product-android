package com.umc.presentation.ui.act.dialog

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

}


data class BottomSheetRewardEtcUiState(
    val description: String = "",
    val reward: Int = 0,
    val punish: Int = 0,
): UiState

sealed interface BottomSheetRewardEtcEvent: UiEvent {

}

