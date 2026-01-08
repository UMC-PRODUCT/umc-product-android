package com.umc.presentation.ui.home

import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import javax.inject.Inject

class PlanAddFragmentViewModel @Inject
constructor() : BaseViewModel<PlanAddFragmentUiState, PlanAddFragmentEvent>(
    PlanAddFragmentUiState()){
}


data class PlanAddFragmentUiState(
    val dummyData: String = "",
) : UiState

sealed class PlanAddFragmentEvent : UiEvent {
    object DummyEvent : HomeFragmentEvent()
}