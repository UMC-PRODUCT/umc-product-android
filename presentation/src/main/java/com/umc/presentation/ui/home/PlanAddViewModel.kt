package com.umc.presentation.ui.home

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.umc.domain.model.UserInfo
import com.umc.domain.model.enums.CategoryType
import com.umc.domain.model.enums.UserPart
import com.umc.domain.model.home.CategoryItem
import com.umc.domain.model.home.LocationItem
import com.umc.domain.model.home.ParticipantItem
import com.umc.domain.model.home.schedule.CreateSchedule
import com.umc.domain.model.home.schedule.UpdateSchedule
import com.umc.domain.usecase.appDataStore.GetUserInfoUseCase
import com.umc.domain.usecase.appDataStore.recent.GetRecentSearchPlaceUseCase
import com.umc.domain.usecase.appDataStore.recent.UpdateRecentSearchPlaceUseCase
import com.umc.domain.usecase.kakao.GetSearchLocationUseCase
import com.umc.domain.usecase.schedule.CreateScheduleUseCase
import com.umc.domain.usecase.schedule.GetScheduleDetailHomeUseCase
import com.umc.domain.usecase.schedule.UpdateScheduleUseCase
import com.umc.presentation.R
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject


@HiltViewModel
class PlanAddViewModel @Inject
constructor(
    private val getRecentSearchPlaceUseCase: GetRecentSearchPlaceUseCase, //최근 장소 기록 불러오기;
    private val updateRecentSearchPlaceUseCase: UpdateRecentSearchPlaceUseCase, //최근 장소 기록 업데이트하기
    private val getSearchLocationUseCase: GetSearchLocationUseCase, //카카오 SDK로 장소 검색하기
    private val getUserInfoUseCase: GetUserInfoUseCase, //유저 정보 가져오기
    private val getScheduleDetailHomeUseCase: GetScheduleDetailHomeUseCase, //일정 상세 정보 가져오기,
    private val createScheduleUseCase: CreateScheduleUseCase, //일정 생성하기
    private val updateScheduleUseCase: UpdateScheduleUseCase, //일정 수정하기
) : BaseViewModel<PlanAddFragmentUiState, PlanAddFragmentEvent>(
    PlanAddFragmentUiState()){

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
                    updateState { copy(myInfo = userInfo) }
                }
            }
        }
    }

    //일정 수정 시 기존 일정 데이터로 UI 채우기
    fun settingUpdateSchedule(scheduleId: Long) {
        viewModelScope.launch {
            resultResponse(
                response = getScheduleDetailHomeUseCase(scheduleId),
                successCallback = { detail ->
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
                            selectedCategoriesString = summaryText
                        )
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
     */
    private fun getIsoDateTime(dateCal: Calendar, timeCal: Calendar): String {
        return String.format(
            Locale.getDefault(),
            "%d-%02d-%02dT%02d:%02d:00.000Z",
            dateCal.get(Calendar.YEAR),
            dateCal.get(Calendar.MONTH) + 1,
            dateCal.get(Calendar.DAY_OF_MONTH),
            timeCal.get(Calendar.HOUR_OF_DAY),
            timeCal.get(Calendar.MINUTE)
        )
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


    /**일정을 생성 or 수정하는 함수**/
    fun submitPlan(){
        val state = uiState.value
        val isEditMode = state.updateScheduleId != -1L

        //날짜 데이터 ISO 8601 포맷으로 변환
        val startsAt = getIsoDateTime(state.startDate, state.startTime)
        val endsAt = getIsoDateTime(state.endDate, state.endTime)

        //선택한 카테고리 enums -> String list로
        val selectedTags = state.categories
            .filter { it.isChecked }
            .mapNotNull { item ->
                CategoryType.entries.find { it.label == item.name }?.name
            }

        //TODO 참여자 ID 임시 하드코딩 (차후 수정 가능하도록 리스트로 관리)
        val tempParticipantIds = listOf(101L, 102L)

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
                    tags = selectedTags
                )

                resultResponse(
                    response = updateScheduleUseCase(state.updateScheduleId, request),
                    successCallback = {
                        emitEvent(PlanAddFragmentEvent.MoveBackPressedEvent)
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
                    /**TODO 차후 확실히**/
                    participantMemberIds = tempParticipantIds,
                    gisuId = 1L,
                    requiresApproval = state.isManager
                )

                resultResponse(
                    response = createScheduleUseCase(request),
                    successCallback = {
                        emitEvent(PlanAddFragmentEvent.MoveBackPressedEvent)
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
    
    /**이벤트 핸들러 정의**/
    //handleEvent
    fun handleEvent(event: PlanAddFragmentEvent){
        when(event){
            // 1. 날짜 및 시간 관련 이벤트
            is PlanAddFragmentEvent.UpdateStartDate,
            is PlanAddFragmentEvent.UpdateStartTime,
            is PlanAddFragmentEvent.UpdateEndDate,
            is PlanAddFragmentEvent.UpdateEndTime -> handleDateTime(event)



            // 3. 카테고리 관련 이벤트
            is PlanAddFragmentEvent.SelectCategory -> handleCategory(event)

            // 4. 기타 관련
            is PlanAddFragmentEvent.UpdatePlanTitle -> {
                updateState {
                    copy(planTitle = event.title)
                }
            }
            is PlanAddFragmentEvent.UpdatePlanLocation -> {
                updateState {
                    copy(planLocation = event.location.title,
                        latitude = event.location.latitude,
                        longitude = event.location.longitude
                    )
                }
            }
            is PlanAddFragmentEvent.UpdatePlanDetail -> {
                updateState {
                    copy(planDetail = event.detail)
                }
            }

            else -> {}
        }
    }


    //날짜 조정 (얘는 Fragment에서 날짜 받아온 후 수행하는 이벤트입니다)
    private fun handleDateTime(event: PlanAddFragmentEvent){
        when(event){
            //시작 날짜 바꾸기
            is PlanAddFragmentEvent.UpdateStartDate -> {
                val newCalendar = (uiState.value.startDate.clone() as Calendar).apply {
                    set(event.year, event.month, event.day)
                }
                updateState {
                    copy(startDate = newCalendar,
                        startDateText = dateDisplaySdf.format(newCalendar.time)
                    )
                }
            }

            //시작 시간 바꾸기
            is PlanAddFragmentEvent.UpdateStartTime -> {
                val newCalendar = (uiState.value.startTime.clone() as Calendar).apply {
                    set(Calendar.HOUR_OF_DAY, event.hour)
                    set(Calendar.MINUTE, event.minute)
                }
                updateState {
                    copy(startTime = newCalendar,
                        startTimeText = timeDisplaySdf.format(newCalendar.time)
                    )

                }
            }

            //끝나는 날짜 바꾸기
            is PlanAddFragmentEvent.UpdateEndDate -> {
                val newCalendar = (uiState.value.endDate.clone() as Calendar).apply {
                    set(event.year, event.month, event.day)
                }
                updateState {
                    copy(endDate = newCalendar,
                        endDateText = dateDisplaySdf.format(newCalendar.time)
                    )

                }
            }

            //끝나는 시간 바꾸기
            is PlanAddFragmentEvent.UpdateEndTime -> {
                val newCalendar = (uiState.value.endTime.clone() as Calendar).apply {
                    set(Calendar.HOUR_OF_DAY, event.hour)
                    set(Calendar.MINUTE, event.minute)
                }
                updateState {
                    copy(endTime = newCalendar,
                        endTimeText = timeDisplaySdf.format(newCalendar.time)
                    )
                }
            }

            else -> {}
        }
    }


    // 카테고리 관련 handleEvent
    private fun handleCategory(event: PlanAddFragmentEvent) {
        when(event){
            is PlanAddFragmentEvent.SelectCategory -> {
                updateState {
                    //카테고리 uistate 업데이트
                    val selectedCategories = categories.map{
                        //만약 터치한 놈이 리스트 중 하나랑 같으면
                        if(it.name == event.category.name){
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

                else -> {}
        }
    }



    //하루종일 관련
    //모집 중 스위치 누를 때마다 상태 변화하고 필터링
    fun setAllday(isAllday: Boolean) {
        updateState { copy(isAllDay = isAllday) }
    }

    

}


data class PlanAddFragmentUiState(
    //챌린저 내 정보
    val myInfo : UserInfo = UserInfo(),

    ////스케쥴 수정용 id
    val updateScheduleId : Long = -1L,


    //운영진 여부 판단
    val isManager: Boolean = true,

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

            //2. 날짜/시간이 초기값이 아님
            val isDateTimeValid = (isAllDay && (startDateText != "시작 날짜" && endDateText != "종료 날짜"))
                    || (!isAllDay && (startDateText != "시작 날짜" && startTimeText != "시작 시간" &&
                            endDateText != "종료 날짜" && endTimeText != "종료 시간"))

            //3. 참여자가 1명 이상임
            val isParticipantValid = isSelectedParticipant

            return isTextValid && isDateTimeValid && isParticipantValid
        }
}

sealed interface PlanAddFragmentEvent : UiEvent {
    //일정 제목이랑 장소, 상세안내를 입력받는 이벤트
    data class UpdatePlanTitle(val title: String) : PlanAddFragmentEvent
    data class UpdatePlanLocation(val location: LocationItem) : PlanAddFragmentEvent
    data class UpdatePlanDetail(val detail: String) : PlanAddFragmentEvent


    //TIME 및 DATE Picker로 값을 가져오는 이벤트
    data class UpdateStartDate(val year: Int, val month: Int, val day: Int) : PlanAddFragmentEvent
    data class UpdateStartTime(val hour: Int, val minute: Int) : PlanAddFragmentEvent
    data class UpdateEndDate(val year: Int, val month: Int, val day: Int) : PlanAddFragmentEvent
    data class UpdateEndTime(val hour: Int, val minute: Int) : PlanAddFragmentEvent


    //다이얼로그에서 받은 

    //카테코리를 선택하는 이벤트
    data class SelectCategory(val category: CategoryItem): PlanAddFragmentEvent

    //뒤로가기
    object MoveBackPressedEvent : PlanAddFragmentEvent

}