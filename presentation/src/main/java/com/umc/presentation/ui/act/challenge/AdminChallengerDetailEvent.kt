package com.umc.presentation.ui.act.challenge

import com.umc.presentation.base.UiEvent

sealed interface AdminChallengerDetailEvent : UiEvent {
    data class ShowToast(val message: String, val isError: Boolean = false) : AdminChallengerDetailEvent
    object NavigateBack : AdminChallengerDetailEvent
}