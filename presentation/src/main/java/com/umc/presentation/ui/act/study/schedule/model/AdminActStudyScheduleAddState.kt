package com.umc.presentation.ui.act.study.schedule.model

import com.umc.domain.model.home.ParticipantItem
import com.umc.presentation.base.UiState
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class AdminActStudyScheduleAddState(
    val groupId: Long = 0L,
    val groupTitle: String = "",
    val groupPart: String = "",

    val studyName: String = "",

    val isOnline: Boolean = false,
    val locationName: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,

    val startDate: Calendar = Calendar.getInstance(),
    val startTime: Calendar = Calendar.getInstance(),
    val endDate: Calendar = Calendar.getInstance(),
    val endTime: Calendar = Calendar.getInstance(),

    val startDateText: String = "시작 날짜",
    val startTimeText: String = "시작 시간",
    val endDateText: String = "종료 날짜",
    val endTimeText: String = "종료 시간",

    val checkInStartDate: Calendar = Calendar.getInstance(),
    val checkInStartTime: Calendar = Calendar.getInstance(),
    val onTimeEndDate: Calendar = Calendar.getInstance(),
    val onTimeEndTime: Calendar = Calendar.getInstance(),
    val lateEndDate: Calendar = Calendar.getInstance(),
    val lateEndTime: Calendar = Calendar.getInstance(),

    val checkInStartDateText: String = "시작 날짜",
    val checkInStartTimeText: String = "시작 시간",
    val onTimeEndDateText: String = "종료 날짜",
    val onTimeEndTimeText: String = "종료 시간",
    val lateEndDateText: String = "종료 날짜",
    val lateEndTimeText: String = "종료 시간",

    val selectedParticipants: List<ParticipantItem> = emptyList(),
    val selectedParticipantsString: String = "",

    val selectedWeek: Int? = null,
) : UiState {

    val isSelectedParticipant: Boolean get() = selectedParticipants.isNotEmpty()

    val weekDisplayText: String get() = selectedWeek?.let { "${it}주차" } ?: "주차 선택"

    val isAttendancePolicyFilled: Boolean
        get() = checkInStartDateText != "시작 날짜" && checkInStartTimeText != "시작 시간" &&
                onTimeEndDateText != "종료 날짜" && onTimeEndTimeText != "종료 시간" &&
                lateEndDateText != "종료 날짜" && lateEndTimeText != "종료 시간"

    val isRegisterOk: Boolean
        get() {
            val isNameOk = studyName.isNotBlank()
            val isLocationOk = isOnline || (locationName.isNotBlank() && latitude != null && longitude != null)
            val isDateTimeOk = startDateText != "시작 날짜" && startTimeText != "시작 시간" &&
                    endDateText != "종료 날짜" && endTimeText != "종료 시간"
            return isNameOk && isLocationOk && isDateTimeOk
        }

    companion object {
        val dateSdf = SimpleDateFormat("yyyy.MM.dd", Locale.KOREAN)
        val timeSdf = SimpleDateFormat("a h:mm", Locale.KOREAN)
    }
}