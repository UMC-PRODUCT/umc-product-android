package com.umc.presentation.study.admin.group.schedule

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.umc.component.base.BaseViewModel
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.home.schedule.CreateSchedule
import com.umc.domain.model.request.organization.CreateStudyGroupScheduleRequest
import com.umc.domain.usecase.curriculum.GetCurriculumOverviewUseCase
import com.umc.domain.usecase.organization.CreateStudyGroupScheduleUseCase
import com.umc.domain.usecase.organization.GetActiveGisuUseCase
import com.umc.domain.usecase.schedule.CreateScheduleUseCase
import com.umc.presentation.study.admin.submit.bottomsheet.AdminSubmitWeekUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import kotlinx.coroutines.launch

/**
 * 관리자 스터디 그룹 일정 등록 화면의 ViewModel
 *
 * 일정 등록 화면의 입력 상태를 관리하고
 * 일정 생성 API 및 스터디 그룹 일정 연결 API를 호출합니다.
 */
@HiltViewModel
class AdminStudyGroupScheduleViewModel @Inject constructor(
    private val getActiveGisuUseCase: GetActiveGisuUseCase,
    private val getCurriculumOverviewUseCase: GetCurriculumOverviewUseCase,
    private val createScheduleUseCase: CreateScheduleUseCase,
    private val createStudyGroupScheduleUseCase:
    CreateStudyGroupScheduleUseCase,
) : BaseViewModel<
        AdminStudyGroupScheduleState,
        AdminStudyGroupScheduleEvent,
        >(
    AdminStudyGroupScheduleState()
) {

    /**
     * 일반 일정의 날짜 + 시간 화면 표시 형식
     *
     * 예)
     * 2026.08.20 · 오후 3:30
     */
    private val displayDateTimeSdf =
        SimpleDateFormat(
            "yyyy.MM.dd · a h:mm",
            Locale.KOREAN
        )

    /**
     * 하루 종일 일정의 날짜 화면 표시 형식
     *
     * 예)
     * 2026.08.20
     */
    private val displayDateSdf =
        SimpleDateFormat(
            "yyyy.MM.dd",
            Locale.KOREAN
        )

    /**
     * 일정 등록 화면의 사용자 액션 처리
     */
    fun onAction(
        action: AdminStudyGroupScheduleAction,
    ) {
        when (action) {
            AdminStudyGroupScheduleAction.ClickBack -> {
                emitEvent(
                    AdminStudyGroupScheduleEvent.NavigateBack
                )
            }

            AdminStudyGroupScheduleAction.ClickRegister -> {
                registerSchedule()
            }

            is AdminStudyGroupScheduleAction.OnStudyNameChanged -> {
                updateState {
                    copy(
                        studyName = action.value
                    )
                }
            }

            is AdminStudyGroupScheduleAction.OnDetailChanged -> {
                updateState {
                    copy(
                        detail = action.value
                    )
                }
            }

            /**
             * 하루 종일 일정 여부 변경
             *
             * 기존에 선택된 시작/종료 시간이 존재한다면
             * 표시 문자열도 현재 하루 종일 상태에 맞게 다시 변환합니다.
             */
            AdminStudyGroupScheduleAction.ToggleAllDay -> {
                val state = uiState.value
                val newIsAllDay = !state.isAllDay

                if (newIsAllDay) {
                    // 하루 종일 ON
                    // 기존에 선택한 날짜가 있다면
                    // 시작 시간은 00:00:00.000,
                    // 종료 시간은 23:59:59.999 로 변경
                    updateState {
                        copy(
                            isAllDay = true,
                            startDateTime = startDateTime?.toAllDayStart(),
                            endDateTime = endDateTime?.toAllDayEnd(),
                            startDateTimeText = startDateTime?.toDateOnlyText(),
                            endDateTimeText = endDateTime?.toDateOnlyText(),
                        )
                    }
                } else {
                    // 하루 종일 OFF
                    // 날짜 선택값은 유지하고,
                    // 이후 DateTimePicker에서 시간을 다시 선택할 수 있도록 함
                    updateState {
                        copy(
                            isAllDay = false,
                            startDateTimeText = startDateTime?.toDateTimeText(),
                            endDateTimeText = endDateTime?.toDateTimeText(),
                        )
                    }
                }
            }

            AdminStudyGroupScheduleAction.ToggleOffline -> {
                updateState {
                    copy(
                        isOffline = !isOffline
                    )
                }
            }

            AdminStudyGroupScheduleAction.ToggleAttendance -> {
                updateState {
                    copy(
                        createAttendance =
                            !createAttendance
                    )
                }
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
                        selectedChallengers =
                            action.challengers,
                        challengerText =
                            action.summaryText,
                    )
                }
            }

            is AdminStudyGroupScheduleAction.SelectWeek -> {
                updateState {
                    copy(
                        weekText =
                            "${action.week}주차",

                        selectedWeeklyCurriculumId =
                            action.weeklyCurriculumId,
                    )
                }
            }

            /**
             * Dialog / BottomSheet 표시 여부는
             * Route에서 관리하므로 ViewModel에서는 처리하지 않습니다.
             */
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

    /**
     * 일정 시작 날짜/시간 저장
     */
    fun updateStartDateTime(
        utcDateTime: String,
    ) {
        val text = formatDateTime(
            utcDateTime = utcDateTime,
            isAllDay = uiState.value.isAllDay,
        ) ?: return

        updateState {
            copy(
                startDateTime = utcDateTime,
                startDateTimeText = text,

                /**
                 * 종료일을 아직 선택하지 않은 경우
                 * 시작일을 기본 종료일로 설정합니다.
                 */
                endDateTime =
                    endDateTime ?: utcDateTime,

                endDateTimeText =
                    endDateTimeText ?: text,
            )
        }
    }

    /**
     * 일정 종료 날짜/시간 저장
     */
    fun updateEndDateTime(
        utcDateTime: String,
    ) {
        val text = formatDateTime(
            utcDateTime = utcDateTime,
            isAllDay = uiState.value.isAllDay,
        ) ?: return

        updateState {
            copy(
                endDateTime = utcDateTime,
                endDateTimeText = text,
            )
        }
    }

    /**
     * 출석 체크 시작 시간 저장
     */
    fun updateCheckInStartDateTime(
        utcDateTime: String,
    ) {
        updateDateTimeText(
            utcDateTime
        ) { text ->
            copy(
                checkInStartDateTime =
                    utcDateTime,
                checkInStartText =
                    text,
            )
        }
    }

    /**
     * 정상 출석 종료 시간 저장
     */
    fun updateOnTimeEndDateTime(
        utcDateTime: String,
    ) {
        updateDateTimeText(
            utcDateTime
        ) { text ->
            copy(
                onTimeEndDateTime =
                    utcDateTime,
                onTimeEndText =
                    text,
            )
        }
    }

    /**
     * 지각 인정 종료 시간 저장
     */
    fun updateLateEndDateTime(
        utcDateTime: String,
    ) {
        updateDateTimeText(
            utcDateTime
        ) { text ->
            copy(
                lateEndDateTime =
                    utcDateTime,
                lateEndText =
                    text,
            )
        }
    }

    /**
     * 일정 등록 대상 스터디 그룹 정보 초기화
     */
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

        // 해당 파트의 커리큘럼 주차 조회
        loadCurriculumWeeks(
            part = groupPart,
        )
    }

    /**
     * 현재 기수와 그룹 파트를 기준으로
     * 선택 가능한 커리큘럼 주차 목록을 조회합니다.
     */
    private fun loadCurriculumWeeks(
        part: String,
    ) {
        viewModelScope.launch {
            when (
                val gisuResult =
                    getActiveGisuUseCase()
            ) {
                is ApiState.Success -> {
                    val activeGisuId =
                        gisuResult.data
                            .gisuId
                            .toLong()

                    when (
                        val curriculumResult =
                            getCurriculumOverviewUseCase(
                                gisuId =
                                    activeGisuId,
                                part =
                                    part,
                            )
                    ) {
                        is ApiState.Success -> {
                            val weeks =
                                curriculumResult
                                    .data
                                    .weeks
                                    .sortedBy {
                                        it.weekNo
                                    }
                                    .map { weeklyCurriculum ->
                                        AdminSubmitWeekUiModel(
                                            week =
                                                weeklyCurriculum.weekNo,

                                            weeklyCurriculumId =
                                                weeklyCurriculum
                                                    .weeklyCurriculumId,
                                        )
                                    }

                            updateState {
                                copy(
                                    weeks = weeks
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

    /**
     * 일정 등록 요청
     */
    private fun registerSchedule() {
        val currentState = uiState.value

        if (
            !currentState.canRegister ||
            currentState.isRegistering
        ) {
            return
        }

        val startsAt =
            currentState.startDateTime
                ?: return

        val endsAt =
            currentState.endDateTime
                ?: return

        val weeklyCurriculumId =
            currentState.selectedWeeklyCurriculumId
                ?: return

        /**
         * 대면 일정인 경우 장소 정보 생성
         *
         * 비대면 일정이면 location = null
         */
        val location =
            if (currentState.isOffline) {
                val latitude =
                    currentState.placeLatitude
                        ?: return

                val longitude =
                    currentState.placeLongitude
                        ?: return

                CreateSchedule.Location(
                    latitude = latitude,
                    longitude = longitude,
                    locationName =
                        currentState.placeText,
                )
            } else {
                null
            }

        /**
         * 출석부 생성이 활성화된 경우
         * 출석 정책 정보를 생성합니다.
         */
        val attendancePolicy =
            if (currentState.createAttendance) {
                val checkInStartAt =
                    currentState.checkInStartDateTime
                        ?: return

                val onTimeEndAt =
                    currentState.onTimeEndDateTime
                        ?: return

                val lateEndAt =
                    currentState.lateEndDateTime
                        ?: return

                CreateSchedule.AttendancePolicy(
                    checkInStartAt =
                        checkInStartAt,
                    onTimeEndAt =
                        onTimeEndAt,
                    lateEndAt =
                        lateEndAt,
                )
            } else {
                null
            }

        /**
         * 일정 생성 API 요청 객체
         */
        val request = CreateSchedule(
            name = currentState.studyName.trim(),
            description =
                currentState.detail.trim(),
            tags = listOf("STUDY"),
            startsAt = startsAt,
            endsAt = endsAt,
            location = location,
            attendancePolicy =
                attendancePolicy,
            participantMemberIds =
                currentState
                    .selectedChallengers
                    .map {
                        it.id
                    }
        )

        viewModelScope.launch {
            updateState {
                copy(
                    isRegistering = true
                )
            }

            when (
                val scheduleResult =
                    createScheduleUseCase(
                        request
                    )
            ) {
                /**
                 * 일정 생성 성공
                 *
                 * 생성된 scheduleId를 이용해
                 * 스터디 그룹과 일정 연결 API 호출
                 */
                is ApiState.Success -> {
                    connectScheduleToStudyGroup(
                        scheduleId =
                            scheduleResult.data,
                        weeklyCurriculumId =
                            weeklyCurriculumId,
                    )
                }

                /**
                 * 일정 생성 실패
                 */
                is ApiState.Fail -> {
                    updateState {
                        copy(
                            isRegistering = false
                        )
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

    /**
     * 생성된 일정을 선택한 스터디 그룹 및 주차와 연결합니다.
     */
    private suspend fun connectScheduleToStudyGroup(
        scheduleId: Long,
        weeklyCurriculumId: Long,
    ) {
        val currentState = uiState.value

        val request =
            CreateStudyGroupScheduleRequest(
                studyGroupId =
                    currentState.groupId,
                scheduleId =
                    scheduleId,
                weeklyCurriculumId =
                    weeklyCurriculumId,
            )

        when (
            createStudyGroupScheduleUseCase(
                request
            )
        ) {
            is ApiState.Success -> {
                updateState {
                    copy(
                        isRegistering = false
                    )
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
                    copy(
                        isRegistering = false
                    )
                }

                emitEvent(
                    AdminStudyGroupScheduleEvent.ShowToast(
                        "스터디 그룹 일정 연결에 실패했어요."
                    )
                )
            }
        }
    }

    /**
     * 출석 관련 날짜/시간을 화면 표시 문자열로 변환합니다.
     *
     * 출석 시간은 항상 날짜 + 시간을 표시합니다.
     */
    private fun updateDateTimeText(
        utcDateTime: String,
        reducer:
        AdminStudyGroupScheduleState.(String) ->
        AdminStudyGroupScheduleState,
    ) {
        val text = formatDateTime(
            utcDateTime = utcDateTime,
            isAllDay = false,
        ) ?: return

        updateState {
            reducer(text)
        }
    }

    /**
     * UTC 날짜/시간 문자열을
     * 화면에 표시할 형식으로 변환합니다.
     *
     * 하루 종일 일정
     * -> yyyy.MM.dd
     *
     * 일반 일정
     * -> yyyy.MM.dd · 오전/오후 h:mm
     */
    private fun formatDateTime(
        utcDateTime: String,
        isAllDay: Boolean,
    ): String? {
        val sdf = SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            Locale.getDefault()
        ).apply {
            timeZone =
                TimeZone.getTimeZone("UTC")
        }

        return try {
            val date =
                sdf.parse(utcDateTime)
                    ?: return null

            if (isAllDay) {
                displayDateSdf.format(date)
            } else {
                displayDateTimeSdf.format(date)
            }
        } catch (e: Exception) {
            Log.e(
                "AdminGroupSchedule",
                "date parse error: ${e.message}"
            )

            null
        }
    }

    /**
     * 선택된 날짜의 시작 시간으로 변경
     *
     * 예)
     * 2026-08-20T15:30:00.000Z
     * ->
     * 2026-08-20T00:00:00.000Z
     */
    private fun String.toAllDayStart(): String {
        val date = take(10)

        return "${date}T00:00:00.000Z"
    }

    /**
     * 선택된 날짜의 마지막 시간으로 변경
     *
     * 예)
     * 2026-08-20T15:30:00.000Z
     * ->
     * 2026-08-20T23:59:59.999Z
     */
    private fun String.toAllDayEnd(): String {
        val date = take(10)

        return "${date}T23:59:59.999Z"
    }

    /**
     * 하루 종일 일정에서 화면에 날짜만 표시
     *
     * 예)
     * 2026-08-20T00:00:00.000Z
     * ->
     * 2026.08.20
     */
    private fun String.toDateOnlyText(): String {
        return take(10).replace("-", ".")
    }

    /**
     * 하루 종일을 해제했을 때 날짜/시간 형식으로 표시
     *
     * 실제 프로젝트에서 기존에 사용 중인 날짜 포맷 함수가 있다면
     * 이 함수 대신 기존 함수를 사용하는 것이 좋음
     */
    private fun String.toDateTimeText(): String {
        val date = take(10).replace("-", ".")
        val time = substringAfter("T")
            .take(5)

        return "$date $time"
    }
}