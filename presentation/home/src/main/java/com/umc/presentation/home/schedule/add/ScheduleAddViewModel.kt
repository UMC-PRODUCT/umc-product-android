package com.umc.presentation.home.schedule.add

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.umc.component.R
import com.umc.component.base.BaseViewModel
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.umc.component.component.UToast
import com.umc.component.component.UToastState
import com.umc.component.util.UTimeFormat
import com.umc.domain.model.UserInfo
import com.umc.domain.model.enums.CategoryType
import com.umc.domain.model.enums.UserPart
import com.umc.domain.model.home.CategoryItem
import com.umc.domain.model.home.LocationItem
import com.umc.domain.model.home.ParticipantItem
import com.umc.domain.model.home.PlanDetailItem
import com.umc.domain.model.home.getGisuSummaryList
import com.umc.domain.model.home.schedule.CreateSchedule
import com.umc.domain.model.home.schedule.UpdateSchedule
import com.umc.domain.usecase.appDataStore.GetUserInfoUseCase
import com.umc.domain.usecase.challenger.SearchChallengerScheduleUseCase
import com.umc.domain.usecase.member.GetMemberProfileUseCase
import com.umc.domain.usecase.schedule.CreateScheduleUseCase
import com.umc.domain.usecase.schedule.GetScheduleDetailHomeUseCase
import com.umc.domain.usecase.schedule.UpdateScheduleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import kotlin.plus
import kotlin.text.isEmpty

@HiltViewModel
class ScheduleAddViewModel @Inject
constructor(
    savedStateHandle: SavedStateHandle, //nav에서 인자로 넘긴 일정 id 체크용
    private val getUserInfoUseCase: GetUserInfoUseCase, //유저 정보 가져오기
    private val getScheduleDetailHomeUseCase: GetScheduleDetailHomeUseCase, //일정 상세 정보 가져오기,
    private val createScheduleUseCase: CreateScheduleUseCase, //일정 생성하기
    private val updateScheduleUseCase: UpdateScheduleUseCase, //일정 수정하기
    private val getMemberProfileUseCase: GetMemberProfileUseCase, //유저 정보 가져오
): BaseViewModel<ScheduleAddUiState, ScheduleAddEvent>(
    ScheduleAddUiState()
) {

    // 날짜 및 시간 포맷팅 및 파싱을 위한 SimpleDateFormat 변수들
    private val dateDisplaySdf = SimpleDateFormat("yyyy.MM.dd", Locale.KOREAN)
    private val timeDisplaySdf = SimpleDateFormat("a h:mm", Locale.KOREAN)
    private val parseDateSdf = SimpleDateFormat("yyyy.MM.dd", Locale.KOREAN)
    private val parseTimeSdf = SimpleDateFormat("HH:mm", Locale.KOREAN)

    // SavedStateHandle에서 전달받은 수정 대상 일정 ID 변수 (-1L인 경우 신규 작성)
    private val checkScheduleId: Long = savedStateHandle.get<Long>("scheduleId") ?: -1L


    init {
        // 초기 유저 정보 로드 실행
        loadInitialData()

        // 전달받은 일정 ID가 존재하는 경우 일정 수정 모드로 데이터 바인딩 실행
        if(checkScheduleId != -1L){
            settingUpdateSchedule(checkScheduleId)
        }
    }

    /**
     * 앱 내 유저 기본 정보 및 최신 기수/운영진 권한 여부를
     * 조회하는 메서드
     */
    private fun loadInitialData() {
        viewModelScope.launch {
            //유저 정보 가져오기
                getUserInfoUseCase().collect { userInfo ->

                    //기수 요약 리스트 작성
                    val gisuSummaryList = userInfo.getGisuSummaryList()
                    val latestGisu = gisuSummaryList.maxByOrNull { it.gisu }
                    //최신 기수ID 얻기
                    val latestGisuId = latestGisu?.gisuId ?: 1L
                    //운영진 인지 판단
                    val isManger = latestGisu?.fromRoles?.isNotEmpty() ?: false

                    updateState {
                        copy(
                            myInfo = userInfo,
                            nowGisuId = latestGisuId,
                            isManager = isManger
                        )
                    }
                }

        }
    }

    /**
     * 기존 일정 수정 모드 진입 시 서버에서 상세 데이터를 불러와
     * UI State에 반영하는 메서드
     *
     * @param scheduleId 수정할 일정 고유 ID
     */
    private fun settingUpdateSchedule(scheduleId: Long) {
        updateState {
            copy(
                editMode = true,
                updateScheduleId = scheduleId
            )
        }

        viewModelScope.launch {
            resultResponse(
                response = getScheduleDetailHomeUseCase(scheduleId),
                successCallback = { detail ->

                    val users = detail.participantMembers
                    val usersIdList = users.map { it ->
                        it.memberId
                    }

                    // 참석자 회원들의 프로필 정보를 병렬로 비동기 로드한 뒤 UI에 적용
                    loadParticipantsProfiles(usersIdList) { participants ->
                        // 받아온 데이터를 UI 상태에 맞게 가공 및 반영
                        applyScheduleDetail(detail, participants)
                    }
                },
                errorCallback = {}
            )
        }
    }

    /**
     * 참석자 ID 리스트를 받아 각 회원의 상세 프로필 정보를 병렬로 불러와 ParticipantItem 목록으로 가공하는 메서드
     *
     * @param memberIds 참석자 회원 ID 리스트
     * @param onComplete 가공이 완료된 ParticipantItem 리스트를 전달받는 콜백
     */
    private fun loadParticipantsProfiles(
        memberIds: List<Long>,
        onComplete: (List<ParticipantItem>) -> Unit
    ) {
        viewModelScope.launch {
            /**코루틴 async를 이용해 각 회원의 프로필 조회를 동시 병렬 요청**/

            //각 유저들의 memberId로 정보들 불러오기
            val tasks = memberIds.map { id -> async { getMemberProfileUseCase(id) } }
            val responses = tasks.awaitAll()

            //각 유저들에 대한 ParticipantItem 만들기
            val participants = responses.mapNotNull { res ->
                var item: ParticipantItem? = null
                resultResponse(
                    response = res,
                    successCallback = { profile ->
                        //최신 기수 정보 가져오기
                        val gisuSummary = profile.getGisuSummaryList().maxByOrNull { it.gisu }
                        item = ParticipantItem(
                            id = profile.id,
                            name = profile.name,
                            nickname = profile.nickname,
                            school = profile.schoolName,
                            profileImage = profile.profileImageLink,
                            gisu = gisuSummary?.gisu ?: 0L,
                            userPart = UserPart.from(gisuSummary?.fromRecords?.get(0)?.responsiblePart)
                        )
                    })
                    item
            }

            //UI 작업 함수 핸들링
            onComplete(participants)
        }
    }

    /**
     * 서버에서 가져온 일정 상세 도메인 데이터(PlanDetailItem)를 ViewModel UI 상태에 파싱 및 적용하는 메서드
     *
     * @param detail 서버에서 수신한 일정 상세 정보
     * @param participants 가공 완료된 참석자 목록
     */
    private fun applyScheduleDetail(detail: PlanDetailItem, participants: List<ParticipantItem>){
        updateState {

            // 하루종일 설정 여부 판별 (00:00 시작 ~ 23:59 종료 기준)
            val checkIsAllDay = detail.startTime.trim() == "00:00" && detail.endTime.trim() == "23:59"

            //1. 도메인 String -> 내부 연산용 Calendar 생성
            val startCal = stringToCalendar(detail.startDay, detail.startTime)
            val endCal = stringToCalendar(detail.endDay, detail.endTime)

            //2. 도메인 시간 문자열 -> UI 표시용 "오전/오후" 변환
            val startTimeTextFormatted = UTimeFormat.formatToAmPm(detail.startTime)
            val endTimeTextFormatted = UTimeFormat.formatToAmPm(detail.endTime)

            //3. 출석부 데이터 유효성 판단 (체크인 시작 날짜 및 시간이 비어있지 않은지)
            val hasAttendancePolicy = detail.checkInStartDay.isNotBlank() && detail.checkInStartTime.isNotBlank()

            //4. 출석부 시간 파싱 (데이터가 있는 경우에만 Calendar 변환, 없으면 기본 Calendar)
            val checkInStartCal = if (hasAttendancePolicy) stringToCalendar(detail.checkInStartDay, detail.checkInStartTime) else Calendar.getInstance()
            val onTimeEndCal = if (hasAttendancePolicy) stringToCalendar(detail.onTimeEndDay, detail.onTimeEndTime) else Calendar.getInstance()
            val lateEndCal = if (hasAttendancePolicy) stringToCalendar(detail.lateEndDay, detail.lateEndTime) else Calendar.getInstance()

            //5. 출석부 UI 표시용 텍스트 가공 ("yyyy.MM.dd" 및 "오전/오후" 포맷)
            val checkInStartDateTextFormatted = if (hasAttendancePolicy) detail.checkInStartDay else ""
            val checkInStartTimeTextFormatted = if (hasAttendancePolicy) UTimeFormat.formatToAmPm(detail.checkInStartTime) else ""

            val onTimeEndDateTextFormatted = if (hasAttendancePolicy) detail.onTimeEndDay else ""
            val onTimeEndTimeTextFormatted = if (hasAttendancePolicy) UTimeFormat.formatToAmPm(detail.onTimeEndTime) else ""

            val lateEndDateTextFormatted = if (hasAttendancePolicy) detail.lateEndDay else ""
            val lateEndTimeTextFormatted = if (hasAttendancePolicy) UTimeFormat.formatToAmPm(detail.lateEndTime) else ""

            //6. 카테고리 매칭
            val updatedCategories = categories.map { item ->
                item.copy(isChecked = detail.tags.any { it.label == item.name })
            }
            val selectedOnes = updatedCategories.filter { it.isChecked }
            val summaryText = when {
                selectedOnes.isEmpty() -> ""
                selectedOnes.size <= 3 -> selectedOnes.joinToString(", ") { it.name }
                else -> "${selectedOnes.take(3).joinToString(", ") { it.name }} 외 ${selectedOnes.size - 3}개"
            }

            //7. 참석자 매칭
            val participantSummaryText = when {
                participants.isEmpty() -> ""
                participants.size == 1 -> participants[0].name
                else -> "${participants[0].name} 외 ${participants.size - 1}명"
            }

            //8. 갱신 저장
            copy(
                planTitle = detail.name,
                planLocation = detail.locationName,
                latitude = detail.latitude,
                longitude = detail.longitude,
                planDetail = detail.description,
                isAllDay = checkIsAllDay,
                startDate = startCal, startTime = startCal,
                endDate = endCal, endTime = endCal,
                categories = updatedCategories,
                isSelectedCategory = true,
                // UI 표시용 텍스트 저장
                startDateText = detail.startDay, // "2026.02.05" 그대로 사용
                startTimeText = startTimeTextFormatted,
                endDateText = detail.endDay,
                endTimeText = endTimeTextFormatted,
                selectedCategoriesString = summaryText,
                selectedParticipants = participants,
                selectedParticipantsString = participantSummaryText,
                //출석부 관련
                isAttendanceChecked = hasAttendancePolicy, // 출석 정보가 존재하면 스위치 켜짐(true)

                checkInStartDate = checkInStartCal,
                checkInStartTime = checkInStartCal,
                onTimeEndDate = onTimeEndCal,
                onTimeEndTime = onTimeEndCal,
                lateEndDate = lateEndCal,
                lateEndTime = lateEndCal,

                checkInStartDateText = checkInStartDateTextFormatted,
                checkInStartTimeText = checkInStartTimeTextFormatted,
                onTimeEndDateText = onTimeEndDateTextFormatted,
                onTimeEndTimeText = onTimeEndTimeTextFormatted,
                lateEndDateText = lateEndDateTextFormatted,
                lateEndTimeText = lateEndTimeTextFormatted,

                isOnlineChecked = detail.isOnline,
            )
        }
    }

    /**
     * 날짜 문자열(yyyy.MM.dd)과 시간 문자열(HH:mm)을 조합해
     * Calendar 객체로 파싱 및 변환하는 메서드
     */
    private fun stringToCalendar(day: String, time: String): Calendar {
        val cal = Calendar.getInstance()
        try {
            val date = parseDateSdf.parse(day) ?: Date()
            val timeData = parseTimeSdf.parse(time) ?: Date()

            cal.time = date // 날짜 세팅 (년, 월, 일)
            val timeCal = Calendar.getInstance().apply { this.time = timeData }

            // 시간 세팅 (시, 분)
            cal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY))
            cal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE))
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
        } catch (e: Exception) {
            Log.e("log_home", "Parsing Error: ${e.message}")
        }
        return cal
    }

    /**
     * 날짜 Calendar와 시간 Calendar 두 개를 합쳐
     * ISO 8601 규격의 UTC 시간 문자열로 변환하는 메서드
     *
     * @return ISO 8601 포맷 문자열 (예: 2026-02-08T09:57:19.628Z)
     */
    private fun getIsoDateTime(dateCal: Calendar, timeCal: Calendar): String {
        //그냥 하나의 타임 포맷으로 바꾸자
        val combineCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, dateCal.get(Calendar.YEAR))
            set(Calendar.MONTH, dateCal.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, dateCal.get(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY))
            set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        return sdf.format(combineCal.time)
    }



    /**
     * 입력을 완료한 일정 데이터를 서버로 생성 또는 수정하여
     * 전송하는 메서드
     *
     * @param isAttendance 출석부 정책 생성 여부 플래그
     */
    fun submitPlan(isAttendance: Boolean){
        val state = uiState.value
        val isEditMode = state.updateScheduleId != -1L

        // 하루종일 여부에 따라 시작(00:00:00) 및 종료(23:59:59) 시각 보정 연산
        val startCal = (state.startDate.clone() as Calendar).apply {
            if (state.isAllDay) {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
        }

        val endCal = (state.endDate.clone() as Calendar).apply {
            if (state.isAllDay) {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }
        }

        //날짜 데이터 ISO 8601 포맷으로 변환
        val startsAt = getIsoDateTime(startCal, if (state.isAllDay) startCal else state.startTime)
        val endsAt = getIsoDateTime(endCal, if (state.isAllDay) endCal else state.endTime)

        //선택한 카테고리 enums -> String 문자열 리스트로 변환
        val selectedTags = state.categories
            .filter { it.isChecked }
            .mapNotNull { item ->
                CategoryType.entries.find { it.label == item.name }?.name
            }

        // 작성자 본인 ID를 포함한 최종 참석자 ID 목록 생성 (중복 제거)
        val participantIds = (state.selectedParticipants.map { it.id } + state.myInfo.id).distinct()

        viewModelScope.launch {
            if (isEditMode) {
                // 기존 일정 수정 처리
                
                //출석부 정보 생성
                val attendancePolicy = if (isAttendance) UpdateSchedule.AttendancePolicy(
                    checkInStartAt = getIsoDateTime(state.checkInStartDate, state.checkInStartTime),
                    onTimeEndAt = getIsoDateTime(state.onTimeEndDate, state.onTimeEndTime),
                    lateEndAt = getIsoDateTime(state.lateEndDate, state.lateEndTime)
                ) else null
                
                val request = UpdateSchedule(
                    name = state.planTitle,
                    description = state.planDetail,
                    tags = selectedTags,
                    startsAt = startsAt,
                    endsAt = endsAt,
                    location = if (state.isOnlineChecked) null else UpdateSchedule.Location(state.latitude, state.longitude, state.planLocation),
                    isOnline = state.isOnlineChecked, // true: 비대면 전환
                    isAttendanceRequired = isAttendance,
                    attendancePolicy = attendancePolicy,
                    participantMemberIds = participantIds
                )

                resultResponse(
                    response = updateScheduleUseCase(state.updateScheduleId, request),
                    successCallback = {
                        emitEvent(ScheduleAddEvent.MoveBackPressedEvent)
                    },
                    errorCallback = { error ->
                        emitEvent(ScheduleAddEvent.ShowErrorToast(error.message))
                    /* 에러 처리 */ }
                )
            } else {

                // 신규 일정 생성 처리
                val attendancePolicy = if (isAttendance) CreateSchedule.AttendancePolicy(
                    checkInStartAt = getIsoDateTime(state.checkInStartDate, state.checkInStartTime),
                    onTimeEndAt = getIsoDateTime(state.onTimeEndDate, state.onTimeEndTime),
                    lateEndAt = getIsoDateTime(state.lateEndDate, state.lateEndTime)
                ) else null

                val request = CreateSchedule(
                    name = state.planTitle,
                    description = state.planDetail,
                    tags = selectedTags,
                    startsAt = startsAt,
                    endsAt = endsAt,
                    location = if (state.isOnlineChecked) null else CreateSchedule.Location(state.latitude, state.longitude, state.planLocation),
                    attendancePolicy = attendancePolicy,
                    participantMemberIds = participantIds
                )

                resultResponse(
                    response = createScheduleUseCase(request),
                    successCallback = {
                        emitEvent(ScheduleAddEvent.MoveBackPressedEvent)
                    },
                    errorCallback = { error ->
                        emitEvent(ScheduleAddEvent.ShowErrorToast(error.message))}
                )
            }
        }


    }

    /**
     * 비대면(온라인) 진행 여부를 스위치 토글로 전환하는 메서드
     *
     * @param isOnline 비대면 체크 여부 (true인 경우 기존 기입된 장소 텍스트 및 위경도 초기화)
     */
    fun toggleOnlineCheck(isOnline: Boolean) {
        updateState {
            copy(
                isOnlineChecked = isOnline,
                //비대면 진행 시 기존 기입된 장소 정보는 초기화
                planLocation = if (isOnline) "" else planLocation,
                latitude = if (isOnline) 0.0 else latitude,
                longitude = if (isOnline) 0.0 else longitude
            )
        }
    }

    /**
     * 출석부 생성 여부를 스위치 토글로 전환하는 메서드
     */
    fun toggleAttendanceCheck(isAttendance: Boolean) {
        updateState { copy(isAttendanceChecked = isAttendance) }
    }



    /**
     * 바텀시트 다이얼로그에서 선택 완료된 참석자 정보를
     * 업데이트하는 메서드
     */
    fun updateParticipants(participants: List<ParticipantItem>, participantsString: String) {
        updateState {
            copy(
                selectedParticipants = participants,
                selectedParticipantsString = participantsString
            ) }
    }

    /**
     * 일정 제목 입력값을 업데이트하는 메서드
     */
    fun updatePlanTitle(title: String) = updateState {
        copy(
            planTitle = title
        )
    }

    /**
     * 일정 상세 내용 입력값을 업데이트하는 메서드
     */
    fun updatePlanDetail(detail: String) = updateState {
        copy(
            planDetail = detail
        )
    }

    /**
     * 장소 검색 결과에서 선택된 장소명 및 좌표 정보를 업데이트하는 메서드
     */
    fun updatePlanLocation(location: LocationItem) = updateState {
        copy(
            planLocation = location.title,
            latitude = location.latitude,
            longitude = location.longitude
        )
    }
    

    /**
     * UTC 일시 문자열을 수신받아 
     * 일정 시작 날짜 및 시간을 통합 업데이트하는 메서드
     */
    fun updateStartDateTime(utcDateTime: String) {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        try {
            val date = sdf.parse(utcDateTime) ?: return
            val newCal = Calendar.getInstance().apply { time = date }
            updateState {
                copy(
                    startDate = newCal,
                    startTime = newCal,
                    startDateText = dateDisplaySdf.format(newCal.time),
                    startTimeText = timeDisplaySdf.format(newCal.time)
                )
            }
        } catch (e: Exception) {

            Log.e("log_home", "Parsing Error: ${e.message}")
        }
    }

    /**
     * UTC 일시 문자열을 수신받아
     * 일정 종료 날짜 및 시간을 통합 업데이트하는 메서드
     */
    fun updateEndDateTime(utcDateTime: String) {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        try {
            val date = sdf.parse(utcDateTime) ?: return
            val newCal = Calendar.getInstance().apply { time = date }
            updateState {
                copy(
                    endDate = newCal,
                    endTime = newCal,
                    endDateText = dateDisplaySdf.format(newCal.time),
                    endTimeText = timeDisplaySdf.format(newCal.time)
                )
            }
        } catch (e: Exception) {
            Log.e("log_home", "Parsing Error: ${e.message}")
        }
    }

    /**
     * UTC 일시 문자열을 수신받아
     * 출석 체크인 시작 날짜 및 시간을 통합 업데이트하는 메서드
     */
    fun updateCheckInStartDateTime(utcDateTime: String) {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        try {
            val date = sdf.parse(utcDateTime) ?: return
            val newCal = Calendar.getInstance().apply { time = date }
            updateState {
                copy(
                    checkInStartDate = newCal,
                    checkInStartTime = newCal,
                    checkInStartDateText = dateDisplaySdf.format(newCal.time),
                    checkInStartTimeText = timeDisplaySdf.format(newCal.time)
                )
            }
        } catch (e: Exception) {
            Log.e("log_home", "Parsing Error: ${e.message}")
        }
    }

    /**
     * UTC 일시 문자열을 수신받아
     * 출석 정시 종료 날짜 및 시간을 통합 업데이트하는 메서드
     */
    fun updateOnTimeEndDateTime(utcDateTime: String) {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        try {
            val date = sdf.parse(utcDateTime) ?: return
            val newCal = Calendar.getInstance().apply { time = date }
            updateState {
                copy(
                    onTimeEndDate = newCal,
                    onTimeEndTime = newCal,
                    onTimeEndDateText = dateDisplaySdf.format(newCal.time),
                    onTimeEndTimeText = timeDisplaySdf.format(newCal.time)
                )
            }
        } catch (e: Exception) {
            Log.e("log_home", "Parsing Error: ${e.message}")
        }
    }

    /**
     * UTC 일시 문자열을 수신받아
     * 출석 지각 인정 날짜 및 시간을 통합 업데이트하는 메서드
     */
    fun updateLateEndDateTime(utcDateTime: String) {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        try {
            val date = sdf.parse(utcDateTime) ?: return
            val newCal = Calendar.getInstance().apply { time = date }
            updateState {
                copy(
                    lateEndDate = newCal,
                    lateEndTime = newCal,
                    lateEndDateText = dateDisplaySdf.format(newCal.time),
                    lateEndTimeText = timeDisplaySdf.format(newCal.time)
                )
            }
        } catch (e: Exception) {
            Log.e("log_home", "Parsing Error: ${e.message}")
        }
    }




    /**
    * 카테고리(태그) 선택 시 토글 및 상단 요약 문구를 생성하여 업데이트하는 메서드
    *
    * @param category 사용자가 선택한 카테고리 아이템
    */
    fun selectCategory(category: CategoryItem) {
        updateState {
            //카테고리 uistate 업데이트
            val selectedCategories = categories.map{
                //만약 터치한 놈이 리스트 중 하나랑 같으면
                if(it.name == category.name){
                    it.copy(isChecked = !it.isChecked)
                }
                else{
                    it.copy(isChecked = it.isChecked)
                }
            }

            //선택된 카테코리 필터링
            val selectedOnes = selectedCategories.filter { it.isChecked }
            val isSelected = when {
                selectedOnes.isEmpty() -> false
                else -> true
            }

            // 선택 개수에 따른 요약 문구 가공 (예: "스터디, 프로젝트, 모임 외 2개")
            val summaryText = when {
                selectedOnes.isEmpty() -> ""
                selectedOnes.size <= 3 -> selectedOnes.joinToString(", ") { it.name }
                else -> "${selectedOnes.take(3).joinToString(", ") { it.name }} 외 ${selectedOnes.size - 3}개"
            }

            copy(categories = selectedCategories,
                isSelectedCategory = isSelected,
                selectedCategoriesString = summaryText
            )
        }
    }



    /**
     * 하루종일 설정 스위치 상태를 업데이트하는 메서드
     */
    fun setAllday(isAllday: Boolean) {
        updateState {
            copy(
                isAllDay = isAllday
            )
        }
    }
}

data class ScheduleAddUiState(
    //챌린저 내 정보
    val myInfo : UserInfo = UserInfo(),

    //내 최신 기수
    val nowGisuId: Long = -1L,

    ////스케쥴 수정용 id
    val updateScheduleId : Long = -1L,
    val editMode : Boolean = false,


    //운영진 여부 판단
    val isManager: Boolean = false,


    //하루 종일 부분에 체크가 되었나
    val isAllDay: Boolean = false,

    //일정 및 장소 관련
    val planTitle: String = "",    //필수
    val planLocation: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val planDetail: String = "",

    //시간 관련 Calendar 및 표시용 텍스트 상태
    val startDate: Calendar = Calendar.getInstance(),
    val startTime: Calendar = Calendar.getInstance(),
    val endDate: Calendar = Calendar.getInstance(),
    val endTime: Calendar = Calendar.getInstance(),
    val startDateText: String = "",
    val startTimeText: String = "",
    val endDateText : String = "",
    val endTimeText : String = "",
    // 출석 정책 (운영진 전용)
    val checkInStartDate: Calendar = Calendar.getInstance(),
    val checkInStartTime: Calendar = Calendar.getInstance(),
    val onTimeEndDate: Calendar = Calendar.getInstance(),
    val onTimeEndTime: Calendar = Calendar.getInstance(),
    val lateEndDate: Calendar = Calendar.getInstance(),
    val lateEndTime: Calendar = Calendar.getInstance(),
    val checkInStartDateText: String = "",
    val checkInStartTimeText: String = "",
    val onTimeEndDateText: String = "",
    val onTimeEndTimeText: String = "",
    val lateEndDateText: String = "",
    val lateEndTimeText: String = "",

    // 참석자 및 비대면/출석 스위치 상태
    val selectedParticipants: List<ParticipantItem> = emptyList(), //선택된 참여자 결과(recyclerview에 쓰임)
    val selectedParticipantsString : String = "", //cdv에 보여줄 string

    val isOnlineChecked: Boolean = false,
    val isAttendanceChecked: Boolean = false,


    //카테고리 리스트
    val categories: List<CategoryItem> = listOf(
        CategoryItem(CategoryType.NETWORKING.label, R.drawable.ic_networking_off, R.drawable.ic_networking_on),
        CategoryItem(CategoryType.PROJECT.label, R.drawable.ic_project_off, R.drawable.ic_project_on),
        CategoryItem(CategoryType.DUES.label, R.drawable.ic_fees_off, R.drawable.ic_fees_on),
        CategoryItem(CategoryType.MEETING.label, R.drawable.ic_meeting_off, R.drawable.ic_meeting_on),

        CategoryItem(CategoryType.ORIENTATION.label, R.drawable.ic_orientation_off, R.drawable.ic_orientation_on),
        CategoryItem(CategoryType.PRESENTATION.label, R.drawable.ic_presentation_off, R.drawable.ic_presentation_on),
        CategoryItem(CategoryType.RETROSPECTIVE.label, R.drawable.ic_retrospective_off, R.drawable.ic_retrospective_on),
        CategoryItem(CategoryType.GENERAL.label, R.drawable.ic_general_off, R.drawable.ic_general_on),

        CategoryItem(CategoryType.LEADERSHIP.label, R.drawable.ic_leadership_off, R.drawable.ic_leadership_on),
        CategoryItem(CategoryType.STUDY.label, R.drawable.ic_study_off, R.drawable.ic_study_on),
        CategoryItem(CategoryType.HACKATHON.label, R.drawable.ic_hackathon_off, R.drawable.ic_hackathon_on),
        CategoryItem(CategoryType.WORKSHOP.label, R.drawable.ic_workshop_off, R.drawable.ic_workshop_on),
        /**아이콘 임시조치**/
        CategoryItem(CategoryType.AFTER_PARTY.label, R.drawable.ic_afterparty_off, R.drawable.ic_afterparty_on)

    ),
    val isSelectedCategory: Boolean = false, //UI에 placeholder or text 보여줄지 판단하는 변수
    val selectedCategoriesString: String = ""



) : UiState {
    // 참석자 존재 여부를 실시간으로 계산하는 프로퍼티
    val isSelectedParticipant: Boolean
        get() = selectedParticipants.isNotEmpty()


    // 필수 입력 조건(제목, 카테고리, 대면 장소) 통과 여부를 실시간으로 연산하여 등록 버튼 활성화 상태를 반환하는 프로퍼티
    val isRegisterOk: Boolean get() = planTitle.isNotBlank() && isSelectedCategory && (isOnlineChecked || planLocation.isNotEmpty())
}

sealed interface ScheduleAddEvent : UiEvent {

    //뒤로가기
    object MoveBackPressedEvent : ScheduleAddEvent
    data class ShowErrorToast(val message: String) : ScheduleAddEvent

}