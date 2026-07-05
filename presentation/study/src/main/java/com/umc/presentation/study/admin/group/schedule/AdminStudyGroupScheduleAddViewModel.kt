package com.umc.presentation.study.admin.group.schedule

import android.util.Log
import com.umc.component.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

@HiltViewModel
class AdminStudyGroupScheduleViewModel @Inject constructor() :
    BaseViewModel<AdminStudyGroupScheduleState, AdminStudyGroupScheduleEvent>(
        AdminStudyGroupScheduleState()
    ) {

    private val displaySdf = SimpleDateFormat("yyyy.MM.dd · a h:mm", Locale.KOREAN)

    fun onAction(action: AdminStudyGroupScheduleAction) {
        when (action) {
            AdminStudyGroupScheduleAction.ClickBack -> {
                emitEvent(AdminStudyGroupScheduleEvent.NavigateBack)
            }

            AdminStudyGroupScheduleAction.ClickRegister -> {
                emitEvent(AdminStudyGroupScheduleEvent.ShowToast("일정이 등록됐어요."))
            }

            is AdminStudyGroupScheduleAction.OnStudyNameChanged -> {
                updateState { copy(studyName = action.value) }
            }

            is AdminStudyGroupScheduleAction.OnDetailChanged -> {
                updateState { copy(detail = action.value) }
            }

            AdminStudyGroupScheduleAction.ToggleAllDay -> {
                updateState { copy(isAllDay = !isAllDay) }
            }

            AdminStudyGroupScheduleAction.ToggleOffline -> {
                updateState { copy(isOffline = !isOffline) }
            }

            AdminStudyGroupScheduleAction.ToggleAttendance -> {
                updateState { copy(createAttendance = !createAttendance) }
            }

            is AdminStudyGroupScheduleAction.SelectPlace -> {
                updateState {
                    copy(placeText = action.place)
                }
            }

            is AdminStudyGroupScheduleAction.SelectChallengers -> {
                updateState {
                    copy(
                        selectedChallengers = action.challengers,
                        challengerText = action.summaryText,
                    )
                }
            }

            is AdminStudyGroupScheduleAction.SelectWeek -> {
                updateState {
                    copy(weekText = "${action.week}주차")
                }
            }

            AdminStudyGroupScheduleAction.ClickStartDateTime,
            AdminStudyGroupScheduleAction.ClickEndDateTime,
            AdminStudyGroupScheduleAction.ClickPlace,
            AdminStudyGroupScheduleAction.ClickChallenger,
            AdminStudyGroupScheduleAction.ClickCheckInStart,
            AdminStudyGroupScheduleAction.ClickOnTimeEnd,
            AdminStudyGroupScheduleAction.ClickLateEnd,
            AdminStudyGroupScheduleAction.ClickWeek -> Unit
        }
    }

    fun updateStartDateTime(utcDateTime: String) {
        updateDateTimeText(utcDateTime) { text ->
            copy(startDateTimeText = text)
        }
    }

    fun updateEndDateTime(utcDateTime: String) {
        updateDateTimeText(utcDateTime) { text ->
            copy(endDateTimeText = text)
        }
    }

    fun updateCheckInStartDateTime(utcDateTime: String) {
        updateDateTimeText(utcDateTime) { text ->
            copy(checkInStartText = text)
        }
    }

    fun updateOnTimeEndDateTime(utcDateTime: String) {
        updateDateTimeText(utcDateTime) { text ->
            copy(onTimeEndText = text)
        }
    }

    fun updateLateEndDateTime(utcDateTime: String) {
        updateDateTimeText(utcDateTime) { text ->
            copy(lateEndText = text)
        }
    }

    private fun updateDateTimeText(
        utcDateTime: String,
        reducer: AdminStudyGroupScheduleState.(String) -> AdminStudyGroupScheduleState,
    ) {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        try {
            val date = sdf.parse(utcDateTime) ?: return
            val text = displaySdf.format(date)
            updateState { reducer(text) }
        } catch (e: Exception) {
            Log.e("AdminGroupSchedule", "date parse error: ${e.message}")
        }
    }
}