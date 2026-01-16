package com.umc.presentation.ui.mypage.mypost

import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class MypostViewModel @Inject
constructor() : BaseViewModel<MypostFragmentUiState, MypostFragmentEvent>(
    MypostFragmentUiState()){
}


data class MypostFragmentUiState(
    val dummy: String = "",
) : UiState

sealed interface MypostFragmentEvent : UiEvent {
    object DummyEvent : MypostFragmentEvent
}
