package com.umc.presentation.ui.mypage

import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MypageViewModel @Inject
constructor() : BaseViewModel<MypageFragmentUiState, MypageFragmentEvent>(
    MypageFragmentUiState()){
}




data class MypageFragmentUiState(
    val dummy: String = "",
) : UiState

sealed interface MypageFragmentEvent : UiEvent {
    object DummyEvent : MypageFragmentEvent
}
