package com.umc.presentation.study.admin.group.create

import com.umc.component.base.UiEvent

sealed interface AdminStudyGroupCreateEvent : UiEvent {
    data object NavigateBack : AdminStudyGroupCreateEvent
    data object RegisterSuccess : AdminStudyGroupCreateEvent

    data class RegisterFailure(
        val message: String,
    ) : AdminStudyGroupCreateEvent
}