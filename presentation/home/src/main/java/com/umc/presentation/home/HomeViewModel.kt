package com.umc.presentation.home

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import com.umc.component.base.BaseViewModel
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.umc.domain.model.enums.HomeViewMode
import com.umc.domain.model.enums.UserType
import com.umc.domain.model.enums.WarningStatus
import com.umc.domain.model.home.SchedulePlanItem
import com.umc.domain.model.home.schedule.ScheduleMonthModel
import com.umc.domain.usecase.GetGisuInfoUseCase
import com.umc.domain.usecase.member.GetMyProfileUseCase
import com.umc.domain.usecase.organization.GetGisuListUseCase
import com.umc.domain.usecase.schedule.GetScheduleMonthUseCase
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getMyProfileUseCase: GetMyProfileUseCase, //내 프로필 정보 가져오기
    private val getScheduleMonthUseCase: GetScheduleMonthUseCase, //월별 일정 가져오기
    private val getGisuInfoUseCase: GetGisuInfoUseCase, //기수 정보 가져오기
    private val getGisuListUseCase: GetGisuListUseCase, //전체 기수 리스트 가져오기
) : BaseViewModel<HomeUiState, HomeEvent>(
    HomeUiState())
{
    init {
        val today = LocalDate.now()

        //유저 정보 가져오기
        getUserInfo()

        //금일(월) 데이터 가져오기 (LocalDate 사용)
        getScheduleMonth(today.year, today.monthValue)

        //전체 기수 리스트 캐시
        loadGisuList()
    }

    //날짜 문자열 변환 유틸 함수 (LocalDate 포맷터 활용)
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")

    private fun formatDate(date: LocalDate): String = date.format(dateFormatter)

    // 달력에서 선택한 날짜 업데이트 (Compose용 LocalDate 수신)
    fun setSelectedDate(date: LocalDate) {
        val dateString = formatDate(date)
        updateState {
            copy(
                selectedDate = date,
                dailyPlans = allPlans.filter { it.date == dateString }
            )
        }
    }

    //일정이 있는 날짜들을 추출하는 함수
    private fun extractEventDates(plans: List<SchedulePlanItem>) {
        val dates = plans.map {
            LocalDate.parse(it.date, dateFormatter)
        }.toSet()

        updateState { copy(eventDates = dates) }
    }

    //월별 정보 받아오기
    fun getScheduleMonth(year: Int, month: Int) {
        viewModelScope.launch {
            resultResponse(
                response = getScheduleMonthUseCase(year, month),
                successCallback = { scheduleMonth ->
                    val planItems = convertToPlanItems(scheduleMonth)
                    val todayString = formatDate(uiState.value.selectedDate)

                    updateState {
                        copy(
                            allPlans = planItems,
                            dailyPlans = planItems.filter { it.date == todayString }
                        )
                    }
                    // 점 찍기 데이터 갱신
                    extractEventDates(planItems)
                }
            )
        }
    }


    // ScheduleMonthModel -> SchedulePlanItem 변환 로직
    private fun convertToPlanItems(domainModels: List<ScheduleMonthModel>): List<SchedulePlanItem> {
        val result = mutableListOf<SchedulePlanItem>()
        val today = LocalDate.now()

        domainModels.forEach { schedule ->
            val startDate = LocalDate.parse(schedule.startDay, dateFormatter)
            val endDate = LocalDate.parse(schedule.endDay, dateFormatter)
            val daysBetween = ChronoUnit.DAYS.between(startDate, endDate).toInt()
            val serverDDay = ChronoUnit.DAYS.between(today, startDate).toInt()

            for (i in 0..daysBetween) {
                val targetDate = startDate.plusDays(i.toLong())
                val isPast = ChronoUnit.DAYS.between(today, targetDate) < 0

                result.add(
                    SchedulePlanItem(
                        id = schedule.scheduleId,
                        title = schedule.name,
                        time = schedule.startTime,
                        date = targetDate.format(dateFormatter),
                        dayOfWeek = targetDate.format(DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH)).uppercase(),
                        day = targetDate.dayOfMonth.toString().padStart(2, '0'),
                        dDay = when {
                            isPast -> null
                            serverDDay + i == 0 -> "D-Day"
                            serverDDay + i > 31 -> "참여 예정"
                            else -> "D-${serverDDay + i}"
                        },
                        isPast = isPast,
                        plusDay = i
                    )
                )
            }
        }
        return result
    }

    // 뷰모드 전환
    fun onChangeViewMode(mode: HomeViewMode) {
        updateState { copy(viewMode = mode) }
    }

    // 이벤트 헬퍼 함수들
    fun onClickNotice() = emitEvent(HomeEvent.MoveNoticeEvent)
    fun onClickNotification() = emitEvent(HomeEvent.MoveNotificationEvent)
    fun onClickPlanAdd() = emitEvent(HomeEvent.MovePlanAddEvent)
    fun onClickPlanDetail(plan: SchedulePlanItem) = emitEvent(HomeEvent.MovePlanDetailEvent(plan))
}


data class HomeUiState(
    //달력 관련
    val selectedDate: LocalDate = LocalDate.now(),
    val eventDates: Set<LocalDate> = emptySet(),

    //달력 <-> 일정 전환
    val viewMode: HomeViewMode = HomeViewMode.CALENDAR,

    //유저 정보 영역
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

