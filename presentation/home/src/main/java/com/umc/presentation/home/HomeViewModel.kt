package com.umc.presentation.home

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    //private val getMyProfileUseCase: GetMyProfileUseCase, //내 프로필 정보 가져오기
    //private val getScheduleMonthUseCase: GetScheduleMonthUseCase, //월별 일정 가져오기
    //private val getGisuInfoUseCase: GetGisuInfoUseCase, //기수 정보 가져오기
    //private val getGisuListUseCase: GetGisuListUseCase, //전체 기수 리스트 가져오기
) //: BaseViewModel<HomeFragmentUiState, HomeFragmentEvent>(
    //HomeFragmentUiState())
{

}

/*
data class HomeUiState(
    // 달력 관련
    val selectedDate: CalendarDay = CalendarDay.today(), // 선택한 날짜
    val eventDates: Set<CalendarDay> = emptySet(), // 일정 있는 날짜들

    // 달력 <-> 일정 전환
    val viewMode: HomeViewMode = HomeViewMode.CALENDAR,

    // 유저 정보 영역
    val userName: String = "",
    val userNickName: String = "",
    val growDay: Int = 0,
    val gisuTag: List<String> = emptyList(),

    val userType: UserType = UserType.ACTIVE,

    val warningStatus: WarningStatus = WarningStatus.NORMAL,

    //알람 존재 관련
    val alarmExist: Boolean = false,

    //상태에 따른 텍스트

    val activeString: String = "0기 활동 상태",
    val sangjum: Int = 0,
    val buljum: Int = 0,
    val total: Int = 0,

    //일정 관련
    val dailyPlans: List<SchedulePlanItem> = emptyList(), //선택한 날들의 일정
    val allPlans: List<SchedulePlanItem> = listOf(
    ), //월별 모든 일정
    val plusDays : Int = 0, //연속 날짜 처리 용도

) : UiState


sealed interface HomeEvent : UiEvent {
    object MoveNoticeEvent : HomeEvent //공시사항 이동
    object MoveNotificationEvent : HomeEvent //알림 이동
    data class MovePlanDetailEvent(val plan: SchedulePlanItem) : HomeEvent //일정 상세 이동
    object MovePlanAddEvent : HomeEvent //일정 추가 이동

    object OpenDatePickerEvent : HomeEvent //날짜 선택 다이얼로그 열기

}
*/
