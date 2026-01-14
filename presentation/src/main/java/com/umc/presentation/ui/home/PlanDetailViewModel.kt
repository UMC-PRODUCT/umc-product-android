package com.umc.presentation.ui.home

import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class PlanDetailViewModel @Inject
constructor() : BaseViewModel<PlanDetailFragmentUiState, PlanDetailFragmentEvent>(
    PlanDetailFragmentUiState()){


    fun onClickBackPressed(){
        emitEvent(PlanDetailFragmentEvent.MoveBackPressedEvent)
    }

    fun onClickConfirmAttention(){
        emitEvent(PlanDetailFragmentEvent.TouchConfirmAttention)
    }


}

data class PlanDetailFragmentUiState(
    val isToday : Boolean = true,

    //일정 관련
    /**TODO 차후 SchedulePlanItem 관련해서 세팅을 해야 할 거 같음**/
    val title : String = "정기 세션 3주차",
    val date : String = "",
    val dateWithDay : String ="",
    val time : String = "",
    val place : String = "",
    val detail : String = "",

) : UiState

sealed class PlanDetailFragmentEvent : UiEvent {
    object MoveBackPressedEvent : PlanDetailFragmentEvent()
    object TouchConfirmAttention : PlanDetailFragmentEvent()

}