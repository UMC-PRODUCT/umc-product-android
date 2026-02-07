package com.umc.presentation.ui.home

import androidx.lifecycle.viewModelScope
import com.prolificinteractive.materialcalendarview.CalendarDay

import com.umc.domain.model.enums.HomeViewMode
import com.umc.domain.model.enums.UserType
import com.umc.domain.model.enums.WarningStatus
import com.umc.domain.model.home.SchedulePlanItem
import com.umc.domain.model.home.schedule.ScheduleMonthModel

import com.umc.domain.usecase.schedule.GetScheduleMonthUseCase

import com.umc.domain.usecase.member.GetMyProfileUseCase
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

//뷰모델에서는 xml에서 이벤트 의도 전달 -> fragment한테 이거 로직 처리하삼
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getMyProfileUseCase: GetMyProfileUseCase, //내 프로필 정보 가져오기
    private val getScheduleMonthUseCase: GetScheduleMonthUseCase, //월별 일정 가져오기
) : BaseViewModel<HomeFragmentUiState, HomeFragmentEvent>(
            HomeFragmentUiState()) {


    init {
        val today = CalendarDay.today()

        //1. 초기 상태 설정(오늘 날짜로)
        updateState { copy(selectedDate = today) }

        //2. 유저 정보 가져오기
        getUserInfo()

        //3. 금일(월) 데이터 가져오기
        getScheduleMonth(today.year, today.month)

    }

    //날짜 문자열 변환 유틸 함수
    private fun formatDate(year: Int, month: Int, day: Int): String {
        return String.format("%d.%02d.%02d", year, month, day)
    }

    // 달력에서 선택한 날짜 + 오늘 일정들로 업데이트하는 함수(Fragment -> ViewModel)
    fun setSelectedDate(date: CalendarDay) {
        // updateState를 통해 UiState의 selectedDate 변경
        val dateString = formatDate(date.year, date.month, date.day)

        updateState {
            copy(
                selectedDate = date,
                dailyPlans = allPlans.filter { it.date == dateString } //dailyPlans에 필터링
            )

        }
    }

    // 일정이 있는 날짜들을 추가(Decorator 용)
    fun loadEvents() {
        val dates = uiState.value.allPlans.map {
            val parts = it.date.split(".")
            CalendarDay.from(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
        }.toSet()

        updateState { copy(eventDates = dates) }
    }

    // 서버에서 내 정보 가져오기
    fun getUserInfo() {
        viewModelScope.launch {
            resultResponse(
                response = getMyProfileUseCase(),
                successCallback = { userInfo ->

                    updateState {
                        copy(
                            userName = userInfo.name,
                            userType = UserType.valueOf(userInfo.status),
                        )
                    }

                },
                errorCallback = {
                    /**TODO. 에러 토스트 메시지 등을 전송**/
                }
            )
        }
    }

    //월별 정보 받아오기
    fun getScheduleMonth(year: Int, month: Int){
        viewModelScope.launch {
            resultResponse(
                response = getScheduleMonthUseCase(year, month),
                successCallback = { scheduleMonth ->
                    //1. 서버 데이터를 SchedulePlanItem으로 변환
                    val planItems = convertToPlanItems(scheduleMonth)
                    val todayString = formatDate(uiState.value.selectedDate.year,
                        uiState.value.selectedDate.month,
                        uiState.value.selectedDate.day)

                    //2. 데이터 월별 최신화
                    updateState {
                        copy(
                            allPlans = planItems,
                            dailyPlans = planItems.filter {
                                it.date == todayString
                            }
                        )
                    }
                    // 3. 점 찍기(Decorator) 데이터 갱신
                    loadEvents()
                },
                errorCallback = {
                    /**TODO. 에러 토스트 메시지 등을 전송**/

                }
            )
        }
    }

    //ScheduleMonthModel -> SchdulePlanItem(보여주기 형식)으로 바꾸는 함수
    //또한, 연속된 내용의 일정(02.06-0.08)에 대해, 동일한 일정 item을 생성 (id는 same)
    private fun convertToPlanItems(domainModels: List<ScheduleMonthModel>): List<SchedulePlanItem> {
        val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
        val result = mutableListOf<SchedulePlanItem>()

        domainModels.forEach { schedule ->
            val startDate = LocalDate.parse(schedule.startDay, formatter)
            val endDate = LocalDate.parse(schedule.endDay, formatter)
            //총 며칠 있는지 계산 (2026.02.07 - 2026.02.09)일 경우 7 8 9에 대해 일정 생성 필요
            val daysBetween = ChronoUnit.DAYS.between(startDate, endDate).toInt()

            //서버에서 준 dDay 값을 숫자로 변환
            val serverDDay = schedule.dDay
            

            for (i in 0..daysBetween) {
                val targetDate = startDate.plusDays(i.toLong())
                val isPast = serverDDay + i < 0 //날짜 추가에 따른 D-Day 변경

                result.add(
                    SchedulePlanItem(
                        id = schedule.scheduleId,
                        title = schedule.name,
                        time = schedule.startTime,
                        date = targetDate.format(formatter),
                        dayOfWeek = targetDate.format(DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH)).uppercase(),
                        day = targetDate.dayOfMonth.toString().padStart(2, '0'),
                        // UI 표시용 dDay 설정
                        dDay = when {
                            isPast -> null                  // 과거면 null
                            serverDDay+i == 0 -> "D-Day"   // 0이면 D-Day
                            serverDDay+i > 31 -> "참여 예정" // 31일보다 크면 참여 예정으로
                            else -> "D-${serverDDay+i}"      // 양수면 D-n
                        },
                        isPast = isPast
                    )
                )
            }
        }
        return result
    }

    // 뷰모드(달력/리스트) 전환 함수
    fun onChangeViewMode(mode: HomeViewMode) {
        updateState {
            copy(viewMode = mode)
        }
    }

    // 달력 상단 열기
    fun onClickCalendarHeader() {
        emitEvent(HomeFragmentEvent.OpenDatePickerEvent)
    }

    // 공시사항 (운영바침) 이동 --> 얘를 보내면 Fragment에서 수신
    fun onClickNotice() {
        emitEvent(HomeFragmentEvent.MoveNoticeEvent)
    }

    // 알림 이동
    fun onClickNotification() {
        emitEvent(HomeFragmentEvent.MoveNotificationEvent)
    }

    // 일정 추가 이동
    fun onClickPlanAdd() {
        emitEvent(HomeFragmentEvent.MovePlanAddEvent)
    }

    // 일정 상세 이동
    fun onClickPlanDetail(plan: SchedulePlanItem) {
        emitEvent(HomeFragmentEvent.MovePlanDetailEvent(plan))
    }


}

data class HomeFragmentUiState(
    // 달력 관련
    val selectedDate: CalendarDay = CalendarDay.today(), // 선택한 날짜
    val eventDates: Set<CalendarDay> = emptySet(), // 일정 있는 날짜들

    // 달력 <-> 일정 전환
    val viewMode: HomeViewMode = HomeViewMode.CALENDAR,

    // 유저 정보 영역
    val userName: String = "홍길동",
    val growDay: Int = 731,

    val userType: UserType = UserType.ACTIVE,

    val warningStatus: WarningStatus = WarningStatus.WARNING,

    //알람 존재 관련
    val alarmExist: Boolean = false,

    //상태에 따른 텍스트
    val warningStatusText: String
        = when(warningStatus){
            WarningStatus.DANGER -> "경고가 2회 누적되었습니다."
            WarningStatus.WARNING -> "경고가 1회 누적되었습니다."
            WarningStatus.NORMAL -> "누적된 경고가 없습니다."
        },



    //일정 관련
    val dailyPlans: List<SchedulePlanItem> = emptyList(), //선택한 날들의 일정
    val allPlans: List<SchedulePlanItem> = listOf(
    ), //월별 모든 일정
    
    val tmptag: List<String> = listOf("11기", "12기", "13기")



) : UiState


sealed interface HomeFragmentEvent : UiEvent {
    object MoveNoticeEvent : HomeFragmentEvent //공시사항 이동
    object MoveNotificationEvent : HomeFragmentEvent //알림 이동
    data class MovePlanDetailEvent(val plan: SchedulePlanItem) : HomeFragmentEvent //일정 상세 이동
    object MovePlanAddEvent : HomeFragmentEvent //일정 추가 이동

    object OpenDatePickerEvent : HomeFragmentEvent //날짜 선택 다이얼로그 열기

}

