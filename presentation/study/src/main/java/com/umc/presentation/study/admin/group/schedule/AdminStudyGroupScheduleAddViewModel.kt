package com.umc.presentation.study.admin.group.schedule

import android.util.Log
import com.umc.component.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import com.umc.domain.model.base.ApiState
import com.umc.domain.usecase.curriculum.GetCurriculumOverviewUseCase
import com.umc.presentation.study.admin.submit.bottomsheet.AdminSubmitWeekUiModel
import com.umc.domain.usecase.organization.GetActiveGisuUseCase
import com.umc.domain.model.home.schedule.CreateSchedule
import com.umc.domain.model.request.organization.CreateStudyGroupScheduleRequest
import com.umc.domain.usecase.schedule.CreateScheduleUseCase
import com.umc.domain.usecase.organization.CreateStudyGroupScheduleUseCase
import kotlinx.coroutines.launch


@HiltViewModel
class AdminStudyGroupScheduleViewModel @Inject constructor(
    private val getActiveGisuUseCase: GetActiveGisuUseCase,
    private val getCurriculumOverviewUseCase: GetCurriculumOverviewUseCase,
    private val createScheduleUseCase: CreateScheduleUseCase,
    private val createStudyGroupScheduleUseCase: CreateStudyGroupScheduleUseCase,
) : BaseViewModel<AdminStudyGroupScheduleState, AdminStudyGroupScheduleEvent>(
    AdminStudyGroupScheduleState()
) {

    private val displaySdf = SimpleDateFormat("yyyy.MM.dd · a h:mm", Locale.KOREAN)

    fun onAction(action: AdminStudyGroupScheduleAction) {
        when (action) {
            AdminStudyGroupScheduleAction.ClickBack -> {
                emitEvent(AdminStudyGroupScheduleEvent.NavigateBack)
            }

            AdminStudyGroupScheduleAction.ClickRegister -> {
                registerSchedule()
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
                    copy(
                        placeText = action.place,
                        placeLatitude = action.latitude,
                        placeLongitude = action.longitude,
                    )
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
                    copy(
                        weekText = "${action.week}주차",
                        selectedWeeklyCurriculumId = action.weeklyCurriculumId,
                    )
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
        val text = formatDateTime(utcDateTime) ?: return

        updateState {
            copy(
                startDateTime = utcDateTime,
                startDateTimeText = text,

                // 종료일이 아직 없을 때만 시작일과 동일하게 설정
                endDateTime = endDateTime ?: utcDateTime,
                endDateTimeText = endDateTimeText ?: text,
            )
        }
    }

    fun updateEndDateTime(utcDateTime: String) {
        val text = formatDateTime(utcDateTime) ?: return

        updateState {
            copy(
                endDateTime = utcDateTime,
                endDateTimeText = text,
            )
        }
    }

    fun updateCheckInStartDateTime(utcDateTime: String) {
        updateDateTimeText(utcDateTime) { text ->
            copy(
                checkInStartDateTime = utcDateTime,
                checkInStartText = text,
            )
        }
    }

    fun updateOnTimeEndDateTime(utcDateTime: String) {
        updateDateTimeText(utcDateTime) { text ->
            copy(
                onTimeEndDateTime = utcDateTime,
                onTimeEndText = text,
            )
        }
    }

    fun updateLateEndDateTime(utcDateTime: String) {
        updateDateTimeText(utcDateTime) { text ->
            copy(
                lateEndDateTime = utcDateTime,
                lateEndText = text,
            )
        }
    }

    fun initializeGroup(
        groupId: Long,
        groupTitle: String,
        groupPart: String,
    ) {
        updateState {
            copy(
                groupId = groupId,
                groupTitle = groupTitle,
                groupPart = groupPart,
            )
        }

        loadCurriculumWeeks(
            part = groupPart,
        )
    }

    private fun loadCurriculumWeeks(
        part: String,
    ) {
        viewModelScope.launch {
            when (val gisuResult = getActiveGisuUseCase()) {
                is ApiState.Success -> {
                    val activeGisuId = gisuResult.data.gisuId.toLong()

                    when (
                        val curriculumResult = getCurriculumOverviewUseCase(
                            gisuId = activeGisuId,
                            part = part,
                        )
                    ) {
                        is ApiState.Success -> {
                            val weeks = curriculumResult.data.weeks
                                .sortedBy { it.weekNo }
                                .map { weeklyCurriculum ->
                                    AdminSubmitWeekUiModel(
                                        week = weeklyCurriculum.weekNo,
                                        weeklyCurriculumId =
                                            weeklyCurriculum.weeklyCurriculumId,
                                    )
                                }

                            updateState {
                                copy(
                                    weeks = weeks,
                                )
                            }
                        }

                        is ApiState.Fail -> {
                            emitEvent(
                                AdminStudyGroupScheduleEvent.ShowToast(
                                    "주차 정보를 불러오지 못했어요."
                                )
                            )
                        }
                    }
                }

                is ApiState.Fail -> {
                    emitEvent(
                        AdminStudyGroupScheduleEvent.ShowToast(
                            "현재 기수 정보를 불러오지 못했어요."
                        )
                    )
                }
            }
        }
    }

    private fun registerSchedule() {
        val currentState = uiState.value

        if (!currentState.canRegister || currentState.isRegistering) {
            return
        }

        val startsAt = currentState.startDateTime ?: return
        val endsAt = currentState.endDateTime ?: return
        val weeklyCurriculumId =
            currentState.selectedWeeklyCurriculumId ?: return

        val location = if (currentState.isOffline) {
            val latitude = currentState.placeLatitude ?: return
            val longitude = currentState.placeLongitude ?: return

            CreateSchedule.Location(
                latitude = latitude,
                longitude = longitude,
                locationName = currentState.placeText,
            )
        } else {
            null
        }

        val attendancePolicy = if (currentState.createAttendance) {
            val checkInStartAt =
                currentState.checkInStartDateTime ?: return
            val onTimeEndAt =
                currentState.onTimeEndDateTime ?: return
            val lateEndAt =
                currentState.lateEndDateTime ?: return

            CreateSchedule.AttendancePolicy(
                checkInStartAt = checkInStartAt,
                onTimeEndAt = onTimeEndAt,
                lateEndAt = lateEndAt,
            )
        } else {
            null
        }

        val request = CreateSchedule(
            name = currentState.studyName.trim(),
            description = currentState.detail.trim(),
            tags = listOf("STUDY"),
            startsAt = startsAt,
            endsAt = endsAt,
            location = location,
            attendancePolicy = attendancePolicy,
            participantMemberIds = currentState.selectedChallengers.map {
                it.id
            }
        )

        viewModelScope.launch {
            updateState {
                copy(isRegistering = true)
            }

            when (val scheduleResult = createScheduleUseCase(request)) {
                is ApiState.Success -> {
                    connectScheduleToStudyGroup(
                        scheduleId = scheduleResult.data,
                        weeklyCurriculumId = weeklyCurriculumId,
                    )
                }

                is ApiState.Fail -> {
                    updateState {
                        copy(isRegistering = false)
                    }

                    emitEvent(
                        AdminStudyGroupScheduleEvent.ShowToast(
                            "일정 등록에 실패했어요."
                        )
                    )
                }
            }
        }
    }

    private suspend fun connectScheduleToStudyGroup(
        scheduleId: Long,
        weeklyCurriculumId: Long,
    ) {
        val currentState = uiState.value

        val request = CreateStudyGroupScheduleRequest(
            studyGroupId = currentState.groupId,
            scheduleId = scheduleId,
            weeklyCurriculumId = weeklyCurriculumId,
        )

        when (createStudyGroupScheduleUseCase(request)) {
            is ApiState.Success -> {
                updateState {
                    copy(isRegistering = false)
                }

                emitEvent(
                    AdminStudyGroupScheduleEvent.ShowToast(
                        "일정이 등록됐어요."
                    )
                )

                emitEvent(
                    AdminStudyGroupScheduleEvent.NavigateBack
                )
            }

            is ApiState.Fail -> {
                updateState {
                    copy(isRegistering = false)
                }

                emitEvent(
                    AdminStudyGroupScheduleEvent.ShowToast(
                        "스터디 그룹 일정 연결에 실패했어요."
                    )
                )
            }
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

    private fun formatDateTime(utcDateTime: String): String? {
        val sdf = SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            Locale.getDefault()
        ).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        return try {
            val date = sdf.parse(utcDateTime) ?: return null
            displaySdf.format(date)
        } catch (e: Exception) {
            Log.e(
                "AdminGroupSchedule",
                "date parse error: ${e.message}"
            )
            null
        }
    }

}