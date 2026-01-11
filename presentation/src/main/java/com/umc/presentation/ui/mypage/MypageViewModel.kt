package com.umc.presentation.ui.mypage

import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import javax.inject.Inject

class MypageViewModel @Inject
constructor() : BaseViewModel<MypageFragmentUiState, MypageFragmentEvent>(
    MypageFragmentUiState()){
}




data class MypageFragmentUiState(
    val dummy: String = "",
) : UiState

sealed class MypageFragmentEvent : UiEvent {
    object DummyEvent : MypageFragmentEvent()
}
