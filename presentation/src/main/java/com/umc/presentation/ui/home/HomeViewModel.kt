package com.umc.presentation.ui.home

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.umc.domain.model.UserInfo

import com.umc.domain.model.enums.HomeViewMode
import com.umc.domain.model.enums.UserType
import com.umc.domain.model.enums.WarningStatus
import com.umc.domain.model.home.SchedulePlanItem
import com.umc.domain.model.home.getGisuSummaryList
import com.umc.domain.model.home.schedule.ScheduleMonthModel
import com.umc.domain.usecase.GetGisuInfoUseCase
import com.umc.domain.usecase.organization.GetGisuListUseCase

import com.umc.domain.usecase.schedule.GetScheduleMonthUseCase

import com.umc.domain.usecase.member.GetMyProfileUseCase
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import com.umc.presentation.util.GisuCache
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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
    private val getGisuInfoUseCase: GetGisuInfoUseCase, //기수 정보 가져오기
    private val getGisuListUseCase: GetGisuListUseCase, //전체 기수 리스트 가져오기
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

        //4. 전체 기수 리스트 캐시
        loadGisuList()

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
                    Log.d("log_home", "$userInfo")
                    settingUserInfoToUI(userInfo)


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

    //전체 기수 리스트 캐시
    private fun loadGisuList() = viewModelScope.launch {
        resultResponse(
            response = getGisuListUseCase(),
            successCallback = { gisuList ->
                // 전역 캐시에 저장
                GisuCache.setGisuList(gisuList.gisuList)
            }
        )
    }

    //UserInfo를 받아았을 때 이를 파싱해서 UI 요소로 분할하는 함수
    fun settingUserInfoToUI(userInfo: UserInfo){
        // 기수별 정보가 담긴 것.
        val gisuSummaryList = userInfo.getGisuSummaryList()

        //1. 기수 태그 정보 (11기, 12기, 13기... 생성)
        val gisuTags: List<String> = gisuSummaryList
            .sortedBy { it.gisu } // 기수 번호 낮은 순으로 정렬
            .map { "${it.gisu}기" } // String 포맷팅

        // 2. 최신기수를 가져오기
        val latestGisu = gisuSummaryList.maxByOrNull { it.gisu }
        val startGisu = gisuSummaryList.minByOrNull { it.gisu }


        viewModelScope.launch {
            //3. 기본 정보 우선 업데이트
            updateState {
                copy(
                    userName = userInfo.name,
                    userNickName = userInfo.nickname,
                    gisuTag = gisuTags
                )
            }

            //최신 기수 정보랑 제일 오래된 기수 정보 둘 다 병렬로 실행(YB는 옛날꺼 필요)
            coroutineScope {
                val latestResponseDeferred = async {
                    latestGisu?.let { getGisuInfoUseCase(it.gisuId) }
                }
                val startResponseDeferred = async {
                    startGisu?.let { getGisuInfoUseCase(it.gisuId) }
                }

                val latestResponse = latestResponseDeferred.await()
                val startResponse = startResponseDeferred.await()

                if (latestResponse != null && startResponse != null){
                    //최신 기수 정보를 성공적으로 받아올 때,
                    resultResponse(
                        response = latestResponse,
                        successCallback = { latestGisuInfo ->
                            //시작 기수 정보 성공적으로 받아올 때
                            resultResponse(
                                response = startResponse,
                                successCallback = { startGisuInfo ->
                                    //5. 성공 시 날짜 계산
                                    val (passedDay, userStatus) = getPassedDaysStatus(latestGisuInfo.startAt, latestGisuInfo.endAt,
                                        startGisuInfo.startAt, startGisuInfo.endAt)

                                    updateState {
                                        copy(
                                            userType = userStatus,
                                            growDay = passedDay.toInt(),
                                        )
                                    }
                                },
                                errorCallback = {
                                    //시작 기수 정보 받기 실패
                                }
                            )


                        },
                        errorCallback = {
                            //최신 기수 정보 받기 실패
                        }
                    )
                }
                

            }
        }

        //5. 점수 계산을 통해, 현재 상태 변경하기
        calculateUserPoint(userInfo)

    }

    //UserInfo의 chllengerspoint를 바탕으로 현재 상태(패널티)를 계산하는 함수
    private fun calculateUserPoint(userInfo: UserInfo){
        //가장 최신 기수 챌린저 정보 가져오기
        val recentChallenge = userInfo.challengerRecords.maxByOrNull { it.gisu }
        
        //일단은 totalPoint로 계산 TODO: 차후 로직 변경 가능
        val recentTotalPoint = recentChallenge?.totalPoint ?: 0.0

        val warningStatus = when {
            recentTotalPoint <= 0.0 -> WarningStatus.NORMAL    // 0이하 (0)
            recentTotalPoint < 2.0 -> WarningStatus.WARNING    // 2 이하인 경우 (1~1.9)
            else -> WarningStatus.DANGER                       // 2 이상인 경우 (2~)
        }

        val warningStatusText = when(warningStatus){
            WarningStatus.DANGER -> "경고가 2회 누적되었습니다."
            WarningStatus.WARNING -> "경고가 1회 누적되었습니다."
            WarningStatus.NORMAL -> "누적된 경고가 없습니다."

        }

        updateState {
            copy(
                warningStatus = warningStatus,
                warningStatusText = warningStatusText
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

            //일정 시작 날짜(StartDate 기준으로 dday 생성)
            val today = LocalDate.now()
            val serverDDay = ChronoUnit.DAYS.between(today, startDate).toInt()
            

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
                        isPast = isPast,
                        plusDay = i
                    )
                )
            }
        }
        return result
    }

    //최신 기수 날짜 정보를 통해, OB인지 ACTIVE인지 판단하고, 몇일 지났는지 표현
    fun getPassedDaysStatus(latestStartDateStr: String, latestEndDateStr: String,
                            oldStartDateStr: String, oldEndDateStr: String): Pair<Long, UserType> {
        val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
        val latestStartDate = LocalDate.parse(latestStartDateStr, formatter)
        val latestEndDate = LocalDate.parse(latestEndDateStr, formatter)
        val oldStartDate = LocalDate.parse(oldStartDateStr, formatter)
        val oldEndDate = LocalDate.parse(oldEndDateStr, formatter)

        val today = LocalDate.now() // 2026-02-16

        //오늘이 종료일보다 뒤면 (OB)
        return if (today.isAfter(latestEndDate)) {
            //종료일로부터 오늘까지 며칠 지났는지 계산
            val days = ChronoUnit.DAYS.between(latestEndDate, today)
            Pair(days, UserType.OB)
        } else {
            //오늘이 종료일 이전이거나 종료일 당일인 경우
            //시작일로부터 오늘까지 며칠 지났는지 계산
            val days = ChronoUnit.DAYS.between(oldStartDate, today)
            Pair(days, UserType.ACTIVE)
        }
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
    val userName: String = "",
    val userNickName: String = "",
    val growDay: Int = 0,
    val gisuTag: List<String> = emptyList(),

    val userType: UserType = UserType.ACTIVE,

    val warningStatus: WarningStatus = WarningStatus.NORMAL,

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
    val plusDays : Int = 0, //연속 날짜 처리 용도

) : UiState


sealed interface HomeFragmentEvent : UiEvent {
    object MoveNoticeEvent : HomeFragmentEvent //공시사항 이동
    object MoveNotificationEvent : HomeFragmentEvent //알림 이동
    data class MovePlanDetailEvent(val plan: SchedulePlanItem) : HomeFragmentEvent //일정 상세 이동
    object MovePlanAddEvent : HomeFragmentEvent //일정 추가 이동

    object OpenDatePickerEvent : HomeFragmentEvent //날짜 선택 다이얼로그 열기

}

