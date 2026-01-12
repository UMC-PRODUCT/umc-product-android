package com.umc.presentation.ui.home

import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NoticeViewModel @Inject
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