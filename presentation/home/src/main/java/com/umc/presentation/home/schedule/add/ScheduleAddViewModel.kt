package com.umc.presentation.home.schedule.add

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.umc.component.R
import com.umc.component.base.BaseViewModel
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.umc.domain.model.UserInfo
import com.umc.domain.model.enums.CategoryType
import com.umc.domain.model.enums.UserPart
import com.umc.domain.model.home.CategoryItem
import com.umc.domain.model.home.LocationItem
import com.umc.domain.model.home.ParticipantItem
import com.umc.domain.model.home.getGisuSummaryList
import com.umc.domain.model.home.schedule.CreateSchedule
import com.umc.domain.model.home.schedule.UpdateSchedule
import com.umc.domain.usecase.appDataStore.GetUserInfoUseCase
import com.umc.domain.usecase.member.GetMemberProfileUseCase
import com.umc.domain.usecase.schedule.CreateScheduleUseCase
import com.umc.domain.usecase.schedule.GetScheduleDetailHomeUseCase
import com.umc.domain.usecase.schedule.UpdateScheduleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

@HiltViewModel
class ScheduleAddViewModel @Inject
constructor(
    private val getUserInfoUseCase: GetUserInfoUseCase, //유저 정보 가져오기
    private val getScheduleDetailHomeUseCase: GetScheduleDetailHomeUseCase, //일정 상세 정보 가져오기,
    private val createScheduleUseCase: CreateScheduleUseCase, //일정 생성하기
    private val updateScheduleUseCase: UpdateScheduleUseCase, //일정 수정하기
    private val getMemberProfileUseCase: GetMemberProfileUseCase, //유저 정보 가져오기
): BaseViewModel<ScheduleAddUiState, ScheduleAddEvent>(
    ScheduleAddUiState()
) {
    //Calender를 String으로 변경하는 포맷들이 담김
    private val dateDisplaySdf = SimpleDateFormat("yyyy.MM.dd", Locale.KOREAN)
    private val timeDisplaySdf = SimpleDateFormat("a h:mm", Locale.KOREAN)
    private val parseDateSdf = SimpleDateFormat("yyyy.MM.dd", Locale.KOREAN)
    private val parseTimeSdf = SimpleDateFormat("HH:mm", Locale.KOREAN)



    init {
        loadInitialData()
    }

    /**초기 데이터 가져오는 작업들 정의 및 파싱 함수들 정의**/
    // 초기 데이터(장소 검색 기록 및 유저 정보 가져오기)
    private fun loadInitialData() {
        viewModelScope.launch {
            //유저 정보 가져오기
            launch {
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
    }

    //일정 수정 시 기존 일정 데이터로 UI 채우기
    fun settingUpdateSchedule(scheduleId: Long) {

        updateState { copy(editMode = true) }

        viewModelScope.launch {
            resultResponse(
                response = getScheduleDetailHomeUseCase(scheduleId),
                successCallback = { detail ->

                    //유저 정보를 위해 getMemberProfileUsecase로 호출
                    viewModelScope.launch {
                        /**추가 로직**/
                        //async로 대기하자
                        //val participantsIds = listOf<Long>(16L, 17L, 18L)
                        val participantTasks = detail.participantMemberIds.map { memberId ->
                            async { getMemberProfileUseCase(memberId) }
                        }
                        val participants = participantTasks.awaitAll().mapNotNull { response ->
                            var item: ParticipantItem? = null
                            resultResponse(
                                response = response,
                                successCallback = { profile ->

                                    //최신 기수 정보 가져오기
                                    val gisuSummary = profile.getGisuSummaryList().maxByOrNull { it.gisu }
                                    val challengerPart = UserPart.from(gisuSummary?.fromRecords?.get(0)?.responsiblePart)

                                    item = ParticipantItem(
                                        id = profile.id,
                                        name = profile.name,
                                        nickname = profile.nickname,
                                        school = profile.schoolName,
                                        profileImage = profile.profileImageLink,
                                        gisu = gisuSummary?.gisu ?: 0L,
                                        userPart = challengerPart
                                    )
                                },
                                errorCallback = {
                                }
                            )
                            item
                        }
                        /**추가 로직 종료**/

                        updateState {
                            //1. 도메인 String -> 내부 연산용 Calendar 생성
                            val startCal = stringToCalendar(detail.startDay, detail.startTime)
                            val endCal = stringToCalendar(detail.endDay, detail.endTime)

                            //2. 도메인 시간 문자열 -> UI 표시용 "오전/오후" 변환
                            val startTimeTextFormatted = formatToAmPm(detail.startTime)
                            val endTimeTextFormatted = formatToAmPm(detail.endTime)

                            //3. 카테고리 매칭
                            val updatedCategories = categories.map { item ->
                                item.copy(isChecked = detail.tags.any { it.label == item.name })
                            }
                            val selectedOnes = updatedCategories.filter { it.isChecked }
                            val summaryText = when {
                                selectedOnes.isEmpty() -> ""
                                selectedOnes.size <= 3 -> selectedOnes.joinToString(", ") { it.name }
                                else -> "${selectedOnes.take(3).joinToString(", ") { it.name }} 외 ${selectedOnes.size - 3}개"
                            }

                            //5. 참석자 매칭
                            val participantSummaryText = when {
                                participants.isEmpty() -> ""
                                participants.size == 1 -> participants[0].name
                                else -> "${participants[0].name} 외 ${participants.size - 1}명"
                            }



                            //4. 갱신 저장
                            copy(
                                updateScheduleId = scheduleId,
                                planTitle = detail.name,
                                planLocation = detail.locationName,
                                latitude = detail.latitude,
                                longitude = detail.longitude,
                                planDetail = detail.description,
                                isAllDay = detail.isAllDay,
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
                                selectedParticipantsString = participantSummaryText
                            )
                        }

                    }
                },
                errorCallback = {}
            )
        }
    }

    //string을 이용해 Calendar로 바꾸는 함수
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
     * Calendar 객체 두 개(날짜, 시간)를 합쳐 ISO 8601 문자열로 변환
     * 예: 2026-02-08T09:57:19.628Z
     * TODO 시간 형태 변경 -9시간
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


    // 24시간 형식 ("14:00") -> 12시간 AM/PM 형식 ("오후 02:00")으로 바꾸는 포맷 함수
    private fun formatToAmPm(time: String): String {
        return try {
            val date = parseTimeSdf.parse(time) // "HH:mm" 포맷으로 읽기
            date?.let { timeDisplaySdf.format(it) } ?: "시간 선택"
        } catch (e: Exception) {
            "시간 선택"
        }
    }


    /**일정을 생성 or 수정하는 함수
     * isAttendance = 출석부도 같이 생성하는지 여부
     * **/
    fun submitPlan(isAttendance: Boolean){
        val state = uiState.value
        val isEditMode = state.updateScheduleId != -1L

        //날짜 데이터 ISO 8601 포맷으로 변환
        val startsAt = getIsoDateTime(state.startDate, state.startTime)
        val endsAt = getIsoDateTime(state.endDate, state.endTime)

        Log.d("log_home", "startsAt: $startsAt, endsAt: $endsAt")


        //선택한 카테고리 enums -> String list로
        val selectedTags = state.categories
            .filter { it.isChecked }
            .mapNotNull { item ->
                CategoryType.entries.find { it.label == item.name }?.name
            }


        //내 ID도 추가(만약 이미 있으면 중복 체거)
        val participantIds = (state.selectedParticipants.map { it.id } + state.myInfo.id).distinct()



        viewModelScope.launch {
            if (isEditMode) {
                // [기존 일정 수정]
                val request = UpdateSchedule(
                    name = state.planTitle,
                    startsAt = startsAt,
                    endsAt = endsAt,
                    isAllDay = state.isAllDay,
                    locationName = state.planLocation,
                    latitude = state.latitude,
                    longitude = state.longitude,
                    description = state.planDetail,
                    tags = selectedTags,
                    participantMemberIds = participantIds,
                )

                resultResponse(
                    response = updateScheduleUseCase(state.updateScheduleId, request),
                    successCallback = {
                        emitEvent(ScheduleAddEvent.MoveBackPressedEvent)
                    },
                    errorCallback = { /* 에러 처리 */ }
                )
            } else {
                // [새 일정 생성]
                val request = CreateSchedule(
                    name = state.planTitle,
                    startsAt = startsAt,
                    endsAt = endsAt,
                    isAllDay = state.isAllDay,
                    locationName = state.planLocation,
                    latitude = state.latitude,
                    longitude = state.longitude,
                    description = state.planDetail,
                    tags = selectedTags,
                    participantMemberIds = participantIds,
                    gisuId = state.nowGisuId,
                    requiresApproval = isAttendance
                )

                resultResponse(
                    response = createScheduleUseCase(request),
                    successCallback = {
                        emitEvent(ScheduleAddEvent.MoveBackPressedEvent)
                    },
                    errorCallback = { /* 에러 처리 */ }
                )
            }
        }


    }


    /**viewModel 값 업데이트**/
    // 다이얼로그에서 가져온 참여자 정보를 업데이트 하는 함수
    fun updateParticipants(participants: List<ParticipantItem>, participantsString: String) {
        updateState {
            copy(
                selectedParticipants = participants,
                selectedParticipantsString = participantsString
            ) }
    }

    // 일정 이름 변경
    fun updatePlanTitle(title: String) = updateState { copy(planTitle = title) }

    // 일정 상세내용 변경
    fun updatePlanDetail(detail: String) = updateState { copy(planDetail = detail) }

    // 일정 위치 변경
    fun updatePlanLocation(location: LocationItem) = updateState {
        copy(planLocation = location.title, latitude = location.latitude, longitude = location.longitude)
    }

    // 일정 시작 날짜 변경
    fun updateStartDate(year: Int, month: Int, day: Int) {
        val newCal = (uiState.value.startDate.clone() as Calendar).apply { set(year, month, day) }
        updateState { copy(startDate = newCal, startDateText = dateDisplaySdf.format(newCal.time)) }
    }

    // 일정 시작 시간 변경
    fun updateStartTime(hour: Int, minute: Int) {
        val newCal = (uiState.value.startTime.clone() as Calendar).apply { set(Calendar.HOUR_OF_DAY, hour); set(Calendar.MINUTE, minute) }
        updateState { copy(startTime = newCal, startTimeText = timeDisplaySdf.format(newCal.time)) }
    }

    // 일정 종료 날짜 변경
    fun updateEndDate(year: Int, month: Int, day: Int) {
        val newCal = (uiState.value.endDate.clone() as Calendar).apply { set(year, month, day) }
        updateState { copy(endDate = newCal, endDateText = dateDisplaySdf.format(newCal.time)) }
    }

    // 일정 종료 시간 변경
    fun updateEndTime(hour: Int, minute: Int) {
        val newCal = (uiState.value.endTime.clone() as Calendar).apply { set(Calendar.HOUR_OF_DAY, hour); set(Calendar.MINUTE, minute) }
        updateState { copy(endTime = newCal, endTimeText = timeDisplaySdf.format(newCal.time)) }
    }

    // 카테고리를 선택하면 진행하는 함수
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
            //더미 텍스트 및 선택 여부 반영
            val isSelected = when {
                selectedOnes.isEmpty() -> false
                else -> true
            }
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



    //하루종일 관련
    //모집 중 스위치 누를 때마다 상태 변화하고 필터링
    fun setAllday(isAllday: Boolean) {
        updateState { copy(isAllDay = isAllday) }
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

    //시간 관련
    val startDate: Calendar = Calendar.getInstance(),
    val startTime: Calendar = Calendar.getInstance(),
    val endDate: Calendar = Calendar.getInstance(),
    val endTime: Calendar = Calendar.getInstance(),
    //(필수)
    val startDateText: String = "시작 날짜",
    val startTimeText: String = "시작 시간",
    val endDateText : String = "종료 날짜",
    val endTimeText : String = "종료 시간",


    //인원 검색 관련
    /**TODO 일단은 이름만 받는다고 가정**/
    val selectedParticipants: List<ParticipantItem> = emptyList(), //선택된 참여자 결과(recyclerview에 쓰임)
    val selectedParticipantsString : String = "", //cdv에 보여줄 string

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
    //참여자 명단(recyclerview를 보여주는지 체크 여부)
    val isSelectedParticipant: Boolean
        get() = selectedParticipants.isNotEmpty()


    //최종 여부를 판단하는 실시간 계산
    val isRegisterOk: Boolean
        get() {
            //1. 텍스트 3종 세트가 비어있지 않음
            val isTextValid = planTitle.isNotBlank()

            /** 필수 내용 수정
            //2. 날짜/시간이 초기값이 아님
            val isDateTimeValid = (isAllDay && (startDateText != "시작 날짜" && endDateText != "종료 날짜"))
            || (!isAllDay && (startDateText != "시작 날짜" && startTimeText != "시작 시간" &&
            endDateText != "종료 날짜" && endTimeText != "종료 시간"))

            //3. 참여자가 1명 이상임
            val isParticipantValid = isSelectedParticipant
             **/


            return isTextValid && isSelectedCategory && planLocation != ""
        }
}

sealed interface ScheduleAddEvent : UiEvent {

    //뒤로가기
    object MoveBackPressedEvent : ScheduleAddEvent
    data class ShowErrorToast(val message: String) : ScheduleAddEvent

}