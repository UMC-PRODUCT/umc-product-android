package com.example.presentation.act.admin.attendance

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.umc.component.theme.UmcTypographyTokens.CalloutBold
import com.umc.component.theme.UmcTypographyTokens.Caption1Bold
import com.umc.component.theme.UmcTypographyTokens.Caption2Bold
import com.umc.component.theme.UmcTypographyTokens.Footnote
import com.umc.component.theme.UmcTypographyTokens.HeadlineBold
import com.umc.component.theme.UmcTypographyTokens.Subheadline
import com.umc.component.theme.UmcTypographyTokens.SubheadlineBold
import com.umc.component.theme.black
import com.umc.component.theme.grey000
import com.umc.component.theme.grey100
import com.umc.component.theme.grey200
import com.umc.component.theme.grey300
import com.umc.component.theme.grey400
import com.umc.component.theme.grey50
import com.umc.component.theme.grey600
import com.umc.component.theme.grey700
import com.umc.component.theme.grey800
import com.umc.component.theme.indigo100
import com.umc.component.theme.indigo500
import com.umc.component.theme.red100
import com.umc.component.theme.red500
import com.umc.domain.model.act.check.AdminPendingUser
import com.umc.domain.model.act.check.AdminSessionCheck
import com.umc.domain.model.enums.AdminSessionStatus

@Composable
fun AttendanceRoute(
    viewModel: AdminAttendanceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AttendanceScreen(
        uiState = uiState
    )
}

@Composable
fun AttendanceScreen(
    uiState: AdminAttendanceUiState,
    modifier: Modifier = Modifier
) {
    if (uiState.isEmpty) {
        EmptyScreen()
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(grey100())
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items = uiState.sessions, key = { it.id }) { session ->
            AdminSessionCard(
                session = session,
                onChangeLocationClick = {},
                onPendingListClick = {},
                onDeleteClick = {},
            )
        }
    }
}

@Composable
fun AdminSessionCard(
    session: AdminSessionCheck,
    onChangeLocationClick: () -> Unit = {},
    onPendingListClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = grey000()),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UText(
                        text = session.title,
                        style = HeadlineBold,
                        color = grey800()
                    )
                    Surface(
                        color = if (session.status == AdminSessionStatus.IN_PROGRESS) {
                            indigo100()
                        } else {
                            grey50()
                        },
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        UText(
                            text = session.status.text,
                            style = Caption2Bold,
                            color = if (session.status == AdminSessionStatus.IN_PROGRESS) {
                                indigo500()
                            } else {
                                grey600()
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UButton(
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        text = AppStrings.ADMIN_CHECK_CHANGE_LOCATION,
                        textStyle = Caption1Bold,
                        textColor = grey700(),
                        prevIcon = painterResource(id = R.drawable.ic_location),
                        prevIconTint = grey700(),
                        prevIconMargin = 6.dp,
                        backgroundColor = grey100(),
                        onClick = onChangeLocationClick,
                        prevIconSize = DpSize(16.dp, 16.dp)
                    )

                    Surface(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(32.dp),
                        color = red100(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_trash_can),
                                contentDescription =
                                    AppStrings.ADMIN_CHECK_DELETE_SESSION_CONTENT_DESCRIPTION,
                                tint = red500(),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CardMetaItem(
                    iconRes = R.drawable.ic_calendar,
                    text = session.date
                )
                CardMetaItem(
                    iconRes = R.drawable.ic_clock,
                    text = AppStrings.ADMIN_CHECK_TIME_RANGE_FORMAT.format(
                        session.startTime,
                        session.endTime
                    )
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(74.dp)
                    .border(
                        width = 1.dp,
                        color = grey200(),
                        shape = RoundedCornerShape(12.dp)
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatItem(
                    modifier = Modifier.weight(1f),
                    iconRes = R.drawable.ic_checkbox_outline,
                    label = AppStrings.ADMIN_CHECK_STATS_ATTENDANCE_RATE,
                    highlightedValue = session.attendanceRate.toString(),
                    trailingValue = AppStrings.ADMIN_CHECK_STATS_RATE_UNIT
                )
                VerticalDivider()
                StatItem(
                    modifier = Modifier.weight(1f),
                    iconRes = R.drawable.ic_person_outline,
                    label = AppStrings.ADMIN_CHECK_STATS_ATTENDANCE_COUNT,
                    highlightedValue = session.attendedChallengers.toString(),
                    trailingValue = AppStrings.ADMIN_CHECK_STATS_TOTAL_COUNT_FORMAT.format(
                        session.totalChallengers
                    )
                )
                VerticalDivider()
                StatItem(
                    modifier = Modifier.weight(1f),
                    iconRes = R.drawable.ic_hourglass,
                    label = AppStrings.ADMIN_CHECK_STATS_PENDING_COUNT,
                    highlightedValue = session.pendingCount.toString(),
                    trailingValue = AppStrings.ADMIN_CHECK_STATS_COUNT_UNIT
                )
            }

            if (session.status == AdminSessionStatus.IN_PROGRESS) {
                CheckAttendanceListButton(onPendingListClick)
            } else {
                SuccessCheckAllAttendanceButton()
            }
        }
    }
}

@Composable
private fun EmptyScreen() {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp),
                contentAlignment = Alignment.Center
            ){
                Icon(
                    painter = painterResource(id = R.drawable.ic_people),
                    contentDescription = null,
                    tint = grey400(),
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            UText(
                text = AppStrings.ATTENDANCE_EMPTY_ADMIN_SESSIONS,
                style = Subheadline,
                color = grey600()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyScreenPreview() {
    UmcTheme(darkTheme = false) {
        EmptyScreen()
    }
}

@Composable
private fun CheckAttendanceListButton(
    onPendingListClick :() -> Unit
) {
    UButton(
        modifier = Modifier.fillMaxWidth(),
        prevIcon = painterResource(id = R.drawable.ic_person),
        prevIconTint = indigo500(),
        text = AppStrings.ADMIN_CHECK_PENDING_LIST_TRIGGER,
        textColor = black(),
        textStyle = CalloutBold,
        endIcon = painterResource(id = R.drawable.ic_arrow_next),
        endIconTint = grey400(),
        endIconSize = DpSize(7.dp, 12.dp),
        prevIconSize = DpSize(20.dp, 20.dp),
        prevIconMargin = 8.dp,
        backgroundColor = grey50(),
        pressedColor = grey100(),
        onClick = onPendingListClick,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
    )
}

@Composable
private fun SuccessCheckAllAttendanceButton() {
    UButton(
        modifier = Modifier.fillMaxWidth(),
        prevIcon = painterResource(id = R.drawable.ic_check_success),
        text = AppStrings.ADMIN_CHECK_COMPLETED_MESSAGE,
        enabled = false,
        prevIconSize = DpSize(18.dp, 18.dp),
        prevIconMargin = 8.dp,
        textColor = grey600(),
        textStyle = SubheadlineBold,
        backgroundColor = grey000(),
        borderColor = grey200(),
        borderWidth = 1.dp,
        onClick = {},
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
    )
}

@Composable
private fun CardMetaItem(
    iconRes: Int,
    text: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = grey600(),
            modifier = Modifier.size(14.dp)
        )
        UText(
            text = text,
            style = Footnote,
            color = grey600()
        )
    }
}

@Composable
private fun StatItem(
    modifier: Modifier = Modifier,
    iconRes: Int,
    label: String,
    highlightedValue: String,
    trailingValue: String
) {
    Column(
        modifier = modifier.padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = grey600(),
            modifier = Modifier.size(20.dp)
        )
        UText(
            text = label,
            style = Subheadline,
            color = grey600()
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            UText(
                text = highlightedValue,
                style = SubheadlineBold,
                color = indigo500()
            )
            UText(
                text = trailingValue,
                style = SubheadlineBold,
                color = grey800()
            )
        }
    }
}

@Composable
private fun VerticalDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .fillMaxHeight()
            .padding(vertical = 12.dp)
            .background(grey200())
    )
}

private fun sampleSessions(): List<AdminSessionCheck> = listOf(
    AdminSessionCheck(
        id = 1L,
        title = AppStrings.ADMIN_CHECK_PREVIEW_SESSION_TITLE,
        date = AppStrings.ADMIN_CHECK_PREVIEW_SESSION_DATE,
        startTime = AppStrings.ADMIN_CHECK_PREVIEW_START_TIME,
        endTime = AppStrings.ADMIN_CHECK_PREVIEW_END_TIME,
        status = AdminSessionStatus.IN_PROGRESS,
        attendanceRate = 85,
        totalChallengers = 40,
        attendedChallengers = 34,
        pendingCount = 3,
        pendingUsers = emptyList<AdminPendingUser>(),
        sheetId = null
    ),
    AdminSessionCheck(
        id = 2L,
        title = AppStrings.ADMIN_CHECK_PREVIEW_SESSION_TITLE,
        date = AppStrings.ADMIN_CHECK_PREVIEW_SESSION_DATE,
        startTime = AppStrings.ADMIN_CHECK_PREVIEW_START_TIME,
        endTime = AppStrings.ADMIN_CHECK_PREVIEW_END_TIME,
        status = AdminSessionStatus.COMPLETED,
        attendanceRate = 85,
        totalChallengers = 40,
        attendedChallengers = 34,
        pendingCount = 3,
        pendingUsers = emptyList<AdminPendingUser>(),
        sheetId = null
    )
)

@Preview(showBackground = true, widthDp = 324, heightDp = 477)
@Composable
private fun AttendanceScreenPreview() {
    UmcTheme(darkTheme = false) {
        AttendanceScreen(
            uiState = AdminAttendanceUiState(sessions = sampleSessions())
        )
    }
}

