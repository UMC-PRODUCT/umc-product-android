package com.umc.presentation.ui.mypage.mycomment

import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class MycommentViewModel @Inject
constructor() : BaseViewModel<MycommentFragmentUiState, MycommentFragmentEvent>(
    MycommentFragmentUiState()){
}


data class MycommentFragmentUiState(
    val dummy: String = "",
) : UiState

sealed interface MycommentFragmentEvent : UiEvent {
    object DummyEvent : MycommentFragmentEvent
}
