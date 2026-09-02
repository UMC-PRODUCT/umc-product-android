package com.umc.presentation.home.schedule.detail

import android.util.Log
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
import com.umc.domain.usecase.schedule.GetScheduleCapabilities
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
    private val savedStateHandle: SavedStateHandle, //nav에서 인자로 넘긴 일정 id 체크용
    private val getScheduleDetailHomeUseCase: GetScheduleDetailHomeUseCase, //일정 상세 정보 가져오기
    private val deleteScheduleUseCase: DeleteScheduleUseCase, //일정 삭제하기
    private val getAuthAccessUseCase: GetAuthAccessUseCase, //리소스 권한 조회
) : BaseViewModel<ScheduleDetailUiState, ScheduleDetailEvent>(
    ScheduleDetailUiState()){

    // SavedStateHandle에서 전달받은 일정 ID 및 연속 일정 일수 오프셋 변수
    /** [예시]
     *  일정이 2026.01.01 ~ 2026.01.17 일 경우,
     *  2026.01.03에 있는 일정을 터치할 경우, 2026.01.01 일정 ID에 +2 day를 포함한 정보가 전송
     * **/
    private val checkScheduleId: Long = savedStateHandle.get<Long>("scheduleId") ?: -1L
    private val checkPlusDay: Int = savedStateHandle.get<Int>("plusDay") ?: -1


    init{
        // 전달받은 인자값이 유효한 경우 상세 데이터를 로드
        if(checkScheduleId != -1L && checkPlusDay != -1) {
            getScheduleDetail(checkScheduleId, checkPlusDay)
        }

        // 일정에 대한 수정 및 삭제 권한 조회
        checkScheduleCapabilities()
    }



    /**
     * SavedStateHandle에 저장된 기본 ID값으로 일정 상세 정보를 조회 및 로드하는 메서드
     */
    fun getScheduleDetail(){
        viewModelScope.launch {
            resultResponse(
                response = getScheduleDetailHomeUseCase(checkScheduleId),
                successCallback = {
                    updateState { copy(
                        content = it,
                        plusDay = plusDay)
                    }

                    // 서버에서 받은 데이터를 UI 상태에 맞게 변환 및 D-Day 계산
                    convertPlanDetailItemToUiState(it, checkPlusDay)
                },
                errorCallback = {

                }
            )
        }
    }


    /**
     * 특정 일정 ID와 오프셋 일수를 직접 전달받아 상세 정보를 서버에서 조회하는 메서드
     *
     * @param scheduleId 일정 고유 ID
     * @param plusDay 시작일 기준 오프셋 일수
     */
    fun getScheduleDetail(scheduleId : Long, plusDay: Int){
        viewModelScope.launch {
            resultResponse(
                response = getScheduleDetailHomeUseCase(scheduleId),
                successCallback = {
                    updateState { copy(
                        content = it,
                        plusDay = plusDay)
                    }

                    // 서버에서 받은 데이터를 UI 상태에 맞게 변환 및 D-Day 계산
                    convertPlanDetailItemToUiState(it, plusDay)
                },
                errorCallback = {

                }
            )
        }
    }



    /**
     * 유저의 일정 수정 및 삭제 권한을 조회하고 UI 케밥 메뉴 항목 노출 상태를 변경하는 메서드
     */
    fun checkScheduleCapabilities(){
        viewModelScope.launch {
            resultResponse(
                response = getAuthAccessUseCase(ResourceType.SCHEDULE, checkScheduleId),
                successCallback = { accessInfo ->

                    var checkEdit = false
                    var checkDelete = false
                    for(item in accessInfo.permissions){
                        if(item.type == PermissionType.EDIT){
                            checkEdit = item.hasPermission
                        }
                        if(item.type == PermissionType.DELETE){
                            checkDelete = item.hasPermission
                        }
                    }

                    updateState {
                        copy(
                            canEdit = checkEdit,
                            canDelete = checkDelete
                        )
                    }
                },
                errorCallback = {

                }
            )
        }
    }


    /**
    * 서버에서 수신받은 상세 도메인 모델(PlanDetailItem)을 UI 상태에 맞춰 변환하고 D-Day를 계산하는 메서드
    *
    * @param item 일정 상세 정보 도메인 모델
    * @param plusDay 시작일 기준 경과 일수 오프셋
    */
    fun convertPlanDetailItemToUiState(item: PlanDetailItem, plusDay: Int) {
        val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
        val startDate = LocalDate.parse(item.startDay, formatter)
        val today = LocalDate.now()
        val dDay = ChronoUnit.DAYS.between(today, startDate).toInt()

        // 시작일 기준 D-Day에 경과 일수를 보정하여 최종 D-Day 연산
        val finalDDayValue = dDay + plusDay //시작 시간과 진행 상황 합치기

        val dDayString: String //D-몇일 포맷
        val isTodayCheck: Boolean //금일 인가? -> 버튼 생성

        when {
            finalDDayValue == 0 -> {
                dDayString = "D-DAY"
                // 오늘 날짜인 경우 현재 시간이 일정 종료 시각 이전인지 확인하여 출석 버튼 활성화
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
                isonline = item.isOnline
            )
        }

    }

    /**
     * 오늘 날짜의 일정일 때 현재 시각이 일정 종료 시각 이전인지 검증하는 메서드
     *
     * @param item 일정 상세 도메인 객체
     * @return 하루종일이거나 현재 시간이 종료 시간 이전이면 true를 반환
     */
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

    /**
     * 일정 시작일과 plusDay 오프셋을 더해 실제 진행 일자(yyyy.MM.dd)를 연산하는 메서드
     */
    private fun calculateTargetDate(startDay: String, plusDay: Int): String {
        return try {
            val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
            val startDate = LocalDate.parse(startDay, formatter)
            startDate.plusDays(plusDay.toLong()).format(formatter)
        } catch (e: Exception) {
            startDay
        }
    }



    /**
     * 상단 우상단 케밥(수정/삭제) 팝업 메뉴 노출 상태를 토글하는 메서드
     */
    fun toggleKebabMenu(){
        updateState { copy(isMenuVisible = !isMenuVisible) }
    }



    /**
     * 일정 수정 화면 이동 이벤트를 발행하는 메서드
     */
    fun editPlan(){
        updateState { copy(isMenuVisible = false) }
        emitEvent(ScheduleDetailEvent.EditPlan)
    }

    /**
     * 일정 삭제 확인 다이얼로그 노출 이벤트를 발행하는 메서드
     */
    fun checkDeletePlan(){
        updateState { copy(isMenuVisible = false) }
        emitEvent(ScheduleDetailEvent.CheckDeletePlan)
    }

    /**
     * 서버에 일정 삭제를 요청하고 성공 시 뒤로가기 이벤트를 발생시키는 메서드
     */
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

}

data class ScheduleDetailUiState(
    //일정 관련
    val content : PlanDetailItem = PlanDetailItem(),
    val plusDay : Int = 0,

    val isonline : Boolean = false,
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


    //수정 및 삭제 권한 표출 상태
    val canEdit: Boolean = false,
    val canDelete: Boolean = false,

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