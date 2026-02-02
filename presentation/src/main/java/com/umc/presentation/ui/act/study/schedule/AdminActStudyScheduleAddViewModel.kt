package com.umc.presentation.ui.act.study.schedule

import com.umc.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class AdminActStudyScheduleAddViewModel @Inject constructor() :
    BaseViewModel<AdminActStudyScheduleAddState, AdminActStudyScheduleAddEvent>(
        AdminActStudyScheduleAddState()
    ) {

    fun handleEvent(event: AdminActStudyScheduleAddEvent) {
        when (event) {
            is AdminActStudyScheduleAddEvent.UpdateStudyName ->
                updateState { copy(studyName = event.text) }

            is AdminActStudyScheduleAddEvent.UpdateLocation ->
                updateState { copy(location = event.text) }

            is AdminActStudyScheduleAddEvent.UpdateStartDate -> {
                val newCal = (uiState.value.startDate.clone() as Calendar).apply {
                    set(event.year, event.month, event.day)
                }
                updateState {
                    copy(
                        startDate = newCal,
                        startDateText = AdminActStudyScheduleAddState.dateSdf.format(newCal.time)
                    )
                }
            }

            is AdminActStudyScheduleAddEvent.UpdateStartTime -> {
                val newCal = (uiState.value.startTime.clone() as Calendar).apply {
                    set(Calendar.HOUR_OF_DAY, event.hour)
                    set(Calendar.MINUTE, event.minute)
                }
                updateState {
                    copy(
                        startTime = newCal,
                        startTimeText = AdminActStudyScheduleAddState.timeSdf.format(newCal.time)
                    )
                }
            }

            is AdminActStudyScheduleAddEvent.UpdateEndDate -> {
                val newCal = (uiState.value.endDate.clone() as Calendar).apply {
                    set(event.year, event.month, event.day)
                }
                updateState {
                    copy(
                        endDate = newCal,
                        endDateText = AdminActStudyScheduleAddState.dateSdf.format(newCal.time)
                    )
                }
            }

            is AdminActStudyScheduleAddEvent.UpdateEndTime -> {
                val newCal = (uiState.value.endTime.clone() as Calendar).apply {
                    set(Calendar.HOUR_OF_DAY, event.hour)
                    set(Calendar.MINUTE, event.minute)
                }
                updateState {
                    copy(
                        endTime = newCal,
                        endTimeText = AdminActStudyScheduleAddState.timeSdf.format(newCal.time)
                    )
                }
            }

            AdminActStudyScheduleAddEvent.ClickRegister -> {
                // TODO: API 호출 자리
            }
        }
    }
}
