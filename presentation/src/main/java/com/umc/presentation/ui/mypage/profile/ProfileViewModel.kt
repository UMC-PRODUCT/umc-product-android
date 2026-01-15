package com.umc.presentation.ui.mypage.profile

import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class ProfileViewModel @Inject
constructor() : BaseViewModel<ProfileFragmentUiState, ProfileFragmentEvent>(
    ProfileFragmentUiState()){
}


data class ProfileFragmentUiState(
    val dummy: String = "",
) : UiState

sealed interface ProfileFragmentEvent : UiEvent {
    object DummyEvent : ProfileFragmentEvent
}