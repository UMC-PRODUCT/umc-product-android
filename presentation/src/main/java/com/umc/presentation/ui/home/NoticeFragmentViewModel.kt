package com.umc.presentation.ui.home

import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import javax.inject.Inject

class NoticeFragmentViewModel @Inject
constructor() : BaseViewModel<NoticeFragmentUiState, NoticeFragmentEvent>(
    NoticeFragmentUiState()){

    init {
        updateState {
            copy()
        }
    }

    fun onClickBackPressed(){
        emitEvent(NoticeFragmentEvent.MoveBackPressedEvent)
    }

}



data class NoticeFragmentUiState(
    val dummyData: String = "",
) : UiState

sealed class NoticeFragmentEvent : UiEvent {
    object MoveBackPressedEvent : NoticeFragmentEvent()
}