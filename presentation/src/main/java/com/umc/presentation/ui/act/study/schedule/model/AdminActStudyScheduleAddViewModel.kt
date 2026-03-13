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
                updateState { copy(groupId = event.groupId) }

            is AdminActStudyScheduleAddEvent.UpdateStudyName ->
                updateState { copy(studyName = event.text) }

            is AdminActStudyScheduleAddEvent.UpdateLocation ->
                updateState { copy(locationName = event.name, latitude = event.lat, longitude = event.lng) }

            AdminActStudyScheduleAddEvent.ClickLocationCard -> Unit

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

        val lat = s.latitude ?: return
        val lng = s.longitude ?: return

        val startsAt = toUtcIsoString(mergeDateTime(s.startDate, s.startTime))
        val endsAt = toUtcIsoString(mergeDateTime(s.endDate, s.endTime))

        val req = CreateStudyGroupSchedule(
            name = s.studyName.trim(),
            startsAt = startsAt,
            endsAt = endsAt,
            isAllDay = false,
            locationName = s.locationName.trim(),
            latitude = lat,
            longitude = lng,
            description = "",
            tags = listOf("STUDY"),
            studyGroupId = s.groupId,
            gisuId = 1L,
            requiresApproval = true
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