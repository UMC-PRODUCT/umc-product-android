package com.umc.presentation.ui.community.top

import com.umc.domain.model.community.ContentItem
import com.umc.domain.model.enums.SearchMode
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel

class TopPostViewModel @Inject constructor() :
BaseViewModel<TopPostFragmentUiState, TopPostFragmentEvent>(
    TopPostFragmentUiState()
) {

}



data class TopPostFragmentUiState(

    val dummy : String = "",

) : UiState

sealed interface TopPostFragmentEvent : UiEvent {


}