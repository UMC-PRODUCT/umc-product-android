package com.umc.presentation.ui.act.study.schedule.model

import androidx.lifecycle.viewModelScope
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.home.schedule.CreateStudyGroupSchedule
import com.umc.domain.usecase.schedule.CreateStudyGroupScheduleUseCase
import com.umc.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

@HiltViewModel
class AdminActStudyScheduleAddViewModel @Inject constructor(
    private val createStudyGroupScheduleUseCase: CreateStudyGroupScheduleUseCase,
) : BaseViewModel<AdminActStudyScheduleAddState, AdminActStudyScheduleAddEvent>(
    AdminActStudyScheduleAddState()
) {

    fun handleEvent(event: AdminActStudyScheduleAddEvent) {
        when (event) {
            is AdminActStudyScheduleAddEvent.Init ->
                updateState { copy(groupId = event.groupId, groupTitle = event.groupTitle, groupPart = event.groupPart) }

            is AdminActStudyScheduleAddEvent.UpdateStudyName ->
                updateState { copy(studyName = event.text) }

            is AdminActStudyScheduleAddEvent.UpdateLocation ->
                updateState { copy(locationName = event.name, latitude = event.lat, longitude = event.lng) }

            AdminActStudyScheduleAddEvent.ClickLocationCard -> Unit

            AdminActStudyScheduleAddEvent.ToggleOnline ->
                updateState { copy(isOnline = !isOnline) }

            is AdminActStudyScheduleAddEvent.UpdateParticipants ->
                updateState { copy(selectedParticipants = event.participants, selectedParticipantsString = event.summaryString) }

            is AdminActStudyScheduleAddEvent.UpdateStartDate -> {
                val newCal = (uiState.value.startDate.clone() as Calendar).apply {
                    set(event.year, event.month, event.day)
                }
                updateState { copy(startDate = newCal, startDateText = AdminActStudyScheduleAddState.dateSdf.format(newCal.time)) }
            }

            is AdminActStudyScheduleAddEvent.UpdateStartTime -> {
                val newCal = (uiState.value.startTime.clone() as Calendar).apply {
                    set(Calendar.HOUR_OF_DAY, event.hour)
                    set(Calendar.MINUTE, event.minute)
                }
                updateState { copy(startTime = newCal, startTimeText = AdminActStudyScheduleAddState.timeSdf.format(newCal.time)) }
            }

            is AdminActStudyScheduleAddEvent.UpdateEndDate -> {
                val newCal = (uiState.value.endDate.clone() as Calendar).apply {
                    set(event.year, event.month, event.day)
                }
                updateState { copy(endDate = newCal, endDateText = AdminActStudyScheduleAddState.dateSdf.format(newCal.time)) }
            }

            is AdminActStudyScheduleAddEvent.UpdateEndTime -> {
                val newCal = (uiState.value.endTime.clone() as Calendar).apply {
                    set(Calendar.HOUR_OF_DAY, event.hour)
                    set(Calendar.MINUTE, event.minute)
                }
                updateState { copy(endTime = newCal, endTimeText = AdminActStudyScheduleAddState.timeSdf.format(newCal.time)) }
            }

            is AdminActStudyScheduleAddEvent.UpdateCheckInStartDate -> {
                val newCal = (uiState.value.checkInStartDate.clone() as Calendar).apply { set(event.year, event.month, event.day) }
                updateState { copy(checkInStartDate = newCal, checkInStartDateText = AdminActStudyScheduleAddState.dateSdf.format(newCal.time)) }
            }
            is AdminActStudyScheduleAddEvent.UpdateCheckInStartTime -> {
                val newCal = (uiState.value.checkInStartTime.clone() as Calendar).apply { set(Calendar.HOUR_OF_DAY, event.hour); set(Calendar.MINUTE, event.minute) }
                updateState { copy(checkInStartTime = newCal, checkInStartTimeText = AdminActStudyScheduleAddState.timeSdf.format(newCal.time)) }
            }
            is AdminActStudyScheduleAddEvent.UpdateOnTimeEndDate -> {
                val newCal = (uiState.value.onTimeEndDate.clone() as Calendar).apply { set(event.year, event.month, event.day) }
                updateState { copy(onTimeEndDate = newCal, onTimeEndDateText = AdminActStudyScheduleAddState.dateSdf.format(newCal.time)) }
            }
            is AdminActStudyScheduleAddEvent.UpdateOnTimeEndTime -> {
                val newCal = (uiState.value.onTimeEndTime.clone() as Calendar).apply { set(Calendar.HOUR_OF_DAY, event.hour); set(Calendar.MINUTE, event.minute) }
                updateState { copy(onTimeEndTime = newCal, onTimeEndTimeText = AdminActStudyScheduleAddState.timeSdf.format(newCal.time)) }
            }
            is AdminActStudyScheduleAddEvent.UpdateLateEndDate -> {
                val newCal = (uiState.value.lateEndDate.clone() as Calendar).apply { set(event.year, event.month, event.day) }
                updateState { copy(lateEndDate = newCal, lateEndDateText = AdminActStudyScheduleAddState.dateSdf.format(newCal.time)) }
            }
            is AdminActStudyScheduleAddEvent.UpdateLateEndTime -> {
                val newCal = (uiState.value.lateEndTime.clone() as Calendar).apply { set(Calendar.HOUR_OF_DAY, event.hour); set(Calendar.MINUTE, event.minute) }
                updateState { copy(lateEndTime = newCal, lateEndTimeText = AdminActStudyScheduleAddState.timeSdf.format(newCal.time)) }
            }

            is AdminActStudyScheduleAddEvent.SelectWeek ->
                updateState { copy(selectedWeek = event.week) }

            AdminActStudyScheduleAddEvent.ClickRegister -> postCreate()

            is AdminActStudyScheduleAddEvent.ShowToast -> Unit
        }
    }

    private fun postCreate() {
        val s = uiState.value
        if (!s.isRegisterOk) return
        if (s.groupId <= 0L) {
            emitEvent(AdminActStudyScheduleAddEvent.ShowToast("그룹 정보가 올바르지 않습니다."))
            return
        }

        val lat = if (s.isOnline) 0.0 else (s.latitude ?: return)
        val lng = if (s.isOnline) 0.0 else (s.longitude ?: return)

        val startsAt = toUtcIsoString(mergeDateTime(s.startDate, s.startTime))
        val endsAt = toUtcIsoString(mergeDateTime(s.endDate, s.endTime))

        val attendancePolicy = if (s.isAttendancePolicyFilled) {
            CreateStudyGroupSchedule.AttendancePolicy(
                checkInStartAt = toUtcIsoString(mergeDateTime(s.checkInStartDate, s.checkInStartTime)),
                onTimeEndAt = toUtcIsoString(mergeDateTime(s.onTimeEndDate, s.onTimeEndTime)),
                lateEndAt = toUtcIsoString(mergeDateTime(s.lateEndDate, s.lateEndTime)),
            )
        } else null

        val req = CreateStudyGroupSchedule(
            name = s.studyName.trim(),
            startsAt = startsAt,
            endsAt = endsAt,
            isAllDay = false,
            locationName = if (s.isOnline) "" else s.locationName.trim(),
            latitude = lat,
            longitude = lng,
            description = "",
            tags = listOf("STUDY"),
            studyGroupId = s.groupId,
            gisuId = 1L,
            requiresApproval = true,
            participantMemberIds = s.selectedParticipants.map { it.id },
            attendancePolicy = attendancePolicy,
        )

        viewModelScope.launch {
            when (val result = createStudyGroupScheduleUseCase(req)) {
                is ApiState.Success -> {
                    emitEvent(AdminActStudyScheduleAddEvent.ShowToast("스터디 일정이 등록되었습니다."))
                }
                is ApiState.Fail -> {
                    emitEvent(
                        AdminActStudyScheduleAddEvent.ShowToast(
                            "일정 등록 실패: ${result.failState.message}"
                        )
                    )
                }
            }
        }
    }

    private fun mergeDateTime(date: Calendar, time: Calendar): Calendar {
        return (date.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, time.get(Calendar.HOUR_OF_DAY))
            set(Calendar.MINUTE, time.get(Calendar.MINUTE))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    private fun toUtcIsoString(cal: Calendar): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(cal.time)
    }
}