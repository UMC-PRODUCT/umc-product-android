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

    //출석 체크 로직
    fun onClickConfirmAttention(){
        emitEvent(PlanDetailFragmentEvent.TouchConfirmAttention)
    }

    //상단 케밥 메뉴 열기
    fun toggleKebabMenu(){
        updateState { copy(isMenuVisible = !isMenuVisible) }
    }

    //신고 로직 수행
    fun reportPlan(){
        updateState { copy(isMenuVisible = false) }
        emitEvent(PlanDetailFragmentEvent.ReportPlan)
    }

    //수정 로직 수행
    fun editPlan(){
        updateState { copy(isMenuVisible = false) }
        emitEvent(PlanDetailFragmentEvent.EditPlan)
    }

    //삭제 로직 수행
    fun deletePlan(){
        updateState { copy(isMenuVisible = false) }
        emitEvent(PlanDetailFragmentEvent.DeletePlan)
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

    //내가 작성한 것인지 여부
    val isAuthor: Boolean = true,

    //케밥 메뉴 아이콘 보이기 여부
    val isMenuVisible : Boolean = false,


) : UiState

sealed interface PlanDetailFragmentEvent : UiEvent {

    object MoveBackPressedEvent : PlanDetailFragmentEvent

    //토글 이벤트
    object ToggleMenu : PlanDetailFragmentEvent
    //신고하기 이벤트
    object ReportPlan : PlanDetailFragmentEvent
    //수정하기 이벤트
    object EditPlan : PlanDetailFragmentEvent
    //삭제하기 이벤트
    object DeletePlan : PlanDetailFragmentEvent


    //출석 체크 이벤트
    object TouchConfirmAttention : PlanDetailFragmentEvent


}