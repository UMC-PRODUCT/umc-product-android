package com.umc.presentation.ui.community

import com.umc.domain.model.enums.CommunityType
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

    fun setNowTab(whichTab: CommunityType) {
        updateState { copy(whichTab = whichTab) }
    }

}


data class CommunityFragmentUiState(

    // 게시글 필터링 용도
    val isRecruit: Boolean = false,
    val whichTab: CommunityType = CommunityType.ALL,


    ) : UiState

sealed interface CommunityFragmentEvent : UiEvent {
    object DummyEvent : CommunityFragmentEvent

}