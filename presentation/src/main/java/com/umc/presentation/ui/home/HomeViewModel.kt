package com.umc.presentation.ui.home

import android.R
import android.text.Spanned
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.enums.HomeViewMode
import com.umc.domain.model.enums.UserType
import com.umc.domain.model.enums.WarningStatus
import com.umc.domain.model.home.SchedulePlanItem
import com.umc.domain.usecase.appDataStore.GetUserInfoUseCase
import com.umc.domain.usecase.appDataStore.UpdateUserInfoUseCase
import com.umc.domain.usecase.member.GetMemberProfileUseCase
import com.umc.domain.usecase.member.GetMyProfileUseCase
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

//뷰모델에서는 xml에서 이벤트 의도 전달 -> fragment한테 이거 로직 처리하삼
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getMyProfileUseCase: GetMyProfileUseCase,
) : BaseViewModel<HomeFragmentUiState, HomeFragmentEvent>(
            HomeFragmentUiState()){


        init{
            //달력 초기화
            initCalendar()

            //유저 정보 가져오기
            getUserInfo()



        }


        // 날짜 정보를 업데이트 함수
        fun setSelectedDate(date: CalendarDay) {
            // updateState를 통해 UiState의 selectedDate 변경
            val dateString = String.format("%d-%02d-%02d", date.year, date.month, date.day)

            updateState {
                copy(selectedDate = date,
                    dailyPlans = allPlans.filter { it.date == dateString } //dailyPlans에 필터리
                    )

            }
        }

        // 초기 데이터 로딩 = 일정이 있는 날짜들을 추가하자.
        fun loadEvents(){
            val dates = uiState.value.allPlans.map {
                val parts = it.date.split("-")
                CalendarDay.from(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
            }.toSet()

            updateState { copy(eventDates = dates) }
        }

        // 초기 달력 초기화
        fun initCalendar(){
            //시작 시 오늘 날짜로
            val today = CalendarDay.today()
            val todayString = String.format("%d-%02d-%02d", today.year, today.month, today.day)

            updateState {
                copy(
                    //임시로 tmp 넣기
                    //allPlans = tmpSchedules,
                    // 초기 실행 시 오늘 날짜(27일 가정)로 필터링된 리스트를 보여줌
                    dailyPlans = allPlans.filter { it.date == todayString }
                )
            }
            //날짜들 가져오기 (eventDecorator)
            loadEvents()
        }

        // 뷰모드(달력/리스트) 전환 함수
        fun onChangeViewMode(mode: HomeViewMode) {
            updateState {
                copy(viewMode = mode)
            }
        }

        // 달력 상단 열기
        fun onClickCalendarHeader(){
            emitEvent(HomeFragmentEvent.OpenDatePickerEvent)
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
        fun onClickPlanDetail(plan : SchedulePlanItem){
            emitEvent(HomeFragmentEvent.MovePlanDetailEvent(plan))
        }



        // 서버에서 내 정보 가져오기
        private fun getUserInfo(){
            viewModelScope.launch {
                val result = getMyProfileUseCase()
                when(result){
                    is ApiState.Success -> {
                        Log.d("log_home", "getUserInfo: ${result.data}")
                    }
                    is ApiState.Fail -> {
                        /**TODO. 에러 토스트 메시지 등을 전송**/
                    }
                }
            }
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
        SchedulePlanItem( "중앙 해커톤", "10:00", "2026-01-20", "TUE", "27", "D-19", false),
        SchedulePlanItem( "아이디어톤", "14:00", "2026-01-08", "WED", "08", "", true),
        SchedulePlanItem( "기획 파트 회의", "19:00", "2026-01-20", "TUE", "27", "D-19", false),
        SchedulePlanItem( "데모 데이", "13:00", "2026-01-30", "SUN", "15", "D-38", false),
        SchedulePlanItem( "데모 데이", "13:00", "2026-01-29", "SUN", "15", "D-38", false),
        SchedulePlanItem( "데모 데이", "13:00", "2026-01-31", "SUN", "15", "D-38", false),
        SchedulePlanItem( "데모 데이", "13:00", "2026-01-08", "SUN", "15", "D-38", false),
        SchedulePlanItem( "데모 데이", "13:00", "2026-02-08", "SUN", "15", "D-38", false),
        SchedulePlanItem( "데모 데이", "13:00", "2026-02-08", "SUN", "15", "D-38", false),
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

