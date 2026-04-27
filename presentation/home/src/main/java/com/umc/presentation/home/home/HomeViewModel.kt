package com.umc.presentation.home.home

import android.util.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import com.umc.component.base.BaseViewModel
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.umc.domain.model.UserInfo
import com.umc.domain.model.enums.HomeViewMode
import com.umc.domain.model.enums.UserType
import com.umc.domain.model.enums.WarningStatus
import com.umc.domain.model.home.SchedulePlanItem
import com.umc.domain.model.home.getGisuSummaryList
import com.umc.domain.model.home.schedule.ScheduleMonthModel
import com.umc.domain.usecase.GetGisuInfoUseCase
import com.umc.domain.usecase.member.GetMyProfileUseCase
import com.umc.domain.usecase.organization.GetGisuListUseCase
import com.umc.domain.usecase.schedule.GetScheduleMonthUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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

    //전체 기수 리스트 캐시
    private fun loadGisuList() = viewModelScope.launch {
        resultResponse(
            response = getGisuListUseCase(),
            successCallback = { gisuList ->
                // 전역 캐시에 저장
                //GisuCache.setGisuList(gisuList.gisuList)
            }
        )
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
                    gisuTag = gisuTags,
                    activeString = "${latestGisu?.gisu}기 활동 상태"
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
            /**수정**/
            Pair(days, UserType.OB)
        } else {
            //오늘이 종료일 이전이거나 종료일 당일인 경우
            //시작일로부터 오늘까지 며칠 지났는지 계산
            val days = ChronoUnit.DAYS.between(oldStartDate, today)
            Pair(days, UserType.ACTIVE)
        }
    }

    //UserInfo의 chllengerspoint를 바탕으로 현재 상태(패널티)를 계산하는 함수
    private fun calculateUserPoint(userInfo: UserInfo){
        //가장 최신 기수 챌린저 정보 가져오기
        val recentChallenge = userInfo.challengerRecords.maxByOrNull { it.gisu }
        Log.d("log_home", "$recentChallenge")

        //일단은 totalPoint로 계산 TODO: 차후 로직 변경 가능
        var sangjumtmp = 0
        var buljumtmp = 0

        //challengerPoints를 돌면서 더하기 빼기
        for (point in recentChallenge?.points ?: emptyList()){
            val nowPoint = point.value.toInt()
            if(nowPoint > 0){sangjumtmp += nowPoint}
            else{buljumtmp += nowPoint}
        }

        updateState {
            copy(
                sangjum = sangjumtmp,
                buljum = buljumtmp,
                total = sangjumtmp + buljumtmp
            )
        }

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
    fun onClickScheduleAdd() = emitEvent(HomeEvent.MovePlanAddEvent)
    fun onClickScheduleDetail(plan: SchedulePlanItem) = emitEvent(HomeEvent.MovePlanDetailEvent(plan))
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
    data class MoveScheduleDetailEvent(val plan: SchedulePlanItem) : HomeEvent //일정 상세 이동
    object MoveScheduleAddEvent : HomeEvent //일정 추가 이동

    object OpenDatePickerEvent : HomeEvent //날짜 선택 다이얼로그 열기

}

