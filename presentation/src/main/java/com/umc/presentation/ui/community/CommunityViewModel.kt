package com.umc.presentation.ui.community

import com.umc.domain.model.enums.LoginType
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class CommunityViewModel @Inject
constructor() : BaseViewModel<CommunityFragmentUiState, CommunityFragmentEvent>(
    CommunityFragmentUiState()
) {

    fun setRecruit(isRecruit: Boolean) {
        updateState { copy(isRecruit = isRecruit) }
    }
}


data class CommunityFragmentUiState(

    val isRecruit: Boolean = false,


    ) : UiState

sealed interface CommunityFragmentEvent : UiEvent {
    object DummyEvent : CommunityFragmentEvent

}