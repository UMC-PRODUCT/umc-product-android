package com.umc.presentation.study.admin.group.schedule

import com.umc.component.base.UiState
import com.umc.presentation.study.admin.group.schedule.bottomsheet.GroupScheduleChallengerUiModel
import com.umc.presentation.study.admin.submit.bottomsheet.AdminSubmitWeekUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * 관리자 스터디 그룹 일정 등록 화면 상태
 *
 * 일정 등록 화면에서 입력하거나 선택한 모든 값을 관리합니다.
 * [canRegister]를 통해 현재 일정 등록 가능 여부를 계산합니다.
 */
data class AdminStudyGroupScheduleState(

    // 일정이 등록될 스터디 그룹 정보
    val groupId: Long = 0L,
    val groupTitle: String = "",
    val groupPart: String = "",

    // 스터디명
    val studyName: String = "",

    // 하루 종일 일정 여부
    val isAllDay: Boolean = false,

    // API 요청에 사용할 시작 / 종료 일시
    val startDateTime: String? = null,
    val endDateTime: String? = null,

    // 화면에 표시할 시작 / 종료 일시
    val startDateTimeText: String? = null,
    val endDateTimeText: String? = null,

    // 대면 진행 여부 및 선택 장소
    val isOffline: Boolean = false,
    val placeText: String = "",
    val placeLatitude: Double? = null,
    val placeLongitude: Double? = null,

    // 일정 상세 안내
    val detail: String = "",

    // 일정에 참여할 챌린저 목록
    val selectedChallengers:
    ImmutableList<GroupScheduleChallengerUiModel> = persistentListOf(),

    // 화면에 표시할 챌린저 선택 요약 문구
    val challengerText: String = "",

    // 출석부 생성 여부
    val createAttendance: Boolean = false,

    // API 요청에 사용할 출석 시간
    val checkInStartDateTime: String? = null,
    val onTimeEndDateTime: String? = null,
    val lateEndDateTime: String? = null,

    // 화면에 표시할 출석 시간
    val checkInStartText: String? = null,
    val onTimeEndText: String? = null,
    val lateEndText: String? = null,

    // 선택 가능한 커리큘럼 주차 목록
    val weeks: ImmutableList<AdminSubmitWeekUiModel> = persistentListOf(),

    // 선택한 주차의 커리큘럼 ID
    val selectedWeeklyCurriculumId: Long? = null,

    // 화면에 표시할 선택 주차
    val weekText: String = "",

    // 일정 등록 API 요청 진행 여부
    val isRegistering: Boolean = false,

    ) : UiState {

    /**
     * 현재 입력값을 기준으로 일정 등록 가능 여부를 반환합니다.
     *
     * 필수 조건
     * - 스터디명이 입력되어 있어야 함
     * - 챌린저가 한 명 이상 선택되어 있어야 함
     * - 시작 / 종료 일시가 선택되어 있어야 함
     * - 커리큘럼 주차가 선택되어 있어야 함
     * - 대면 일정이면 장소 및 좌표가 필요함
     * - 출석부를 생성하면 출석 관련 시간이 모두 필요함
     * - 일정 등록 요청 중이 아니어야 함
     */
    val canRegister: Boolean
        get() {
            if (studyName.isBlank()) {
                return false
            }

            if (selectedChallengers.isEmpty()) {
                return false
            }

            if (
                startDateTime == null ||
                endDateTime == null
            ) {
                return false
            }

            if (selectedWeeklyCurriculumId == null) {
                return false
            }

            // 대면 일정인 경우 장소 정보 확인
            if (
                isOffline &&
                (
                        placeText.isBlank() ||
                                placeLatitude == null ||
                                placeLongitude == null
                        )
            ) {
                return false
            }

            // 출석부 생성 시 출석 시간 입력 여부 확인
            if (
                createAttendance &&
                (
                        checkInStartDateTime == null ||
                                onTimeEndDateTime == null ||
                                lateEndDateTime == null
                        )
            ) {
                return false
            }

            return !isRegistering
        }
}