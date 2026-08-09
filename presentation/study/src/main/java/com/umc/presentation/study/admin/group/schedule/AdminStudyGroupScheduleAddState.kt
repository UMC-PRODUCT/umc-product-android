package com.umc.presentation.study.admin.group.schedule

import com.umc.component.base.UiState
import com.umc.presentation.study.admin.group.schedule.bottomsheet.GroupScheduleChallengerUiModel
import com.umc.presentation.study.admin.submit.bottomsheet.AdminSubmitWeekUiModel


data class AdminStudyGroupScheduleState(
    val groupId: Long = 0L,
    val groupTitle: String = "",
    val groupPart: String = "",

    val studyName: String = "",

    val isAllDay: Boolean = false,

    val startDateTime: String? = null,
    val endDateTime: String? = null,

    val startDateTimeText: String? = null,
    val endDateTimeText: String? = null,

    val isOffline: Boolean = false,
    val placeText: String = "",
    val placeLatitude: Double? = null,
    val placeLongitude: Double? = null,

    val detail: String = "",

    val selectedChallengers: List<GroupScheduleChallengerUiModel> = emptyList(),
    val challengerText: String = "",

    val createAttendance: Boolean = false,

    val checkInStartDateTime: String? = null,
    val onTimeEndDateTime: String? = null,
    val lateEndDateTime: String? = null,

    val checkInStartText: String? = null,
    val onTimeEndText: String? = null,
    val lateEndText: String? = null,


    val weeks: List<AdminSubmitWeekUiModel> = emptyList(),
    val selectedWeeklyCurriculumId: Long? = null,
    val weekText: String = "",

    val isRegistering: Boolean = false,
) : UiState {

    val canRegister: Boolean
        get() {
            if (studyName.isBlank()) return false
            if (selectedChallengers.isEmpty()) return false
            if (startDateTime == null || endDateTime == null) return false
            if (selectedWeeklyCurriculumId == null) return false

            if (
                isOffline &&
                (
                        placeText.isBlank() ||
                                placeLatitude == null ||
                                placeLongitude == null
                        )
            ) {
                return false
            }

            if (
                createAttendance &&
                (
                        checkInStartDateTime == null ||
                                onTimeEndDateTime == null ||
                                lateEndDateTime == null
                        )
            ) {
                return false
            }

            return !isRegistering
        }
}