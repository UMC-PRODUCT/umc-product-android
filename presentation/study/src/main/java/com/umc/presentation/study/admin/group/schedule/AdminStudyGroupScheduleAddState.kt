package com.umc.presentation.study.admin.group.schedule

import com.umc.component.base.UiState
import com.umc.presentation.study.admin.group.schedule.bottomsheet.GroupScheduleChallengerUiModel

data class AdminStudyGroupScheduleState(
    val groupTitle: String = "React A팀",
    val groupPart: String = "Web",

    val studyName: String = "",

    val isAllDay: Boolean = false,
    val startDateTimeText: String? = null,
    val endDateTimeText: String? = null,

    val isOffline: Boolean = false,
    val placeText: String = "",

    val detail: String = "",

    val selectedChallengers: List<GroupScheduleChallengerUiModel> = emptyList(),
    val challengerText: String = "",

    val createAttendance: Boolean = false,
    val checkInStartText: String? = null,
    val onTimeEndText: String? = null,
    val lateEndText: String? = null,

    val weekText: String = "",
) : UiState {
    val canRegister: Boolean
        get() = studyName.isNotBlank()
}