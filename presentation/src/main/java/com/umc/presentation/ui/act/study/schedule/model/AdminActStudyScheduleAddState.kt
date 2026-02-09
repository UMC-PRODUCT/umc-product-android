package com.umc.presentation.ui.act.study.schedule.model

import com.umc.presentation.base.UiState
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class AdminActStudyScheduleAddState(
    val studyName: String = "",
    val location: String = "",

    val startDate: Calendar = Calendar.getInstance(),
    val startTime: Calendar = Calendar.getInstance(),
    val endDate: Calendar = Calendar.getInstance(),
    val endTime: Calendar = Calendar.getInstance(),

    val startDateText: String = "시작 날짜",
    val startTimeText: String = "시작 시간",
    val endDateText: String = "종료 날짜",
    val endTimeText: String = "종료 시간",
) : UiState {

    val isRegisterOk: Boolean
        get() {
            val isNameOk = studyName.isNotBlank()
            val isLocationOk = location.isNotBlank()
            val isDateTimeOk =
                startDateText != "시작 날짜" &&
                        startTimeText != "시작 시간" &&
                        endDateText != "종료 날짜" &&
                        endTimeText != "종료 시간"
            return isNameOk && isLocationOk && isDateTimeOk
        }

    companion object {
        val dateSdf = SimpleDateFormat("yyyy.MM.dd", Locale.KOREAN)
        val timeSdf = SimpleDateFormat("a h:mm", Locale.KOREAN)
    }
}
