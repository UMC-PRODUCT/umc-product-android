package com.umc.presentation.study.admin.group.schedule

import com.umc.presentation.study.admin.group.schedule.bottomsheet.GroupScheduleChallengerUiModel

sealed interface AdminStudyGroupScheduleAction {
    data object ClickBack : AdminStudyGroupScheduleAction
    data object ClickRegister : AdminStudyGroupScheduleAction

    data class OnStudyNameChanged(val value: String) : AdminStudyGroupScheduleAction
    data class OnDetailChanged(val value: String) : AdminStudyGroupScheduleAction

    data object ToggleAllDay : AdminStudyGroupScheduleAction
    data object ToggleOffline : AdminStudyGroupScheduleAction
    data object ToggleAttendance : AdminStudyGroupScheduleAction

    data object ClickStartDateTime : AdminStudyGroupScheduleAction
    data object ClickEndDateTime : AdminStudyGroupScheduleAction
    data object ClickPlace : AdminStudyGroupScheduleAction
    data object ClickChallenger : AdminStudyGroupScheduleAction
    data object ClickCheckInStart : AdminStudyGroupScheduleAction
    data object ClickOnTimeEnd : AdminStudyGroupScheduleAction
    data object ClickLateEnd : AdminStudyGroupScheduleAction
    data object ClickWeek : AdminStudyGroupScheduleAction

    data class SelectWeek(
        val week: Int,
        val weeklyCurriculumId: Long,
    ) : AdminStudyGroupScheduleAction

    data class SelectPlace(
        val place: String,
        val latitude: Double,
        val longitude: Double,
    ) : AdminStudyGroupScheduleAction

    data class SelectChallengers(
        val challengers: List<GroupScheduleChallengerUiModel>,
        val summaryText: String,
    ) : AdminStudyGroupScheduleAction
}