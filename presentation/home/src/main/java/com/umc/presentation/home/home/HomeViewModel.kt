package com.umc.presentation.home.home

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import com.umc.component.base.BaseViewModel
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.umc.component.util.UTimeFormat
import com.umc.domain.model.UserInfo
import com.umc.domain.model.enums.HomeViewMode
import com.umc.domain.model.enums.UserType
import com.umc.domain.model.enums.WarningStatus
import com.umc.domain.model.home.GisuSummary
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
    private val savedStateHandle: SavedStateHandle, //배너 터치 상태 저장
    private val getMyProfileUseCase: GetMyProfileUseCase, //내 프로필 정보 가져오기
    private val getScheduleMonthUseCase: GetScheduleMonthUseCase, //월별 일정 가져오기
    private val getGisuInfoUseCase: GetGisuInfoUseCase, //기수 정보 가져오기
    private val getGisuListUseCase: GetGisuListUseCase, //전체 기수 리스트 가져오기
) : BaseViewModel<HomeUiState, HomeEvent>(
    HomeUiState())
{

    /**
     * 사용자가 이번 세션에서 배너를 닫았는지 저장하는 플래그 (화면 전환 후에도 저장을 위해 savedStateHandle 사용)
     * 사용자가 명함 교환 배너를 닫은 상태(isBannerDismissed)는 Process Death 및 화면 전환 후에도 유지되도록 SavedStateHandle에 가공하여 저장
     * **/
    private var isBannerDismissed: Boolean
        get() = savedStateHandle.get<Boolean>("IS_BANNER_DISMISSED") ?: false
        set(value) { savedStateHandle["IS_BANNER_DISMISSED"] = value }

    init {
        val today = UTimeFormat.getToday()

        //유저 프로필 정보 가져오기
        getUserInfo()

        //금일 기준 월별 데이터 가져오기 (LocalDate 사용)
        getScheduleMonth(today.year, today.monthValue, true)


        
    }

    //날짜 문자열 포맷 유틸리티 (LocalDate 포맷터 활용)
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")

    /**
     * [LocalDate 객체를 yyyy.MM.dd 규격의 문자열로 변환하는 헬퍼 함수]
     */
    private fun formatDate(date: LocalDate): String = date.format(dateFormatter)

    /**
     * 달력 날짜 선택 이벤트 처리 메서드
     *
     * 사용자가 달력에서 특정 날짜를 클릭했을 때 선택 날짜 상태를 업데이트하고,
     * 해당 날짜에 부합하는 일일 일정 리스트(dailyPlans)만 필터링하여 UI에 반영합니다.
     *
     * @param date 선택한 LocalDate 객체
     */
    fun setSelectedDate(date: LocalDate) {
        val dateString = formatDate(date)
        updateState {
            copy(
                selectedDate = date,
                dailyPlans = allPlans.filter { it.date == dateString }
            )
        }
    }

    /**
     * 달력 점(Dot) 표기를 위한 일정 보유 날짜 추출 메서드
     *
     * 월별 일정 목록(plans)에서 날짜 문자열만 중복 제거(toSet) 추출하여
     * HomeCalendar에서 빨간 점으로 표출할 eventDates 집합을 갱신합니다.
     *
     * 결과적으로 eventDates를 통해, 달력에 dot를 찍습낟.
     */
    private fun extractEventDates(plans: List<SchedulePlanItem>) {
        val dates = plans.map {
            LocalDate.parse(it.date, dateFormatter)
        }.toSet()

        updateState { copy(eventDates = dates) }
    }

    /**
     * 서버 내 프로필 정보 로드 및 저장
     *
     * 사용자 기본 프로필, 수료/참여 기수 정보, 상벌점 레코드를 가져옵니다.
     */
    fun getUserInfo() {
        viewModelScope.launch {
            resultResponse(
                response = getMyProfileUseCase(),
                successCallback = { userInfo ->
                    Log.d("log_home", "$userInfo")
                    //서버에서 로드한 정보를 바탕으로 UI에 들어갈 데이터 포멧팅
                    settingUserInfoToUI(userInfo)

                },
                errorCallback = {
                    /**TODO. 에러 토스트 메시지 등을 전송**/
                }
            )
        }
    }

    /**
     * 수신된 유저 정보를 기반으로 UI State 가공 및 업데이트 메인 함수
     *
     * @param userInfo 서버에서 전달받은 내 프로필
     */
    fun settingUserInfoToUI(userInfo: UserInfo) {
        /**
         * UserInfoParseItem.kt에 정의된 getGisuSummaryList()를 이용하여
         * UI에 사용할 핵심 데이터만을 로드
         * **/
        val gisuSummaryList = userInfo.getGisuSummaryList() //최신 기수 정보
        val gisuTags = extractGisuTags(gisuSummaryList) //기수 태그(프로필 카드)
        val latestGisu = gisuSummaryList.maxByOrNull { it.gisu } //최신 기수
        val startGisu = gisuSummaryList.minByOrNull { it.gisu } //시작 기수


        // 즉시 표출 가능한 기본 인적사항 UI State 반영
        updateState {
            copy(
                userName = userInfo.name,
                userNickName = userInfo.nickname,
                userMemberId = userInfo.id,
                gisuTag = gisuTags,
                activeString = "${latestGisu?.gisu ?: 0}기 활동 상태",
                growDay = userInfo.totalActivityDays.toInt()
            )
        }

        // 비동기로 시작 기수 및 최신 기수 상세 정보를 조회
        loadGisuDetailAndCalculateStatus(latestGisu?.gisuId, startGisu?.gisuId)

        // 최신 기수 상벌점 포인트 합산 집계
        calculateUserPoint(userInfo)
    }

    /**
     * 기수 요약 목록에서 UI용 기수 태그 문자열 추츌 험슈
     *
     * 입력된 기수 정보들을 오름차순 정렬하여 ["8기", "9기", "10기"] 형태의 문자열 리스트로 변환합니다.
     */
    private fun extractGisuTags(summaryList: List<GisuSummary>): List<String> {
        return summaryList.sortedBy { it.gisu }.map { "${it.gisu}기" }
    }

    /**
     * 최신 기수 및 시작 기수 정보를 비동기로 병렬 조회하여 유저 상태(OB/ACTIVE) 연산
     *
     * 두 개의 API 요청을 coroutineScope + async로 동시 수행하여 네트워크 대기 시간을 최적화합니다.
     */
    private fun loadGisuDetailAndCalculateStatus(latestGisuId: Long?, startGisuId: Long?) {
        if (latestGisuId == null || startGisuId == null) return

        viewModelScope.launch {
            coroutineScope {
                val latestDeferred = async { getGisuInfoUseCase(latestGisuId) }
                val startDeferred = async { getGisuInfoUseCase(startGisuId) }

                val latestRes = latestDeferred.await()
                val startRes = startDeferred.await()

                /**시작 기수와 최신 기수의 시작&종료 날짜 받아오기**/
                //최신 기수 정보를 성공적으로 받아올 때
                resultResponse(response = latestRes, successCallback = { latestInfo ->
                    //시작 기수 정보 성공적으로 받아올 때
                    resultResponse(response = startRes, successCallback = { startInfo ->
                        val (passedDay, userStatus) = getPassedDaysStatus(
                            latestInfo.startAt, latestInfo.endAt,
                            startInfo.startAt, startInfo.endAt
                        )

                        updateState {
                            copy(
                                userType = userStatus,
                            )
                        }
                    })
                })
            }
        }
    }


    /**
     * 기수 시작/종료일을 기반으로 유저의 현재 활동 상태(OB vs ACTIVE) 판단
     *
     * 오늘 날짜가 최신 기수의 종료일보다 뒤면 수료 회원(OB)으로 판단하고,
     * 진행 중이거나 이전이면 현재 활동 중인 회원(ACTIVE)으로 구분합니다.
     *
     * [수정] 현재 내 정보 조회 api v2를 통해 활동 날짜를 조회할 수 있습니다.
     *       그러나 차후 OB/YB 여부 체크를 위해, 해당 함수는 삭제하지 않고 남겨놓겠습니다.
     *       코드 이용 시 참고해주시면 감사합니다.
     */
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

    /**
     * 가장 최근 기수의 챌린저 포인트를 분석하여 상점/벌점/합계 점수 집계 메소드
     *
     * 양수 포인트는 상점(sangjum)으로, 음수 포인트는 벌점(buljum)으로 구분하여 가산 연산합니다.
     */
    private fun calculateUserPoint(userInfo: UserInfo){
        //가장 최신 기수 챌린저 정보 가져오기
        val recentChallenge = userInfo.challengerRecords.maxByOrNull { it.gisu }
        Log.d("log_home", "$recentChallenge")

        //일단은 totalPoint로 계산
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

    /**
     * 특정 년/월의 일정 데이터 조회 및 일일 일정/배너 노출 상태 업데이트
     *
     * @param year 연도 (예: 2026)
     * @param month 월 (예: 4)
     * @param todayCheck 초기 로딩 시 오늘 일정 존재 여부에 따른 명함 교환 배너 노출 처리 유무 (사용 X)
     */
    fun getScheduleMonth(year: Int, month: Int, todayCheck: Boolean = false) {
        viewModelScope.launch {
            resultResponse(
                response = getScheduleMonthUseCase(year, month),
                successCallback = { scheduleMonth ->

                    // 기간제 원본 일정을 날짜별 개별 아이템 리스트로 평탄화(Flatten)
                    val planItems = convertToPlanItems(scheduleMonth)
                    val todayString = formatDate(uiState.value.selectedDate)

                    // 오늘 날짜에 속한 일정이 존재하는지 확인
                    val hasTodayPlan = planItems.any { it.date == todayString }

                    // 오늘 일정이 존재하고 사용자가 수동으로 배너를 닫지 않았다면 명함 교환 배너 노출
                    val shouldShowBanner = hasTodayPlan && !isBannerDismissed


                    updateState {
                        copy(
                            allPlans = planItems,
                            dailyPlans = planItems.filter { it.date == todayString },
                            isBannerVisible = shouldShowBanner
                        )
                    }

                    // 달력 셀에 표시할 일정 보유 날짜(Dot) 집계 갱신
                    extractEventDates(planItems)
                },
                errorCallback = { message ->
                    Log.d("log_home", "월별 일정 조회 실패: $message")
                }
            )
        }
    }


    /**
     * 도메인 모델(ScheduleMonthModel)을 UI용 확장 일일 일정 리스트(SchedulePlanItem)로 변환하는 메서드
     *
     * [주의] 서버에서는 연일 일정에 대해 1개의 데이터를 주지만, UI 상에서는 연일 데이터를 모두 표시하고, 이에 대한 D-Day 계산도 수행해야 합니다.
     *       그래서 만들었습니다.
     * 서버에서 전달되는 시작일~종료일 기간 일정을 반복문을 통해 일자별 아이템으로 개별 생성합니다.
     * 오늘 날짜 기준으로 과거 여부(isPast) 및 D-Day(D-Day, D-N, 참여 예정 등) 문구를 계산하여 매핑합니다.
     */
    private fun convertToPlanItems(domainModels: List<ScheduleMonthModel>): List<SchedulePlanItem> {
        val result = mutableListOf<SchedulePlanItem>()
        val today = LocalDate.now()

        domainModels.forEach { schedule ->
            val startDate = LocalDate.parse(schedule.startDay, dateFormatter)
            val endDate = LocalDate.parse(schedule.endDay, dateFormatter)

            // 일정 총 기간 일수 연산
            val daysBetween = ChronoUnit.DAYS.between(startDate, endDate).toInt()
            val serverDDay = ChronoUnit.DAYS.between(today, startDate).toInt()

            // 시작일부터 종료일까지 매일의 날짜 아이템 개별 생성
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



    /**
     * 뷰 모드 전환 버튼 클릭 이벤트 (달력+일정 vs 전체 리스트)
     */
    fun onChangeViewMode(mode: HomeViewMode) {
        updateState { copy(viewMode = mode) }
    }

    // 이벤트 헬퍼 함수들
    fun onClickNotice() = emitEvent(HomeEvent.MoveNoticeEvent)
    fun onClickNotification() = emitEvent(HomeEvent.MoveNotificationEvent)
    fun onClickScheduleAdd() = emitEvent(HomeEvent.MoveScheduleAddEvent)
    fun onClickScheduleDetail(plan: SchedulePlanItem) = emitEvent(HomeEvent.MoveScheduleDetailEvent(plan))
    fun onClickCardShare() {
        isBannerDismissed = true // 사용자가 클릭하여 닫았음을 기억!

        updateState {
            copy(isBannerVisible = false)
        }
        emitEvent(HomeEvent.MoveShareCardEvent)
    }
}


data class HomeUiState(
    // 달력 및 일정 관련 상태
    val selectedDate: LocalDate = LocalDate.now(),
    val eventDates: Set<LocalDate> = emptySet(),
    val viewMode: HomeViewMode = HomeViewMode.CALENDAR,

    // 유저 프로필 영역 상태
    val userName: String = "",
    val userMemberId : Long = 0L,
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

    //홈 카드 보여주기
    val isBannerVisible : Boolean = true

) : UiState


sealed interface HomeEvent : UiEvent {
    object MoveNoticeEvent : HomeEvent //공시사항 이동
    object MoveNotificationEvent : HomeEvent //알림 이동
    data class MoveScheduleDetailEvent(val plan: SchedulePlanItem) : HomeEvent //일정 상세 이동
    object MoveScheduleAddEvent : HomeEvent //일정 추가 이동

    object OpenDatePickerEvent : HomeEvent //날짜 선택 다이얼로그 열기
    object MoveShareCardEvent : HomeEvent //카드 공유 이동

}

