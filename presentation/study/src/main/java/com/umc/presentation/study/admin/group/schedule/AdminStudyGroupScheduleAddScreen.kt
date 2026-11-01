package com.umc.presentation.study.admin.group.schedule

import com.umc.component.theme.AppStrings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.umc.component.theme.grey50
import com.umc.presentation.study.admin.group.schedule.component.*

/**
 * 관리자 스터디 그룹 일정 등록 화면
 *
 * 선택한 스터디 그룹에 새로운 일정을 등록하기 위한 화면입니다.
 *
 * 주요 입력 정보
 * - 스터디명
 * - 시작 / 종료 일시
 * - 하루 종일 여부
 * - 대면 진행 여부 및 장소
 * - 상세 안내
 * - 참여 챌린저
 * - 출석부 생성 여부 및 출석 시간
 * - 연결할 커리큘럼 주차
 */
@Composable
fun AdminStudyGroupScheduleScreen(
    state: AdminStudyGroupScheduleState,
    onAction: (AdminStudyGroupScheduleAction) -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(grey50())
    ) {

        // 상단 뒤로가기 및 일정 등록 버튼
        GroupScheduleAddTopBar(
            canRegister = state.canRegister,
            onBackClick = {
                onAction(AdminStudyGroupScheduleAction.ClickBack)
            },
            onRegisterClick = {
                onAction(AdminStudyGroupScheduleAction.ClickRegister)
            },
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(
                    top = 24.dp,
                    bottom = 40.dp
                )
        ) {

            // 현재 일정을 등록하고 있는 스터디 그룹 정보
            GroupScheduleInfoCard(
                groupTitle = state.groupTitle,
                groupPart = state.groupPartLabel,
            )

            Spacer(Modifier.height(28.dp))

            // 스터디명 입력
            GroupScheduleSectionTitle(
                text = AppStrings.STUDY_SCHEDULE_NAME_LABEL
            )

            Spacer(Modifier.height(12.dp))

            GroupScheduleTextField(
                value = state.studyName,
                placeholder = AppStrings.STUDY_SCHEDULE_NAME_PLACEHOLDER,
                onValueChange = {
                    onAction(
                        AdminStudyGroupScheduleAction
                            .OnStudyNameChanged(it)
                    )
                },
            )

            Spacer(Modifier.height(32.dp))

            // 일정 시작 / 종료 일시
            GroupScheduleSectionTitle(
                text = AppStrings.STUDY_SCHEDULE_TIME_LABEL
            )

            Spacer(Modifier.height(16.dp))

            GroupScheduleDateTimeBox(
                isAllDay = state.isAllDay,
                startText = state.startDateTimeText,
                endText = state.endDateTimeText,
                onAllDayClick = {
                    onAction(
                        AdminStudyGroupScheduleAction.ToggleAllDay
                    )
                },
                onStartClick = {
                    onAction(
                        AdminStudyGroupScheduleAction.ClickStartDateTime
                    )
                },
                onEndClick = {
                    onAction(
                        AdminStudyGroupScheduleAction.ClickEndDateTime
                    )
                },
            )

            Spacer(Modifier.height(32.dp))

            // 대면 진행 여부 및 장소
            GroupScheduleSectionTitle(
                text = AppStrings.STUDY_SCHEDULE_LOCATION_LABEL
            )

            Spacer(Modifier.height(12.dp))

            GroupScheduleToggleRow(
                checked = state.isOffline,
                text = AppStrings.STUDY_SCHEDULE_ONLINE_TITLE,
                onClick = {
                    onAction(
                        AdminStudyGroupScheduleAction.ToggleOffline
                    )
                },
            )

            // 대면 일정일 경우에만 장소 선택 영역 표시
            if (state.isOffline) {
                Spacer(Modifier.height(12.dp))

                GroupScheduleSelectRow(
                    text = state.placeText,
                    placeholder = AppStrings.STUDY_SCHEDULE_LOCATION_PLACEHOLDER,
                    enabled = true,
                    onClick = {
                        onAction(
                            AdminStudyGroupScheduleAction.ClickPlace
                        )
                    },
                )
            }

            Spacer(Modifier.height(32.dp))

            // 일정 상세 안내 입력
            GroupScheduleSectionTitle(
                text = AppStrings.STUDY_SCHEDULE_DESCRIPTION_LABEL
            )

            Spacer(Modifier.height(12.dp))

            GroupScheduleMemoBox(
                value = state.detail,
                placeholder = AppStrings.STUDY_SCHEDULE_DESCRIPTION_PLACEHOLDER,
                onValueChange = {
                    onAction(
                        AdminStudyGroupScheduleAction
                            .OnDetailChanged(it)
                    )
                },
            )

            Spacer(Modifier.height(32.dp))

            // 일정에 참여할 챌린저 선택
            GroupScheduleSectionTitle(
                text = AppStrings.STUDY_SCHEDULE_CHALLENGER_LABEL
            )

            Spacer(Modifier.height(12.dp))

            GroupScheduleSelectRow(
                text = state.challengerText,
                placeholder = AppStrings.STUDY_SCHEDULE_CHALLENGER_PLACEHOLDER,
                onClick = {
                    onAction(
                        AdminStudyGroupScheduleAction.ClickChallenger
                    )
                },
            )

            Spacer(Modifier.height(16.dp))

            // 출석부 생성 여부
            GroupScheduleToggleRow(
                checked = state.createAttendance,
                text = AppStrings.STUDY_SCHEDULE_ATTENDANCE_TITLE,
                onClick = {
                    onAction(
                        AdminStudyGroupScheduleAction.ToggleAttendance
                    )
                },
            )

            Spacer(Modifier.height(12.dp))

            // 출석부 생성 시 출석 시간 설정 영역 표시
            if (state.createAttendance) {
                GroupScheduleAttendanceBox(
                    checkInStartText = state.checkInStartText,
                    onTimeEndText = state.onTimeEndText,
                    lateEndText = state.lateEndText,
                    onCheckInStartClick = {
                        onAction(
                            AdminStudyGroupScheduleAction.ClickCheckInStart
                        )
                    },
                    onOnTimeEndClick = {
                        onAction(
                            AdminStudyGroupScheduleAction.ClickOnTimeEnd
                        )
                    },
                    onLateEndClick = {
                        onAction(
                            AdminStudyGroupScheduleAction.ClickLateEnd
                        )
                    },
                )
            }

            Spacer(Modifier.height(32.dp))

            // 일정과 연결할 커리큘럼 주차
            GroupScheduleSectionTitle(
                text = AppStrings.STUDY_SCHEDULE_WEEK_LABEL
            )

            Spacer(Modifier.height(12.dp))

            GroupScheduleSelectRow(
                text = state.weekText,
                placeholder = AppStrings.STUDY_SCHEDULE_WEEK_PLACEHOLDER,
                onClick = {
                    onAction(
                        AdminStudyGroupScheduleAction.ClickWeek
                    )
                },
            )
        }
    }
}

/**
 * 관리자 스터디 그룹 일정 등록 화면 Preview
 */
@Preview(
    showBackground = true,
    showSystemUi = true,
    widthDp = 390,
    heightDp = 844
)
@Composable
private fun AdminStudyGroupScheduleScreenPreview() {
    AdminStudyGroupScheduleScreen(
        state = AdminStudyGroupScheduleState()
    )
}