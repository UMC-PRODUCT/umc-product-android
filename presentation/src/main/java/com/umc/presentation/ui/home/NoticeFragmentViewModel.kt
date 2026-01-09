package com.umc.presentation.ui.home

import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import javax.inject.Inject

class NoticeFragmentViewModel @Inject
constructor() : BaseViewModel<NoticeFragmentUiState, NoticeFragmentEvent>(
    NoticeFragmentUiState()){

}



data class NoticeFragmentUiState(
    val dummyData: String = "",
) : UiState

sealed class NoticeFragmentEvent : UiEvent {
    object DummyEvent : HomeFragmentEvent()
}