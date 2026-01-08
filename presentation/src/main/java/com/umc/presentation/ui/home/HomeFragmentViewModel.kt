package com.umc.presentation.ui.home

import com.prolificinteractive.materialcalendarview.CalendarDay
import com.umc.domain.model.home.SchedulePlan
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import javax.inject.Inject

//뷰모델에서는 xml에서 이벤트 의도 전달 -> fragment한테 이거 로직 처리하삼
class HomeFragmentViewModel
    @Inject
    constructor() : BaseViewModel<HomeFragmentUiState, HomeFragmentEvent>(
            HomeFragmentUiState()){


        //테스트용 데이터
        private val dummySchedules = listOf(
            SchedulePlan( "중앙 해커톤", "10:00", "2026-01-27", "TUE", "27", "D-19", false),
            SchedulePlan( "아이디어톤", "14:00", "2026-01-08", "WED", "08", "", true),
            SchedulePlan( "기획 파트 회의", "19:00", "2026-01-27", "TUE", "27", "D-19", false),
            SchedulePlan( "데모 데이", "13:00", "2026-02-15", "SUN", "15", "D-38", false)
        )
        init{
            updateState {
                copy(
                    allPlans = dummySchedules,
                    // 초기 실행 시 오늘 날짜(27일 가정)로 필터링된 리스트를 보여줌
                    dailyPlans = dummySchedules.filter { it.date == "2026-01-27" }
                )
            }
        }


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

        // 공시사항 (운영바침) 이동 --> 얘를 보내면 Fragment에서 수신
        fun onClickNotice(){
            emitEvent(HomeFragmentEvent.MoveNoticeEvent)
        }

        // 알림 이동
        fun onClickNotification(){
            emitEvent(HomeFragmentEvent.MoveNotificationEvent)
        }

        // 일정 추가 이동
        fun onClickPlanAdd(){
            emitEvent(HomeFragmentEvent.MovePlanAddEvent)
        }
    
        // 일정 상세 이동
        fun onClickPlanDetail(){
            emitEvent(HomeFragmentEvent.MovePlanDetailEvent)
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

    //알람 존재 관련
    val alarmExist: Boolean = false,

    //임시 데이터
    val dailyPlans: List<SchedulePlan> = emptyList(),
    val allPlans: List<SchedulePlan> = emptyList()

/*


    // 3. 필터링된 일정 데이터
    val schedules: List<Schedule> = emptyList(),
    */
) : UiState

sealed class HomeFragmentEvent : UiEvent {
    object MoveNoticeEvent : HomeFragmentEvent() //공시사항 이동
    object MoveNotificationEvent : HomeFragmentEvent() //알림 이동
    object MovePlanDetailEvent : HomeFragmentEvent() //일정 상세 이동
    object MovePlanAddEvent : HomeFragmentEvent() //일정 추가 이동
}

enum class ViewMode { CALENDAR, LIST }
enum class UserType {ACTIVE, OB}
enum class WarningStatus {NORMAL, WARNING, DANGER}
