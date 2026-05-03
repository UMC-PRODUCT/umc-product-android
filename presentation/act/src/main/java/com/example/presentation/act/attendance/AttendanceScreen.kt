package com.example.presentation.act.attendance

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.umc.component.R
import com.umc.component.component.UText
import com.umc.component.theme.UmcTheme
import com.umc.component.theme.UmcTypographyTokens.BodyBold
import com.umc.component.theme.UmcTypographyTokens.Caption1
import com.umc.component.theme.UmcTypographyTokens.Caption1Bold
import com.umc.component.theme.UmcTypographyTokens.Subheadline
import com.umc.component.theme.UmcTypographyTokens.SubheadlineBold
import com.umc.component.theme.UmcTypographyTokens.Title3Bold
import com.umc.component.theme.neutral000
import com.umc.component.theme.neutral100
import com.umc.component.theme.neutral200
import com.umc.component.theme.neutral300
import com.umc.component.theme.neutral600
import com.umc.component.theme.neutral800
import com.umc.component.theme.primary100
import com.umc.component.theme.primary500
import com.umc.domain.model.act.check.AdminPendingUser
import com.umc.domain.model.act.check.AdminSessionCheck
import com.umc.domain.model.enums.AdminSessionStatus

@Composable
fun AttendanceRoute() {
    AttendanceScreen(
        sessions = sampleSessions()
    )
}

@Composable
fun AttendanceScreen(
    sessions: List<AdminSessionCheck>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .background(neutral100())
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items = sessions, key = { it.id }) { session ->
            AdminSessionCard(
                session = session,
                onChangeLocationClick = {},
                onPendingListClick = {}
            )
        }
    }
}

@Composable
fun AdminSessionCard(
    session: AdminSessionCheck,
    onChangeLocationClick: () -> Unit = {},
    onPendingListClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = neutral000()),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UText(
                        text = session.title,
                        style = Title3Bold,
                        color = neutral800()
                    )
                    if (session.status == AdminSessionStatus.IN_PROGRESS) {
                        Surface(
                            color = primary100(),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            UText(
                                text = session.status.toString(),
                                style = Caption1Bold,
                                color = primary500(),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            color = neutral200(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable(onClick = onChangeLocationClick)
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_location),
                        contentDescription = null,
                        tint = neutral600(),
                        modifier = Modifier.size(14.dp)
                    )
                    UText(
                        text = "위치 변경",
                        style = SubheadlineBold,
                        color = neutral600()
                    )
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
                    text = "${session.startTime} - ${session.endTime}"
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .border(
                        width = 1.dp,
                        color = neutral300(),
                        shape = RoundedCornerShape(12.dp)
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatItem(
                    modifier = Modifier.weight(1f),
                    iconRes = R.drawable.ic_checklist,
                    label = "출석률",
                    value = "${session.attendanceRate}(%)"
                )
                VerticalDivider()
                StatItem(
                    modifier = Modifier.weight(1f),
                    iconRes = R.drawable.ic_person_outline,
                    label = "출석 인원",
                    value = "${session.attendedChallengers}/${session.totalChallengers}(명)"
                )
                VerticalDivider()
                StatItem(
                    modifier = Modifier.weight(1f),
                    iconRes = R.drawable.ic_hourglass,
                    label = "승인 대기",
                    value = "${session.pendingCount}(명)"
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = primary500(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable(onClick = onPendingListClick)
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_person),
                        contentDescription = null,
                        tint = primary500(),
                        modifier = Modifier.size(18.dp)
                    )
                    UText(
                        text = "승인 대기 명단 확인하기",
                        style = BodyBold,
                        color = neutral800()
                    )
                }
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_next),
                    contentDescription = null,
                    tint = primary500(),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
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
            tint = neutral600(),
            modifier = Modifier.size(14.dp)
        )
        UText(
            text = text,
            style = Caption1,
            color = neutral600()
        )
    }
}

@Composable
private fun StatItem(
    modifier: Modifier = Modifier,
    iconRes: Int,
    label: String,
    value: String
) {
    Column(
        modifier = modifier.padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = neutral600(),
            modifier = Modifier.size(18.dp)
        )
        UText(
            text = label,
            style = Subheadline,
            color = neutral600()
        )
        UText(
            text = value,
            style = SubheadlineBold,
            color = neutral800()
        )
    }
}

@Composable
private fun VerticalDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .fillMaxHeight()
            .padding(vertical = 12.dp)
            .background(neutral200())
    )
}

private fun sampleSessions(): List<AdminSessionCheck> = listOf(
    AdminSessionCheck(
        id = 1L,
        title = "4주차 정기세션",
        date = "2026-04-30",
        startTime = "14:00",
        endTime = "18:00",
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
        title = "3주차 정기세션",
        date = "2026-04-23",
        startTime = "14:00",
        endTime = "18:00",
        status = AdminSessionStatus.COMPLETED,
        attendanceRate = 92,
        totalChallengers = 40,
        attendedChallengers = 37,
        pendingCount = 0,
        pendingUsers = emptyList<AdminPendingUser>(),
        sheetId = null
    )
)

@Preview(showBackground = true)
@Composable
private fun AttendanceScreenPreview() {
    UmcTheme(darkTheme = false) {
        AttendanceScreen(
            sessions = sampleSessions()
        )
    }
}

