package com.umc.presentation.study.admin.submit

import com.umc.component.base.UiEvent

sealed interface AdminSubmitEvent : UiEvent {
    data class ShowToast(val message: String) : AdminSubmitEvent
}