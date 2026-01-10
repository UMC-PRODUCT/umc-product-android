package com.umc.presentation.ui.home

import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import javax.inject.Inject

class PlanDetailFragmentViewModel @Inject
constructor() : BaseViewModel<PlanDetailFragmentUiState, PlanDetailFragmentEvent>(
    PlanDetailFragmentUiState()){


    fun onClickBackPressed(){
        emitEvent(PlanDetailFragmentEvent.MoveBackPressedEvent)
    }

}

data class PlanDetailFragmentUiState(
    val dummyData: String = "",
) : UiState

sealed class PlanDetailFragmentEvent : UiEvent {
    object MoveBackPressedEvent : PlanDetailFragmentEvent()
}