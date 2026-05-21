package com.umc.presentation.home.schedule.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.umc.component.base.BaseViewModel
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.umc.domain.model.enums.PermissionType
import com.umc.domain.model.enums.ResourceType
import com.umc.domain.model.home.PlanDetailItem
import com.umc.domain.usecase.GetAuthAccessUseCase
import com.umc.domain.usecase.schedule.DeleteScheduleUseCase
import com.umc.domain.usecase.schedule.GetScheduleDetailHomeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class ScheduleDetailViewModel @Inject
constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getScheduleDetailHomeUseCase: GetScheduleDetailHomeUseCase, //일정 상세 정보 가져오기
    private val deleteScheduleUseCase: DeleteScheduleUseCase, //일정 삭제하기
    private val getAuthAccessUseCase: GetAuthAccessUseCase, //리소스 권한 조회
) : BaseViewModel<ScheduleDetailUiState, ScheduleDetailEvent>(
    ScheduleDetailUiState()){

    private val checkScheduleId: Long = savedStateHandle.get<Long>("scheduleId") ?: -1L
    private val checkPlusDay: Int = savedStateHandle.get<Int>("plusDay") ?: -1


    init{
        if(checkScheduleId != -1L && checkPlusDay != -1) {
            getScheduleDetail(checkScheduleId, checkPlusDay)
        }
    }


    //서버에서 게시글 상세 정보 가져오기
    fun getScheduleDetail(scheduleId : Long, plusDay: Int){
        viewModelScope.launch {
            resultResponse(
                response = getScheduleDetailHomeUseCase(scheduleId),
                successCallback = {
                    updateState { copy(
                        content = it,
                        plusDay = plusDay)
                    }
                    settingScheduleAuthAccess(it.scheduleId)

                    convertPlanDetailItemToUiState(it, plusDay)
                },
                errorCallback = {

                }
            )
        }
    }

    //일정 게시글 접근 권한 조회 및 UI 설정 함수
    fun settingScheduleAuthAccess(scheduleId : Long){
        viewModelScope.launch {
            resultResponse(
                response = getAuthAccessUseCase(ResourceType.SCHEDULE, scheduleId),
                successCallback = { authAccess ->
                    //삭제나 작성 권한이 있으면 isAuthor로 취급
                    val isAuthor = authAccess.permissions.any { item ->
                        (item.type == PermissionType.DELETE || item.type == PermissionType.EDIT)
                                && item.hasPermission
                    }

                    updateState {
                        copy(isAuthor = isAuthor)
                    }

                },
                errorCallback = {},
            )

        }
    }

    //PlanDetailItem에서 UI에 맞게 데이터를 조절하는 함수
    fun convertPlanDetailItemToUiState(item: PlanDetailItem, plusDay: Int) {
        val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
        val startDate = LocalDate.parse(item.startDay, formatter)
        val today = LocalDate.now()
        val dDay = ChronoUnit.DAYS.between(today, startDate).toInt()


        val finalDDayValue = dDay + plusDay //시작 시간과 진행 상황 합치기

        val dDayString: String //D-몇일 포맷
        val isTodayCheck: Boolean //금일 인가? -> 버튼 생성

        when {
            finalDDayValue == 0 -> {
                dDayString = "D-DAY"
                //여기서 startTime과 endTime 사이에 현 시간이 있는지 판단
                isTodayCheck = checkTodayTime(item)
            }
            finalDDayValue > 31 -> {
                dDayString = "참여 예정"
                isTodayCheck = false
            }
            finalDDayValue > 0 -> {
                dDayString = "D-$finalDDayValue"
                isTodayCheck = false
            }
            else -> { // 음수 (이미 종료된 날짜)
                dDayString = "종료된 일정"
                isTodayCheck = false
            }
        }

        val todayDateString = calculateTargetDate(item.startDay, plusDay)
        val todayTime = if (item.isAllDay) {
            "00:00-23:59"
        } else {
            "${item.startTime}-${item.endTime}"
        }


        updateState {
            copy(
                content = item,
                plusDay = plusDay,
                isToday = isTodayCheck,
                dDay = dDayString,
                title = item.name,
                startDate = item.startDay, // "2026.02.05"
                todayDate = todayDateString, // "2026.02.07"
                todayTime = todayTime, // "05:24-05:24"
                place = item.locationName,
                detail = item.description,
                longitude = item.longitude,
                latitude = item.latitude,
            )
        }

    }

    //D-Day일 때 시간 체크 함수
    private fun checkTodayTime(item: PlanDetailItem): Boolean{
        if(item.isAllDay){
            return true
        }

        //시간 get
        val nowTime = LocalTime.now()
        val formatter = DateTimeFormatter.ofPattern("HH:mm")

        val start = LocalTime.parse(item.startTime, formatter)
        val end = LocalTime.parse(item.endTime, formatter)

        //종료 시간 이전인지만 확인
        val isTimeInRange = !nowTime.isAfter(end)

        return isTimeInRange
    }

    //일정 계산하는 포맷 함수
    private fun calculateTargetDate(startDay: String, plusDay: Int): String {
        return try {
            val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
            val startDate = LocalDate.parse(startDay, formatter)
            startDate.plusDays(plusDay.toLong()).format(formatter)
        } catch (e: Exception) {
            startDay
        }
    }


    //출석 체크 로직
    fun onClickConfirmAttention(){
        emitEvent(ScheduleDetailEvent.TouchConfirmAttention)
    }

    //상단 케밥 메뉴 열기
    fun toggleKebabMenu(){
        updateState { copy(isMenuVisible = !isMenuVisible) }
    }



    //수정 로직 수행
    fun editPlan(){
        updateState { copy(isMenuVisible = false) }
        emitEvent(ScheduleDetailEvent.EditPlan)
    }

    //삭제 로직 확인 다이얼로그 생성 로직 수행
    fun checkDeletePlan(){
        updateState { copy(isMenuVisible = false) }
        emitEvent(ScheduleDetailEvent.CheckDeletePlan)
    }

    //삭제 로직 수행
    fun deletePlan(){
        viewModelScope.launch {
            val scheduleId = uiState.value.content.scheduleId
            resultResponse(
                response = deleteScheduleUseCase(scheduleId),
                successCallback = {
                    emitEvent(ScheduleDetailEvent.MoveBackPressedEvent)
                },
                errorCallback = {

                }
            )
        }
    }


    //뒤로 가기
    fun onClickBackPressed(){
        emitEvent(ScheduleDetailEvent.MoveBackPressedEvent)
    }




}

data class ScheduleDetailUiState(
    //일정 관련
    val content : PlanDetailItem = PlanDetailItem(),
    val plusDay : Int = 0,

    val isToday : Boolean = false, //출석 체크 버튼 visible 유무
    val dDay : String = "참여 예정", // or D-DAY or D-몇일
    val title : String = "정기 세션 3주차",
    val startDate : String = "",
    val todayDate : String = "",
    val todayTime : String = "",
    val place : String = "",
    val detail : String = "",
    val longitude : Double = 0.0,
    val latitude : Double = 0.0,


    //내가 작성한 것인지 여부
    val isAuthor: Boolean = false,

    //케밥 메뉴 아이콘 보이기 여부
    val isMenuVisible : Boolean = false,


    ) : UiState

sealed interface ScheduleDetailEvent : UiEvent {

    object MoveBackPressedEvent : ScheduleDetailEvent

    //토글 이벤트
    object ToggleMenu : ScheduleDetailEvent
    //신고하기 이벤트
    //object ReportPlan : PlanDetailFragmentEvent
    //수정하기 이벤트
    object EditPlan : ScheduleDetailEvent
    //삭제하기 이벤트 (다이얼로그로 확인)
    object CheckDeletePlan : ScheduleDetailEvent


    //출석 체크 이벤트
    object TouchConfirmAttention : ScheduleDetailEvent


}