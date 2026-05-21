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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.umc.component.R
import com.umc.component.component.UButton
import com.umc.component.component.UText
import com.umc.component.theme.AppStrings
import com.umc.component.theme.UmcTheme
import com.umc.component.theme.UmcTypographyTokens.Callout
import com.umc.component.theme.UmcTypographyTokens.CalloutBold
import com.umc.component.theme.UmcTypographyTokens.Caption1
import com.umc.component.theme.UmcTypographyTokens.Caption1Bold
import com.umc.component.theme.UmcTypographyTokens.Footnote
import com.umc.component.theme.UmcTypographyTokens.Subheadline
import com.umc.component.theme.UmcTypographyTokens.Title3Bold
import com.umc.component.theme.danger500
import com.umc.component.theme.indigo600
import com.umc.component.theme.neutral000
import com.umc.component.theme.neutral100
import com.umc.component.theme.neutral200
import com.umc.component.theme.neutral400
import com.umc.component.theme.neutral500
import com.umc.component.theme.neutral600
import com.umc.component.theme.neutral700
import com.umc.component.theme.neutral800
import com.umc.component.theme.primary100
import com.umc.component.theme.primary500
import com.umc.component.theme.success500
import com.umc.component.theme.warning500

private data class AvailableSessionItem(
    val id: Long,
    val title: String,
    val timeRange: String,
    val statusText: String,
    val isLocationCertified: Boolean,
    val address: String
)

private data class HistorySessionItem(
    val id: Long,
    val title: String,
    val timeRange: String,
    val status: AttendanceState
)

private enum class AttendanceState(val label: String) {
    ATTEND("출석"),
    LATE("지각"),
    ABSENT("결석")
}

@Composable
fun NormalAttendanceRoute() {
    NormalAttendanceScreen()
}

@Composable
fun NormalAttendanceScreen(
    modifier: Modifier = Modifier,
    isEmpty: Boolean = false
) {
    val availableSessions = sampleAvailableSessions()
    val historySessions = sampleHistorySessions()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(neutral100())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp)
    ) {
        item {
            AvailableSession(
                isEmpty = isEmpty,
                sessions = availableSessions
            )
        }

        item {
            MyAttendance(
                isEmpty = isEmpty,
                sessions = historySessions
            )
        }
    }
}

@Composable
private fun AvailableSession(
    isEmpty: Boolean,
    sessions: List<AvailableSessionItem>
) {
    var expandedSessionId by rememberSaveable { mutableStateOf<Long?>(null) }

    Column {
        Text(
            text = AppStrings.ATTENDANCE_HEADER_AVAILABLE,
            color = neutral800(),
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
                        onExpandToggle = {
                            expandedSessionId =
                                if (expandedSessionId == session.id) null else session.id
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MyAttendance(
    isEmpty: Boolean,
    sessions: List<HistorySessionItem>
) {
    Column {
        Text(
            text = AppStrings.ATTENDANCE_HEADER_HISTORY,
            color = neutral800(),
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
                    .background(neutral000(), RoundedCornerShape(12.dp))
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
                                    .background(neutral200())
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
    session: AvailableSessionItem,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(neutral000(), RoundedCornerShape(12.dp))
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SessionIcon()

            Spacer(modifier = Modifier.size(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.title,
                    color = neutral800(),
                    style = Subheadline
                )

                Spacer(modifier = Modifier.height(2.dp))

                SessionTimeText(timeRange = session.timeRange)
            }

            StatusChip(
                text = session.statusText,
                background = neutral100(),
                textColor = neutral700()
            )

            Spacer(modifier = Modifier.size(8.dp))

            Icon(
                painter = painterResource(
                    id = if (isExpanded) R.drawable.ic_dropdown_up else R.drawable.ic_dropdown_down
                ),
                contentDescription = null,
                tint = neutral400(),
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
            AvailableSessionExpandedContent(session = session)
        }
    }
}

@Composable
private fun AvailableSessionExpandedContent(session: AvailableSessionItem) {
    Column() {
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(neutral200())
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_location_marker),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier
                    .size(52.dp)
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
                    .background(neutral000())
                    .align(Alignment.BottomCenter)

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
                        color = success500()
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    UText(
                        text = session.address,
                        style = Caption1,
                        color = neutral600()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        UButton(
            modifier = Modifier
                .fillMaxWidth(),
            cornerRadius = 8.dp,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 13.dp),
            backgroundColor = primary500(),
            text = AppStrings.ATTENDANCE_REQUEST_BUTTON,
            textStyle = CalloutBold,
            prevIcon = painterResource(R.drawable.ic_location_white),
            prevIconSize = DpSize(20.dp, 20.dp),
            onClick = {}
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            UText(
                text = AppStrings.ATTENDANCE_FAIL_REASON_QUESTION,
                color = neutral600(),
                style = Footnote
            )
            Spacer(modifier = Modifier.width(3.dp))
            UText(
                text = AppStrings.ATTENDANCE_FAIL_REASON_ACTION,
                color = indigo600(),
                style = Footnote,
                modifier = Modifier.clickable(
                 onClick = { }
                )
            )
        }
    }

}

@Composable
private fun HistorySessionRow(session: HistorySessionItem) {
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
                color = neutral800(),
                style = Subheadline
            )

            Spacer(modifier = Modifier.height(2.dp))

            SessionTimeText(timeRange = session.timeRange)
        }

        StatusChip(
            text = session.status.label,
            background = when (session.status) {
                AttendanceState.ATTEND -> success500()
                AttendanceState.LATE -> warning500()
                AttendanceState.ABSENT -> danger500()
            },
            textColor = neutral000()
        )
    }
}

@Composable
private fun SessionIcon() {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(primary100(), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_people),
            contentDescription = null,
            tint = primary500(),
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
            tint = neutral500(),
            modifier = Modifier.size(14.dp)
        )

        Spacer(modifier = Modifier.size(4.dp))

        Text(
            text = timeRange,
            color = neutral500(),
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
            .background(color = neutral000(), shape = RoundedCornerShape(12.dp))
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painter,
                    contentDescription = null,
                    tint = neutral400(),
                    modifier = Modifier.size(32.dp)
                )
            }

            Text(
                text = text,
                color = neutral600(),
                style = Callout
            )
        }
    }
}

private fun sampleAvailableSessions(): List<AvailableSessionItem> = listOf(
    AvailableSessionItem(
        id = 1L,
        title = "정기 세션 3주차",
        timeRange = "14:00 - 18:00",
        statusText = "출석 전",
        isLocationCertified = true,
        address = "서울 강남구 역삼동 마루 18"
    ),
    AvailableSessionItem(
        id = 2L,
        title = "정기 세션 3주차",
        timeRange = "14:00 - 18:00",
        statusText = "출석 전",
        isLocationCertified = false,
        address = "서울 강남구 역삼동 마루 18"
    )
)

private fun sampleHistorySessions(): List<HistorySessionItem> = listOf(
    HistorySessionItem(
        id = 1L,
        title = "정기 세션",
        timeRange = "14:00 - 18:00",
        status = AttendanceState.ATTEND
    ),
    HistorySessionItem(
        id = 2L,
        title = "정기 세션",
        timeRange = "14:00 - 18:00",
        status = AttendanceState.LATE
    ),
    HistorySessionItem(
        id = 3L,
        title = "정기 세션",
        timeRange = "14:00 - 18:00",
        status = AttendanceState.ABSENT
    )
)

@Preview(showBackground = true)
@Composable
private fun NormalAttendanceEmptyScreenPreview() {
    UmcTheme(darkTheme = false) {
        NormalAttendanceScreen(isEmpty = true)
    }
}


@Preview(showBackground = true)
@Composable
private fun NormalAttendanceScreenPreview() {
    UmcTheme(darkTheme = false) {
        NormalAttendanceScreen(isEmpty = false)
    }
}

@Preview(showBackground = true, name = "Available Session Expanded")
@Composable
private fun AvailableSessionCardExpandedPreview() {
    UmcTheme(darkTheme = false) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(neutral100())
                .padding(16.dp)
        ) {
            AvailableSessionCard(
                session = sampleAvailableSessions().first(),
                isExpanded = true,
                onExpandToggle = {}
            )
        }
    }
}
