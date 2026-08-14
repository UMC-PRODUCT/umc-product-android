package com.umc.presentation.act

import com.umc.component.base.UiEvent

sealed interface ActEvent : UiEvent {
    data class ShowToast(val message: String) : ActEvent
}
