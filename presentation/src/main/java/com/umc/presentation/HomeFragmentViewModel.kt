package com.umc.presentation

import com.prolificinteractive.materialcalendarview.CalendarDay
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import javax.inject.Inject

class HomeFragmentViewModel
    @Inject
    constructor() : BaseViewModel<HomeFragmentUiState, HomeFragmentEvent>(
            HomeFragmentUiState()){

    // 날짜 정보를 업데이트 함수
    fun setSelectedDate(date: CalendarDay) {
        // updateState를 통해 UiState의 selectedDate 변경
        updateState {
            copy(selectedDate = date)
        }
    }

    }

data class HomeFragmentUiState(
    val dummy: String = "",
    // 달력 관련
    val selectedDate: CalendarDay = CalendarDay.today(), // 선택한 날짜
    val eventDates: Set<CalendarDay> = emptySet(), // 일정 있는 날짜들

    /*
    // 1. 유저 정보 영역
    val userType: UserType = UserType.ACTIVE,
    val profileInfo: ProfileInfo? = null,
    val warningStatus: WarningStatus = WarningStatus.NORMAL,

    // 2. 일정 보기 모드
    val viewMode: HomeViewMode = HomeViewMode.CALENDAR,
    val selectedDate: LocalDate = LocalDate.now(),

    // 3. 필터링된 일정 데이터
    val schedules: List<Schedule> = emptyList(),

    // 4. 로딩 및 에러 상태
    val isLoading: Boolean = false
    */
) : UiState

sealed class HomeFragmentEvent : UiEvent {
    object DummyEvent : HomeFragmentEvent()
}
