package com.umc.presentation.ui.home

import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import javax.inject.Inject

class NotificationFragmentViewModel @Inject
constructor() : BaseViewModel<NotificationFragmentUiState, NotificationFragmentEvent>(
    NotificationFragmentUiState()){
}


data class NotificationFragmentUiState(
    val dummyData: String = "",
) : UiState

sealed class NotificationFragmentEvent : UiEvent {
    object DummyEvent : HomeFragmentEvent()
}