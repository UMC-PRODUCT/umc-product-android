package com.example.presentation.act.normal.attendance

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.umc.component.R
import com.umc.component.component.UButton
import com.umc.component.component.UText
import com.umc.component.theme.AppStrings
import com.umc.component.theme.UmcTheme
import com.umc.component.theme.UmcTypographyTokens.BodyBold
import com.umc.component.theme.UmcTypographyTokens.Callout
import com.umc.component.theme.UmcTypographyTokens.CalloutBold
import com.umc.component.theme.UmcTypographyTokens.Caption1
import com.umc.component.theme.UmcTypographyTokens.Caption1Bold
import com.umc.component.theme.UmcTypographyTokens.Footnote
import com.umc.component.theme.UmcTypographyTokens.FootnoteBold
import com.umc.component.theme.UmcTypographyTokens.HeadlineBold
import com.umc.component.theme.UmcTypographyTokens.Subheadline
import com.umc.component.theme.UmcTypographyTokens.Title3Bold
import com.umc.component.theme.red500
import com.umc.component.theme.indigo600
import com.umc.component.theme.grey000
import com.umc.component.theme.grey50
import com.umc.component.theme.grey100
import com.umc.component.theme.grey200
import com.umc.component.theme.grey300
import com.umc.component.theme.grey400
import com.umc.component.theme.grey500
import com.umc.component.theme.grey600
import com.umc.component.theme.grey800
import com.umc.component.theme.indigo100
import com.umc.component.theme.indigo500
import com.umc.component.theme.green100
import com.umc.component.theme.green500
import com.umc.component.theme.yellow100
import com.umc.component.theme.yellow500
import com.umc.component.theme.yellow700
import com.umc.component.theme.yellow900
import com.umc.domain.model.enums.CheckAvailableStatus
import com.umc.domain.model.enums.CheckHistoryStatus

@Composable
fun NormalAttendanceRoute(
    viewModel: NormalAttendanceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    NormalAttendanceScreen(
        uiState = uiState,
        onExpandToggle = viewModel::toggleSessionExpanded,
        onAttendanceClick = viewModel::requestAttendance,
        onReasonClick = viewModel::openReasonDialog,
        onReasonChange = viewModel::onReasonChanged,
        onDismissReason = viewModel::dismissReasonDialog,
        onSubmitReason = viewModel::submitReason
    )
}

@Composable
fun NormalAttendanceScreen(
    modifier: Modifier = Modifier,
    uiState: NormalAttendanceUiState = NormalAttendanceUiState(),
    onExpandToggle: (Long) -> Unit = {},
    onAttendanceClick: (NormalAvailableSessionUi) -> Unit = {},
    onReasonClick: (Long) -> Unit = {},
    onReasonChange: (String) -> Unit = {},
    onDismissReason: () -> Unit = {},
    onSubmitReason: () -> Unit = {},
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(grey100())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp)
    ) {
        item {
            AvailableSession(
                isEmpty = uiState.isAvailableEmpty,
                sessions = uiState.availableSessions,
                expandedSessionId = uiState.expandedSessionId,
                onExpandToggle = onExpandToggle,
                onAttendanceClick = onAttendanceClick,
                onReasonClick = onReasonClick
            )
        }

        item {
            MyAttendance(
                isEmpty = uiState.isHistoryEmpty,
                sessions = uiState.historySessions
            )
        }
    }

    if (uiState.reasonSessionId != null) {
        AttendanceReasonDialog(
            reason = uiState.reason,
            onReasonChange = onReasonChange,
            onDismissRequest = onDismissReason,
            onSubmit = { onSubmitReason() }
        )
    }
}

@Composable
private fun AvailableSession(
    isEmpty: Boolean,
    sessions: List<NormalAvailableSessionUi>,
    expandedSessionId: Long?,
    onExpandToggle: (Long) -> Unit,
    onAttendanceClick: (NormalAvailableSessionUi) -> Unit,
    onReasonClick: (Long) -> Unit,
) {
    Column {
        Text(
            text = AppStrings.ATTENDANCE_HEADER_AVAILABLE,
            color = grey800(),
            style = Title3Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (isEmpty) {
            EmptyComponent(
                painter = painterResource(R.drawable.ic_people),
                text = AppStrings.ATTENDANCE_EMPTY_AVAILABLE
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                sessions.forEach { session ->
                    AvailableSessionCard(
                        session = session,
                        isExpanded = expandedSessionId == session.id,
                        onExpandToggle = { onExpandToggle(session.id) },
                        onAttendanceClick = { onAttendanceClick(session) },
                        onReasonClick = { onReasonClick(session.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MyAttendance(
    isEmpty: Boolean,
    sessions: List<NormalHistorySessionUi>
) {
    Column {
        Text(
            text = AppStrings.ATTENDANCE_HEADER_HISTORY,
            color = grey800(),
            style = Title3Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (isEmpty) {
            EmptyComponent(
                painter = painterResource(R.drawable.ic_checkbook),
                text = AppStrings.ATTENDANCE_EMPTY_HISTORY
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(grey000(), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Column {
                    sessions.forEachIndexed { index, session ->
                        HistorySessionRow(session = session)
                        if (index != sessions.lastIndex) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(grey200())
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AvailableSessionCard(
    session: NormalAvailableSessionUi,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    onAttendanceClick: () -> Unit,
    onReasonClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(grey000(), RoundedCornerShape(12.dp))
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SessionIcon()

            Spacer(modifier = Modifier.size(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.title,
                    color = grey800(),
                    style = Subheadline
                )

                Spacer(modifier = Modifier.height(2.dp))

                SessionTimeText(timeRange = session.timeRange)
            }

            StatusChip(
                text = session.status.text,
                background = when (session.status) {
                    CheckAvailableStatus.BEFORE -> grey50()
                    CheckAvailableStatus.PENDING -> yellow100()
                    CheckAvailableStatus.COMPLETED -> green100()
                },
                textColor = when (session.status) {
                    CheckAvailableStatus.BEFORE -> grey600()
                    CheckAvailableStatus.PENDING -> yellow500()
                    CheckAvailableStatus.COMPLETED -> green500()
                }
            )

            Spacer(modifier = Modifier.size(8.dp))

            Icon(
                painter = painterResource(
                    id = if (isExpanded) R.drawable.ic_dropdown_up else R.drawable.ic_dropdown_down
                ),
                contentDescription = null,
                tint = grey400(),
                modifier = Modifier
                    .size(18.dp)
                    .clickable(onClick = onExpandToggle)
            )
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            AvailableSessionExpandedContent(
                session = session,
                onAttendanceClick = onAttendanceClick,
                onReasonClick = onReasonClick
            )
        }
    }
}

@Composable
private fun AvailableSessionExpandedContent(
    session: NormalAvailableSessionUi,
    onAttendanceClick: () -> Unit,
    onReasonClick: () -> Unit,
) {
    Column() {
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(grey200())
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_location_marker),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.Center)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(horizontal = 6.dp)
                    .padding(bottom = 6.dp)
                    .shadow(
                        elevation = 6.dp,
                        shape = RoundedCornerShape(4.dp),
                        clip = false
                    )
                    .clip(RoundedCornerShape(4.dp))
                    .background(grey000())
                    .align(Alignment.BottomCenter)

            ) {
                if(session.isLocationCertified) {
                    CanCheckLocation(session)
                } else {
                    CantCheckLocation(session)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when(session.status) {
            CheckAvailableStatus.BEFORE -> {
                UButton(
                    modifier = Modifier
                        .fillMaxWidth(),
                    enabled = session.isLocationCertified,
                    cornerRadius = 8.dp,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 13.dp),
                    backgroundColor = if(session.isLocationCertified) indigo500() else grey100(),
                    text = AppStrings.ATTENDANCE_REQUEST_BUTTON,
                    textStyle = CalloutBold,
                    textColor = if(session.isLocationCertified) grey000() else grey300(),
                    prevIcon = painterResource(R.drawable.ic_location_white),
                    prevIconTint = if(session.isLocationCertified) grey000() else grey300(),
                    prevIconSize = DpSize(20.dp, 20.dp),
                    onClick = onAttendanceClick
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    UText(
                        text = AppStrings.ATTENDANCE_FAIL_REASON_QUESTION,
                        color = grey600(),
                        style = Footnote
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    UText(
                        text = AppStrings.ATTENDANCE_FAIL_REASON_ACTION,
                        color = indigo600(),
                        style = Footnote,
                        modifier = Modifier.clickable(
                            onClick = onReasonClick
                        )
                    )
                }
            }

            CheckAvailableStatus.PENDING -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(yellow100()),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 50.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            modifier = Modifier.size(32.dp),
                            painter = painterResource(R.drawable.ic_hourglass),
                            contentDescription = null,
                            tint = Color.Unspecified
                        )
                        UText(
                            text = AppStrings.ATTENDANCE_STATUS_PENDING_TITLE,
                            style = HeadlineBold,
                            color = yellow900()
                        )
                        UText(
                            text = AppStrings.ATTENDANCE_STATUS_PENDING_DESCRIPTION,
                            style = Footnote,
                            color = yellow700()
                        )
                    }
                }
            }

            CheckAvailableStatus.COMPLETED -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(green100()),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 50.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            modifier = Modifier.size(32.dp),
                            painter = painterResource(R.drawable.ic_check_success),
                            contentDescription = null,
                            tint = Color.Unspecified
                        )
                        UText(
                            text = AppStrings.ATTENDANCE_STATUS_COMPLETED_DESCRIPTION,
                            style = FootnoteBold,
                            color = green500()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CanCheckLocation(
    session: NormalAvailableSessionUi
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_check_success),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        UText(
            text = AppStrings.LOCATION_CERTIFIED,
            style = Caption1Bold,
            color = green500()
        )

        Spacer(modifier = Modifier.weight(1f))

        UText(
            text = session.address,
            style = Caption1,
            color = grey600()
        )
    }
}

@Composable
private fun CantCheckLocation(
    session: NormalAvailableSessionUi
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_check_failed),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        UText(
            text = AppStrings.LOCATION_CERTIFICATION_FAILED,
            style = Caption1Bold,
            color = red500()
        )

        Spacer(modifier = Modifier.weight(1f))

        UText(
            text = session.address,
            style = Caption1,
            color = grey600()
        )
    }
}

@Composable
private fun HistorySessionRow(session: NormalHistorySessionUi) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SessionIcon()

        Spacer(modifier = Modifier.size(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = session.title,
                color = grey800(),
                style = BodyBold
            )

            Spacer(modifier = Modifier.height(2.dp))

            SessionTimeText(timeRange = session.timeRange)
        }

        StatusChip(
            text = session.status.text,
            background = when (session.status) {
                CheckHistoryStatus.PRESENT -> green500()
                CheckHistoryStatus.LATE -> yellow500()
                CheckHistoryStatus.ABSENT -> red500()
            },
            textColor = grey000()
        )
    }
}

@Composable
private fun SessionIcon() {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(indigo100(), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_people_color),
            contentDescription = null,
            tint = indigo500(),
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun SessionTimeText(timeRange: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(id = R.drawable.ic_clock),
            contentDescription = null,
            tint = grey500(),
            modifier = Modifier.size(14.dp)
        )

        Spacer(modifier = Modifier.size(4.dp))

        Text(
            text = timeRange,
            color = grey500(),
            style = Footnote
        )
    }
}

@Composable
private fun StatusChip(
    text: String,
    background: Color,
    textColor: Color
) {
    Box(
        modifier = Modifier
            .background(background, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            style = Caption1
        )
    }
}

@Composable
fun EmptyComponent(
    painter: Painter,
    text: String
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color = grey000())
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painter,
                    contentDescription = null,
                    tint = grey400(),
                    modifier = Modifier.size(32.dp)
                )
            }

            Text(
                text = text,
                color = grey600(),
                style = Callout
            )
        }
    }
}

private fun sampleAvailableSessions(): List<NormalAvailableSessionUi> = listOf(
    NormalAvailableSessionUi(
        id = 1L,
        sheetId = 1L,
        title = "정기 세션 3주차",
        timeRange = "14:00 - 18:00",
        status = CheckAvailableStatus.BEFORE,
        isLocationCertified = false,
        address = "서울 강남구 역삼동 마루 18",
        latitude = null,
        longitude = null
    ),
    NormalAvailableSessionUi(
        id = 2L,
        sheetId = 2L,
        title = "정기 세션 3주차",
        timeRange = "14:00 - 18:00",
        status = CheckAvailableStatus.COMPLETED,
        isLocationCertified = true,
        address = "서울 강남구 역삼동 마루 18",
        latitude = null,
        longitude = null
    ),
    NormalAvailableSessionUi(
        id = 3L,
        sheetId = 3L,
        title = "정기 세션 3주차",
        timeRange = "14:00 - 18:00",
        status = CheckAvailableStatus.PENDING,
        isLocationCertified = true,
        address = "서울 강남구 역삼동 마루 18",
        latitude = null,
        longitude = null
    )
)

private fun sampleHistorySessions(): List<NormalHistorySessionUi> = listOf(
    NormalHistorySessionUi(
        id = 1L,
        title = "정기 세션",
        timeRange = "14:00 - 18:00",
        status = CheckHistoryStatus.PRESENT
    ),
    NormalHistorySessionUi(
        id = 2L,
        title = "정기 세션",
        timeRange = "14:00 - 18:00",
        status = CheckHistoryStatus.LATE
    ),
    NormalHistorySessionUi(
        id = 3L,
        title = "정기 세션",
        timeRange = "14:00 - 18:00",
        status = CheckHistoryStatus.ABSENT
    )
)

@Preview(showBackground = true)
@Composable
private fun NormalAttendanceEmptyScreenPreview() {
    UmcTheme(darkTheme = false) {
        NormalAttendanceScreen()
    }
}


@Preview(showBackground = true)
@Composable
private fun NormalAttendanceScreenPreview() {
    UmcTheme(darkTheme = false) {
        NormalAttendanceScreen(
            uiState = NormalAttendanceUiState(
                availableSessions = sampleAvailableSessions(),
                historySessions = sampleHistorySessions()
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AvailableSessionCardCanCheckPreview() {
    UmcTheme(darkTheme = false) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(grey100())
                .padding(16.dp)
        ) {
            AvailableSessionCard(
                session = sampleAvailableSessions().first(),
                isExpanded = true,
                onExpandToggle = {},
                onAttendanceClick = {},
                onReasonClick = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Available Session Expanded")
@Composable
private fun AvailableSessionCardCantCheckPreview() {
    UmcTheme(darkTheme = false) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(grey100())
                .padding(16.dp)
        ) {
            AvailableSessionCard(
                session = sampleAvailableSessions().first(),
                isExpanded = true,
                onExpandToggle = {},
                onAttendanceClick = {},
                onReasonClick = {}
            )
        }
    }
}
