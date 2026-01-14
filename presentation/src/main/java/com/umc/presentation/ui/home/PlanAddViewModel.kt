package com.umc.presentation.ui.home

import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject


@HiltViewModel
class PlanAddViewModel @Inject
constructor() : BaseViewModel<PlanAddFragmentUiState, PlanAddFragmentEvent>(
    PlanAddFragmentUiState()){

    private val dateSdf = SimpleDateFormat("yyyy.MM.dd", Locale.KOREAN)
    private val timeSdf = SimpleDateFormat("a h:mm", Locale.KOREAN)


    //날짜 조정 (얘는 Fragment에서 날짜 받아온 후 수행하는 이벤트입니다)
    fun processDateTime(event: PlanAddFragmentEvent){
        when(event){
            is PlanAddFragmentEvent.UpdateStartDate -> {
                val newCalendar = (uiState.value.startDate.clone() as Calendar).apply {
                    set(event.year, event.month, event.day)
                }
                updateState {
                    copy(startDate = newCalendar,
                        startDateText = dateSdf.format(newCalendar.time)
                    )
                }
            }
            is PlanAddFragmentEvent.UpdateStartTime -> {
                val newCalendar = (uiState.value.startTime.clone() as Calendar).apply {
                    set(Calendar.HOUR_OF_DAY, event.hour)
                    set(Calendar.MINUTE, event.minute)
                }
                updateState {
                    copy(startTime = newCalendar,
                        startTimeText = timeSdf.format(newCalendar.time)
                    )

                }
            }
            is PlanAddFragmentEvent.UpdateEndDate -> {
                val newCalendar = (uiState.value.startDate.clone() as Calendar).apply {
                    set(event.year, event.month, event.day)
                }
                updateState {
                    copy(endDate = newCalendar,
                        endDateText = dateSdf.format(newCalendar.time)
                    )

                }
            }
            is PlanAddFragmentEvent.UpdateEndTime -> {
                val newCalendar = (uiState.value.startTime.clone() as Calendar).apply {
                    set(Calendar.HOUR_OF_DAY, event.hour)
                    set(Calendar.MINUTE, event.minute)
                }
                updateState {
                    copy(endTime = newCalendar,
                        endTimeText = timeSdf.format(newCalendar.time)
                    )
                }
            }

            else -> {}
        }
    }

    //값 업데이트
    fun processParticipants(event: PlanAddFragmentEvent) {
        when(event){
            is PlanAddFragmentEvent.UpdateParticipants -> {
                updateState {
                    copy(selectedParticipants = event.participants)
                }
            }

            else -> {}
        }

    }


}


data class PlanAddFragmentUiState(
    val dummyData: String = "",

    //하루 종일 부분에 체크가 되었나
    val isAllDay: Boolean = false,

    //일정 관련
    val planTitle: String = "",
    val planLocation: String = "",
    val planDetail: String = "",

    //시간 관련
    val startDate: Calendar = Calendar.getInstance(),
    val startTime: Calendar = Calendar.getInstance(),
    val endDate: Calendar = Calendar.getInstance(),
    val endTime: Calendar = Calendar.getInstance(),

    val startDateText: String = "시작 날짜",
    val startTimeText: String = "시작 시간",
    val endDateText : String = "종료 날짜",
    val endTimeText : String = "종료 시간",


    //인원 검색 관련
    /**TODO 일단은 이름만 받는다고 가정**/
    val selectedParticipants: List<String> = emptyList(), //선택 결과(recyclerview)
    val searchQuery: String = "",
    val searchResults: List<String> = emptyList(), //검색 결과
    val isSearchOverlayVisible: Boolean = false, //검색 결과창 보여주기

    val isRegisterOk: Boolean = false, //모든 조건을 만족해 업로드 가능한지
    
    ) : UiState

sealed interface PlanAddFragmentEvent : UiEvent {

    //TIME 및 DATE Picker로 값을 가져오는 이벤트
    data class UpdateStartDate(val year: Int, val month: Int, val day: Int) : PlanAddFragmentEvent
    data class UpdateStartTime(val hour: Int, val minute: Int) : PlanAddFragmentEvent
    data class UpdateEndDate(val year: Int, val month: Int, val day: Int) : PlanAddFragmentEvent
    data class UpdateEndTime(val hour: Int, val minute: Int) : PlanAddFragmentEvent

    //CSV 파일 파싱 결과를 가져오느 이벤트
    data class UpdateParticipants(val participants: List<String>) : PlanAddFragmentEvent



}