package com.umc.presentation.ui.home

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.umc.domain.model.home.PlanDetailItem
import com.umc.domain.usecase.schedule.GetScheduleDetailHomeUseCase
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject


@HiltViewModel
class PlanDetailViewModel @Inject
constructor(
    private val getScheduleDetailHomeUseCase: GetScheduleDetailHomeUseCase,
    ) : BaseViewModel<PlanDetailFragmentUiState, PlanDetailFragmentEvent>(
    PlanDetailFragmentUiState()){




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

                        convertPlanDetailItemToUiState(it, plusDay)
                    },
                    errorCallback = {

                    }
                )
            }
        }

        //PlanDetailItem에서 UI에 맞게 데이터를 조절하는 함수
        fun convertPlanDetailItemToUiState(item: PlanDetailItem, plusDay: Int) {
            val finalDDayValue = item.dDay + plusDay //시작 시간과 진행 상황 합치기

            val dDayString: String //D-몇일 포맷
            val isTodayCheck: Boolean //금일 인가? -> 버튼 생성

            when {
                finalDDayValue == 0 -> {
                    dDayString = "D-DAY"
                    isTodayCheck = true
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
            emitEvent(PlanDetailFragmentEvent.TouchConfirmAttention)
        }

        //상단 케밥 메뉴 열기
        fun toggleKebabMenu(){
            updateState { copy(isMenuVisible = !isMenuVisible) }
        }

        //신고 로직 수행
        fun reportPlan(){
            updateState { copy(isMenuVisible = false) }
            emitEvent(PlanDetailFragmentEvent.ReportPlan)
        }

        //수정 로직 수행
        fun editPlan(){
            updateState { copy(isMenuVisible = false) }
            emitEvent(PlanDetailFragmentEvent.EditPlan)
        }

        //삭제 로직 수행
        fun deletePlan(){
            updateState { copy(isMenuVisible = false) }
            emitEvent(PlanDetailFragmentEvent.DeletePlan)
        }

        //뒤로 가기
        fun onClickBackPressed(){
            emitEvent(PlanDetailFragmentEvent.MoveBackPressedEvent)
        }




}

data class PlanDetailFragmentUiState(
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
    val isAuthor: Boolean = true,

    //케밥 메뉴 아이콘 보이기 여부
    val isMenuVisible : Boolean = false,


) : UiState

sealed interface PlanDetailFragmentEvent : UiEvent {

    object MoveBackPressedEvent : PlanDetailFragmentEvent

    //토글 이벤트
    object ToggleMenu : PlanDetailFragmentEvent
    //신고하기 이벤트
    object ReportPlan : PlanDetailFragmentEvent
    //수정하기 이벤트
    object EditPlan : PlanDetailFragmentEvent
    //삭제하기 이벤트
    object DeletePlan : PlanDetailFragmentEvent


    //출석 체크 이벤트
    object TouchConfirmAttention : PlanDetailFragmentEvent


}