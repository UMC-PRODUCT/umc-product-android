package com.umc.presentation.ui.act.study.schedule.model

import androidx.lifecycle.viewModelScope
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.home.schedule.CreateSchedule
import com.umc.domain.model.home.schedule.CreateStudyGroupSchedule
import com.umc.domain.usecase.curriculum.GetCurriculumOverviewUseCase
import com.umc.domain.usecase.organization.GetActiveGisuUseCase
import com.umc.domain.usecase.schedule.CreateScheduleUseCase
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
    private val createScheduleUseCase: CreateScheduleUseCase,
    private val createStudyGroupScheduleUseCase: CreateStudyGroupScheduleUseCase,
    private val getCurriculumOverviewUseCase: GetCurriculumOverviewUseCase,
    private val getActiveGisuUseCase: GetActiveGisuUseCase,
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

        val week = s.selectedWeek
        if (week == null) {
            emitEvent(AdminActStudyScheduleAddEvent.ShowToast("주차를 선택해주세요."))
            return
        }

        val location = if (s.isOnline) null else {
            val lat = s.latitude ?: run {
                emitEvent(AdminActStudyScheduleAddEvent.ShowToast("장소를 선택해주세요."))
                return
            }
            val lng = s.longitude ?: return
            CreateSchedule.Location(lat, lng, s.locationName.trim())
        }

        val attendancePolicy = if (s.isAttendancePolicyFilled) {
            CreateSchedule.AttendancePolicy(
                checkInStartAt = toUtcIsoString(mergeDateTime(s.checkInStartDate, s.checkInStartTime)),
                onTimeEndAt = toUtcIsoString(mergeDateTime(s.onTimeEndDate, s.onTimeEndTime)),
                lateEndAt = toUtcIsoString(mergeDateTime(s.lateEndDate, s.lateEndTime)),
            )
        } else null

        val scheduleReq = CreateSchedule(
            name = s.studyName.trim(),
            description = "",
            tags = listOf("STUDY"),
            startsAt = toUtcIsoString(mergeDateTime(s.startDate, s.startTime)),
            endsAt = toUtcIsoString(mergeDateTime(s.endDate, s.endTime)),
            location = location,
            attendancePolicy = attendancePolicy,
            participantMemberIds = s.selectedParticipants.map { it.id },
        )

        viewModelScope.launch {
            // 1단계: 현재 활성 기수 조회
            val gisuResult = getActiveGisuUseCase()
            if (gisuResult is ApiState.Fail) {
                emitEvent(AdminActStudyScheduleAddEvent.ShowToast("기수 정보를 불러오지 못했습니다."))
                return@launch
            }
            val gisuId = (gisuResult as ApiState.Success).data

            // 2단계: 해당 주차의 weeklyCurriculumId 조회 (커리큘럼 overview)
            val curriculumResult = getCurriculumOverviewUseCase(
                gisuId = gisuId,
                part = s.groupPart,
                weekNo = week.toLong(),
            )
            if (curriculumResult is ApiState.Fail) {
                emitEvent(AdminActStudyScheduleAddEvent.ShowToast("커리큘럼 정보를 불러오지 못했습니다."))
                return@launch
            }
            val studyProgress = (curriculumResult as ApiState.Success).data
            val weeklyCurriculumId = studyProgress.weeks.firstOrNull { it.weekNo == week }?.weeklyCurriculumId
            if (weeklyCurriculumId == null) {
                emitEvent(AdminActStudyScheduleAddEvent.ShowToast("${week}주차 커리큘럼을 찾을 수 없습니다."))
                return@launch
            }

            // 3단계: 일정 생성 (v2)
            val scheduleResult = createScheduleUseCase(scheduleReq)
            if (scheduleResult is ApiState.Fail) {
                emitEvent(AdminActStudyScheduleAddEvent.ShowToast("일정 등록 실패: ${scheduleResult.failState.message}"))
                return@launch
            }
            val scheduleId = (scheduleResult as ApiState.Success).data

            // 4단계: 스터디 그룹 일정 연결
            when (val linkResult = createStudyGroupScheduleUseCase(
                CreateStudyGroupSchedule(
                    studyGroupId = s.groupId,
                    scheduleId = scheduleId,
                    weeklyCurriculumId = weeklyCurriculumId,
                )
            )) {
                is ApiState.Success -> emitEvent(AdminActStudyScheduleAddEvent.ShowToast("스터디 일정이 등록되었습니다."))
                is ApiState.Fail -> emitEvent(AdminActStudyScheduleAddEvent.ShowToast("일정 등록 실패: ${linkResult.failState.message}"))
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
