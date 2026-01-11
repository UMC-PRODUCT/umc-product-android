package com.umc.presentation.ui.home

import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class PlanAddViewModel @Inject
constructor() : BaseViewModel<PlanAddFragmentUiState, PlanAddFragmentEvent>(
    PlanAddFragmentUiState()){
}


data class PlanAddFragmentUiState(
    val dummyData: String = "",
) : UiState

sealed class PlanAddFragmentEvent : UiEvent {
    object DummyEvent : HomeFragmentEvent()
}