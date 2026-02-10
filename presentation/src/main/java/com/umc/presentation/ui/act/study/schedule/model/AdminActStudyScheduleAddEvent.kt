package com.umc.presentation.ui.act.study.schedule.model

import com.umc.presentation.base.UiEvent

sealed interface AdminActStudyScheduleAddEvent : UiEvent {
    data class UpdateStudyName(val text: String) : AdminActStudyScheduleAddEvent
    data class UpdateLocation(val text: String) : AdminActStudyScheduleAddEvent

    data class UpdateStartDate(val year: Int, val month: Int, val day: Int) : AdminActStudyScheduleAddEvent
    data class UpdateStartTime(val hour: Int, val minute: Int) : AdminActStudyScheduleAddEvent
    data class UpdateEndDate(val year: Int, val month: Int, val day: Int) : AdminActStudyScheduleAddEvent
    data class UpdateEndTime(val hour: Int, val minute: Int) : AdminActStudyScheduleAddEvent

    object ClickRegister : AdminActStudyScheduleAddEvent
}
