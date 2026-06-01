package com.umc.presentation.ui.act.study.schedule.model

import com.umc.domain.model.home.ParticipantItem
import com.umc.presentation.base.UiEvent

sealed interface AdminActStudyScheduleAddEvent : UiEvent {

    data class Init(
        val groupId: Long,
        val groupTitle: String,
        val groupPart: String,
    ) : AdminActStudyScheduleAddEvent

    data class UpdateStudyName(val text: String) : AdminActStudyScheduleAddEvent

    data class UpdateLocation(
        val name: String,
        val lat: Double,
        val lng: Double
    ) : AdminActStudyScheduleAddEvent

    object ClickLocationCard : AdminActStudyScheduleAddEvent

    data class UpdateStartDate(val year: Int, val month: Int, val day: Int) : AdminActStudyScheduleAddEvent
    data class UpdateStartTime(val hour: Int, val minute: Int) : AdminActStudyScheduleAddEvent
    data class UpdateEndDate(val year: Int, val month: Int, val day: Int) : AdminActStudyScheduleAddEvent
    data class UpdateEndTime(val hour: Int, val minute: Int) : AdminActStudyScheduleAddEvent

    object ToggleOnline : AdminActStudyScheduleAddEvent
    data class UpdateParticipants(val participants: List<ParticipantItem>, val summaryString: String) : AdminActStudyScheduleAddEvent

    data class UpdateCheckInStartDate(val year: Int, val month: Int, val day: Int) : AdminActStudyScheduleAddEvent
    data class UpdateCheckInStartTime(val hour: Int, val minute: Int) : AdminActStudyScheduleAddEvent
    data class UpdateOnTimeEndDate(val year: Int, val month: Int, val day: Int) : AdminActStudyScheduleAddEvent
    data class UpdateOnTimeEndTime(val hour: Int, val minute: Int) : AdminActStudyScheduleAddEvent
    data class UpdateLateEndDate(val year: Int, val month: Int, val day: Int) : AdminActStudyScheduleAddEvent
    data class UpdateLateEndTime(val hour: Int, val minute: Int) : AdminActStudyScheduleAddEvent

    data class SelectWeek(val week: Int) : AdminActStudyScheduleAddEvent

    object ClickRegister : AdminActStudyScheduleAddEvent

    data class ShowToast(val message: String) : AdminActStudyScheduleAddEvent
}