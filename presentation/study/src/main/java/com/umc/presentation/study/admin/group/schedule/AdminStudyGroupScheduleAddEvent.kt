package com.umc.presentation.study.admin.group.schedule

import com.umc.component.base.UiEvent

sealed interface AdminStudyGroupScheduleEvent : UiEvent {
    data object NavigateBack : AdminStudyGroupScheduleEvent
    data class ShowToast(val message: String) : AdminStudyGroupScheduleEvent
}