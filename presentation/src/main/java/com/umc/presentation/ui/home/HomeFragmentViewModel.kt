package com.umc.presentation.ui.home

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

    // 뷰모드(달력/리스트) 전환 함수
    fun onChangeViewMode(mode: ViewMode) {
        updateState {
            copy(viewMode = mode)
        }
    }
    }

data class HomeFragmentUiState(
    // 달력 관련
    val selectedDate: CalendarDay = CalendarDay.today(), // 선택한 날짜
    val eventDates: Set<CalendarDay> = emptySet(), // 일정 있는 날짜들

    // 달력 <-> 일정 전환
    val viewMode: ViewMode = ViewMode.CALENDAR,
    // 유저 정보 영역
    val userType: UserType = UserType.ACTIVE,
    //val profileInfo: ProfileInfo? = null,
    val warningStatus: WarningStatus = WarningStatus.NORMAL,

/*


    // 3. 필터링된 일정 데이터
    val schedules: List<Schedule> = emptyList(),
    */
) : UiState

sealed class HomeFragmentEvent : UiEvent {
    object DummyEvent : HomeFragmentEvent()
}

enum class ViewMode { CALENDAR, LIST }
enum class UserType {ACTIVE, OB}
enum class WarningStatus {NORMAL, WARNING, CRITICAL}