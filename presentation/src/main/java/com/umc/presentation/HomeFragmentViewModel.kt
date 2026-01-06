package com.umc.presentation

import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import javax.inject.Inject

class HomeFragmentViewModel @Inject constructor() : BaseViewModel<HomeFragmentUiState, HomeFragmentEvent>(
    HomeFragmentUiState()
){
}


data class HomeFragmentUiState(
    val dummy: String = ""
) : UiState

sealed class HomeFragmentEvent : UiEvent{
    object DummyEvent: HomeFragmentEvent()
}