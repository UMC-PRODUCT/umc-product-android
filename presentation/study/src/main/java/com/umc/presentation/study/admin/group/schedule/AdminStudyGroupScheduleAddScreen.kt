package com.umc.presentation.study.admin.group.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.umc.component.theme.grey000
import com.umc.presentation.study.admin.group.schedule.component.*

@Composable
fun AdminStudyGroupScheduleScreen(
    state: AdminStudyGroupScheduleState,
    onAction: (AdminStudyGroupScheduleAction) -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(grey000())
    ) {
        GroupScheduleAddTopBar(
            canRegister = state.canRegister,
            onBackClick = { onAction(AdminStudyGroupScheduleAction.ClickBack) },
            onRegisterClick = { onAction(AdminStudyGroupScheduleAction.ClickRegister) },
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 24.dp, bottom = 40.dp)
        ) {
            GroupScheduleInfoCard(
                groupTitle = state.groupTitle,
                groupPart = state.groupPart,
            )

            Spacer(Modifier.height(28.dp))

            GroupScheduleSectionTitle(text = "스터디명")
            Spacer(Modifier.height(12.dp))

            GroupScheduleTextField(
                value = state.studyName,
                placeholder = "스터디명을 입력하세요.",
                onValueChange = {
                    onAction(AdminStudyGroupScheduleAction.OnStudyNameChanged(it))
                },
            )


            Spacer(Modifier.height(32.dp))

            GroupScheduleSectionTitle(text = "일시")
            Spacer(Modifier.height(16.dp))
            GroupScheduleDateTimeBox(
                isAllDay = state.isAllDay,
                startText = state.startDateTimeText,
                endText = state.endDateTimeText,
                onAllDayClick = { onAction(AdminStudyGroupScheduleAction.ToggleAllDay) },
                onStartClick = { onAction(AdminStudyGroupScheduleAction.ClickStartDateTime) },
                onEndClick = { onAction(AdminStudyGroupScheduleAction.ClickEndDateTime) },
            )

            Spacer(Modifier.height(32.dp))

            GroupScheduleSectionTitle(text = "장소")
            Spacer(Modifier.height(12.dp))
            GroupScheduleToggleRow(
                checked = state.isOffline,
                text = "대면으로 진행하기",
                onClick = { onAction(AdminStudyGroupScheduleAction.ToggleOffline) },
            )

            if (state.isOffline) {
                Spacer(Modifier.height(12.dp))
                GroupScheduleSelectRow(
                    text = state.placeText,
                    placeholder = "장소를 선택하세요",
                    enabled = true,
                    onClick = { onAction(AdminStudyGroupScheduleAction.ClickPlace) },
                )
            }

            Spacer(Modifier.height(32.dp))

            GroupScheduleSectionTitle(text = "상세 안내")
            Spacer(Modifier.height(12.dp))
            GroupScheduleMemoBox(
                value = state.detail,
                placeholder = "상세 내용을 입력하세요",
                onValueChange = {
                    onAction(AdminStudyGroupScheduleAction.OnDetailChanged(it))
                },
            )

            Spacer(Modifier.height(32.dp))

            GroupScheduleSectionTitle(text = "챌린저 명단")
            Spacer(Modifier.height(12.dp))
            GroupScheduleSelectRow(
                text = state.challengerText,
                placeholder = "챌린저를 선택하세요",
                onClick = { onAction(AdminStudyGroupScheduleAction.ClickChallenger) },
            )

            Spacer(Modifier.height(16.dp))

            GroupScheduleToggleRow(
                checked = state.createAttendance,
                text = "출석부 생성하기",
                onClick = { onAction(AdminStudyGroupScheduleAction.ToggleAttendance) },
            )

            Spacer(Modifier.height(12.dp))

            if (state.createAttendance) {
                GroupScheduleAttendanceBox(
                    checkInStartText = state.checkInStartText,
                    onTimeEndText = state.onTimeEndText,
                    lateEndText = state.lateEndText,
                    onCheckInStartClick = {
                        onAction(AdminStudyGroupScheduleAction.ClickCheckInStart)
                    },
                    onOnTimeEndClick = {
                        onAction(AdminStudyGroupScheduleAction.ClickOnTimeEnd)
                    },
                    onLateEndClick = {
                        onAction(AdminStudyGroupScheduleAction.ClickLateEnd)
                    },
                )
            }

            Spacer(Modifier.height(32.dp))

            GroupScheduleSectionTitle(text = "주차")
            Spacer(Modifier.height(12.dp))
            GroupScheduleSelectRow(
                text = state.weekText,
                placeholder = "주차를 선택하세요",
                onClick = { onAction(AdminStudyGroupScheduleAction.ClickWeek) },
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, widthDp = 390, heightDp = 844)
@Composable
private fun AdminStudyGroupScheduleScreenPreview() {
    AdminStudyGroupScheduleScreen(
        state = AdminStudyGroupScheduleState()
    )
}