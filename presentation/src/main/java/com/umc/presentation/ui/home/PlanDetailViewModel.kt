package com.umc.presentation.ui.home

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.umc.domain.model.enums.CategoryType
import com.umc.domain.model.enums.PermissionType
import com.umc.domain.model.enums.ResourceType
import com.umc.domain.model.home.PlanDetailItem
import com.umc.domain.usecase.GetAuthAccessUseCase
import com.umc.domain.usecase.schedule.DeleteScheduleUseCase
import com.umc.domain.usecase.schedule.GetScheduleDetailHomeUseCase
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject


@HiltViewModel
class PlanDetailViewModel @Inject
constructor(
    private val getScheduleDetailHomeUseCase: GetScheduleDetailHomeUseCase, //일정 상세 정보 가져오기
    private val deleteScheduleUseCase: DeleteScheduleUseCase, //일정 삭제하기
    private val getAuthAccessUseCase: GetAuthAccessUseCase, //리소스 권한 조회
    ) : BaseViewModel<PlanDetailFragmentUiState, PlanDetailFragmentEvent>(
    PlanDetailFragmentUiState()){




        //서버에서 게시글 상세 정보 가져오기
        fun getScheduleDetail(scheduleId : Long, plusDay: Int){
            val now = uiState.value.listDetailItem.find { it.scheduleId == scheduleId }
            Log.d("log_home", "getScheduleDetail: $now")
            updateState {
                copy(
                    content = now ?: PlanDetailItem(),
                    plusDay = plusDay
                )
            }
            convertPlanDetailItemToUiState(now ?: PlanDetailItem(), plusDay)
        }

        //일정 게시글 접근 권한 조회 및 UI 설정 함수
        fun settingScheduleAuthAccess(scheduleId : Long){
            viewModelScope.launch {
                resultResponse(
                    response = getAuthAccessUseCase(ResourceType.SCHEDULE, scheduleId),
                    successCallback = { authAccess ->
                        //삭제나 작성 권한이 있으면 isAuthor로 취급
                        val isAuthor = authAccess.permissions.any { item ->
                            (item.type == PermissionType.DELETE || item.type == PermissionType.EDIT)
                                    && item.hasPermission
                        }

                        updateState {
                            copy(isAuthor = isAuthor)
                        }

                    },
                    errorCallback = {},
                )

            }
        }

        //PlanDetailItem에서 UI에 맞게 데이터를 조절하는 함수
        fun convertPlanDetailItemToUiState(item: PlanDetailItem, plusDay: Int) {
            val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
            val startDate = LocalDate.parse(item.startDay, formatter)
            val today = LocalDate.now()
            val dDay = ChronoUnit.DAYS.between(today, startDate).toInt()


            val finalDDayValue = dDay + plusDay //시작 시간과 진행 상황 합치기

            val dDayString: String //D-몇일 포맷
            val isTodayCheck: Boolean //금일 인가? -> 버튼 생성

            when {
                finalDDayValue == 0 -> {
                    dDayString = "D-DAY"
                    isTodayCheck = true
                }
                finalDDayValue > 31 -> {
                    dDayString = "참여 예정"
                    isTodayCheck = false
                }
                finalDDayValue > 0 -> {
                    dDayString = "D-$finalDDayValue"
                    isTodayCheck = false
                }
                else -> { // 음수 (이미 종료된 날짜)
                    dDayString = "종료된 일정"
                    isTodayCheck = false
                }
            }

            val todayDateString = calculateTargetDate(item.startDay, plusDay)
            val todayTime = if (item.isAllDay) {
                "00:00-23:59"
            } else {
                "${item.startTime}-${item.endTime}"
            }


            updateState {
                copy(
                    content = item,
                    plusDay = plusDay,
                    isToday = isTodayCheck,
                    dDay = dDayString,
                    title = item.name,
                    startDate = item.startDay, // "2026.02.05"
                    todayDate = todayDateString, // "2026.02.07"
                    todayTime = todayTime, // "05:24-05:24"
                    place = item.locationName,
                    detail = item.description,
                    longitude = item.longitude,
                    latitude = item.latitude,
                )
            }

        }

        //일정 계산하는 포맷 함수
        private fun calculateTargetDate(startDay: String, plusDay: Int): String {
            return try {
                val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
                val startDate = LocalDate.parse(startDay, formatter)
                startDate.plusDays(plusDay.toLong()).format(formatter)
            } catch (e: Exception) {
                startDay
            }
        }


        //출석 체크 로직
        fun onClickConfirmAttention(){
            emitEvent(PlanDetailFragmentEvent.TouchConfirmAttention)
        }

        //상단 케밥 메뉴 열기
        fun toggleKebabMenu(){
            updateState { copy(isMenuVisible = !isMenuVisible) }
        }

        //신고 로직 수행
        fun reportPlan(){
            updateState { copy(isMenuVisible = false) }
            emitEvent(PlanDetailFragmentEvent.ReportPlan)
        }

        //수정 로직 수행
        fun editPlan(){
            updateState { copy(isMenuVisible = false) }
            emitEvent(PlanDetailFragmentEvent.EditPlan)
        }

        //삭제 로직 확인 다이얼로그 생성 로직 수행
        fun checkDeletePlan(){
            updateState { copy(isMenuVisible = false) }
            emitEvent(PlanDetailFragmentEvent.CheckDeletePlan)
        }

        //삭제 로직 수행
        fun deletePlan(){
            viewModelScope.launch {
                val scheduleId = uiState.value.content.scheduleId
                resultResponse(
                    response = deleteScheduleUseCase(scheduleId),
                    successCallback = {
                        emitEvent(PlanDetailFragmentEvent.MoveBackPressedEvent)
                    },
                    errorCallback = {

                    }
                )
            }
        }


        //뒤로 가기
        fun onClickBackPressed(){
            emitEvent(PlanDetailFragmentEvent.MoveBackPressedEvent)
        }




}

data class PlanDetailFragmentUiState(

    val listDetailItem: List<PlanDetailItem> = listOf(
        // ID 1: UMC 데모데이 준비
        PlanDetailItem(
            scheduleId = 1,
            name = "UMC 데모데이 준비",
            description = "UMC 5기 데모데이 부스 운영 및 시연용 디바이스 최종 세팅",
            tags = listOf(CategoryType.PROJECT, CategoryType.PRESENTATION),
            startDay = "2026.02.13",
            startTime = "14:00",
            endDay = "2026.02.13",
            endTime = "17:00",
            locationName = "숭실대학교 정보과학관",
            latitude = 37.4963,
            longitude = 126.9574,
            status = "COMPLETED",
            dDay = -1
        ),
        // ID 2: 최종 성적 확인
        PlanDetailItem(
            scheduleId = 2,
            name = "최종 성적 확인",
            description = "2025학년도 2학기 전체 성적 확인 및 성적 이의신청 기간 확인",
            tags = listOf(CategoryType.GENERAL),
            startDay = "2026.02.14",
            startTime = "10:00",
            endDay = "2026.02.14",
            endTime = "11:00",
            locationName = "숭실대학교 정보과학관",
            status = "COMPLETED",
            dDay = -1
        ),
        // ID 3: UMC 중앙진 회의
        PlanDetailItem(
            scheduleId = 3,
            name = "UMC 중앙진 회의",
            description = "UMC 운영진 정기 회의 및 차기 기수 모집 공고 검토",
            tags = listOf(CategoryType.PRESENTATION),
            startDay = "2026.02.18",
            startTime = "18:00",
            endDay = "2026.02.18",
            endTime = "20:00",
            locationName = "강남 스터디룸",
            status = "COMPLETED",
            dDay = -1
        ),
        // ID 4: 앱 안드로이드 배포 (기준일 D-Day)
        PlanDetailItem(
            scheduleId = 4,
            name = "앱 안드로이드 배포",
            description = "SleepingHero 또는 Momenty 앱 구글 플레이 스토어 최종 빌드 및 배포 심사 제출",
            tags = listOf(CategoryType.PROJECT),
            startDay = "2026.02.20",
            startTime = "21:00",
            endDay = "2026.02.20",
            endTime = "22:00",
            locationName = "자택",
            status = "IN_PROGRESS",
            dDay = 0,
            requiresAttendanceApproval = true
        ),
        // ID 5: 앱 프로덕트 점검 (24일)
        PlanDetailItem(
            scheduleId = 5,
            name = "앱 프로덕트 점검",
            description = "배포 이후 발생한 런타임 오류 및 사용자 피드백 1차 정리",
            tags = listOf(CategoryType.PROJECT),
            startDay = "2026.02.24",
            startTime = "15:00",
            endDay = "2026.02.24",
            endTime = "18:00",
            locationName = "스타벅스 숭실대입구점",
            latitude = 37.4963,
            longitude = 126.9574,
            status = "PENDING",
            dDay = 4
        ),
        // ID 5: 앱 프로덕트 점검 (25일 - 연속 일정)
        PlanDetailItem(
            scheduleId = 6,
            name = "앱 프로덕트 점검",
            description = "피드백 기반 핫픽스 업데이트 진행 및 코드 리뷰",
            tags = listOf(CategoryType.PROJECT),
            startDay = "2026.02.25",
            startTime = "15:00",
            endDay = "2026.02.25",
            endTime = "18:00",
            locationName = "스타벅스 숭실대입구점",
            status = "PENDING",
            dDay = 5
        ),
        // ID 6: 숭실대학교 개강
        PlanDetailItem(
            scheduleId = 7,
            name = "숭실대학교 개강",
            description = "2026학년도 1학기 개강. 강의실 위치 확인 및 강의계획서 점검",
            tags = listOf(CategoryType.NETWORKING),
            startDay = "2026.03.02",
            startTime = "09:00",
            endDay = "2026.03.02",
            endTime = "18:00",
            isAllDay = true,
            locationName = "숭실대학교",
            latitude = 37.4963,
            longitude = 126.9574,
            status = "UPCOMING",
            dDay = 10
        ),
        // ID 7: 아이디어톤 (27일)
        PlanDetailItem(
            scheduleId = 8,
            name = "아이디어톤",
            description = "본선 진출팀 대상 아이디어톤 무박 2일 진행 (프로토타입 개발)",
            tags = listOf(CategoryType.PROJECT, CategoryType.RETROSPECTIVE),
            startDay = "2026.03.27",
            startTime = "10:00",
            endDay = "2026.03.27",
            endTime = "23:59",
            locationName = "강남 코엑스",
            latitude = 37.5113,
            longitude = 127.0596,
            status = "UPCOMING",
            dDay = 35,
            requiresAttendanceApproval = true
        ),
        // ID 7: 아이디어톤 (28일 - 연속 일정)
        PlanDetailItem(
            scheduleId = 9,
            name = "아이디어톤",
            description = "최종 결과 발표 및 네트워킹 세션",
            tags = listOf(CategoryType.PROJECT, CategoryType.RETROSPECTIVE),
            startDay = "2026.03.28",
            startTime = "13:00",
            endDay = "2026.03.28",
            endTime = "18:00",
            locationName = "강남 코엑스",
            status = "UPCOMING",
            dDay = 36
        )
    ),

    //일정 관련
    val content : PlanDetailItem = PlanDetailItem(),
    val plusDay : Int = 0,

    val isToday : Boolean = false, //출석 체크 버튼 visible 유무
    val dDay : String = "참여 예정", // or D-DAY or D-몇일
    val title : String = "정기 세션 3주차",
    val startDate : String = "",
    val todayDate : String = "",
    val todayTime : String = "",
    val place : String = "",
    val detail : String = "",
    val longitude : Double = 0.0,
    val latitude : Double = 0.0,


    //내가 작성한 것인지 여부
    val isAuthor: Boolean = false,

    //케밥 메뉴 아이콘 보이기 여부
    val isMenuVisible : Boolean = false,


) : UiState

sealed interface PlanDetailFragmentEvent : UiEvent {

    object MoveBackPressedEvent : PlanDetailFragmentEvent

    //토글 이벤트
    object ToggleMenu : PlanDetailFragmentEvent
    //신고하기 이벤트
    object ReportPlan : PlanDetailFragmentEvent
    //수정하기 이벤트
    object EditPlan : PlanDetailFragmentEvent
    //삭제하기 이벤트 (다이얼로그로 확인)
    object CheckDeletePlan : PlanDetailFragmentEvent


    //출석 체크 이벤트
    object TouchConfirmAttention : PlanDetailFragmentEvent


}