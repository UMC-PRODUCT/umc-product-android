package com.umc.presentation.study.admin.group.schedule

import com.umc.presentation.study.admin.group.schedule.bottomsheet.GroupScheduleChallengerUiModel

/**
 * 관리자 스터디 그룹 일정 등록 화면에서 발생하는 사용자 액션
 *
 * 화면에서 발생한 클릭, 입력, 선택 이벤트를 ViewModel로 전달합니다.
 */
sealed interface AdminStudyGroupScheduleAction {

    // 상단 뒤로가기 버튼 클릭
    data object ClickBack : AdminStudyGroupScheduleAction

    // 일정 등록 버튼 클릭
    data object ClickRegister : AdminStudyGroupScheduleAction

    // 스터디명 입력
    data class OnStudyNameChanged(
        val value: String,
    ) : AdminStudyGroupScheduleAction

    // 상세 안내 내용 입력
    data class OnDetailChanged(
        val value: String,
    ) : AdminStudyGroupScheduleAction

    // 하루 종일 여부 변경
    data object ToggleAllDay : AdminStudyGroupScheduleAction

    // 대면 진행 여부 변경
    data object ToggleOffline : AdminStudyGroupScheduleAction

    // 출석부 생성 여부 변경
    data object ToggleAttendance : AdminStudyGroupScheduleAction

    // 시작 일시 선택
    data object ClickStartDateTime : AdminStudyGroupScheduleAction

    // 종료 일시 선택
    data object ClickEndDateTime : AdminStudyGroupScheduleAction

    // 장소 선택
    data object ClickPlace : AdminStudyGroupScheduleAction

    // 챌린저 선택
    data object ClickChallenger : AdminStudyGroupScheduleAction

    // 출석 시작 시간 선택
    data object ClickCheckInStart : AdminStudyGroupScheduleAction

    // 정상 출석 종료 시간 선택
    data object ClickOnTimeEnd : AdminStudyGroupScheduleAction

    // 지각 종료 시간 선택
    data object ClickLateEnd : AdminStudyGroupScheduleAction

    // 커리큘럼 주차 선택
    data object ClickWeek : AdminStudyGroupScheduleAction

    /**
     * 일정과 연결할 커리큘럼 주차 선택
     *
     * @param week 화면에 표시할 주차
     * @param weeklyCurriculumId 일정 등록 API에 전달할 주차 커리큘럼 ID
     */
    data class SelectWeek(
        val week: Int,
        val weeklyCurriculumId: Long,
    ) : AdminStudyGroupScheduleAction

    /**
     * 대면 스터디 장소 선택
     *
     * @param place 선택한 장소명
     * @param latitude 위도
     * @param longitude 경도
     */
    data class SelectPlace(
        val place: String,
        val latitude: Double,
        val longitude: Double,
    ) : AdminStudyGroupScheduleAction

    /**
     * 일정에 참여할 챌린저 선택
     *
     * @param challengers 선택된 챌린저 목록
     * @param summaryText 화면에 표시할 선택 결과 요약 문구
     */
    data class SelectChallengers(
        val challengers: List<GroupScheduleChallengerUiModel>,
        val summaryText: String,
    ) : AdminStudyGroupScheduleAction
}