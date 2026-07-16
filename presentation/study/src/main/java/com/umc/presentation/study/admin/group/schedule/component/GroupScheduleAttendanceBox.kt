package com.umc.presentation.study.admin.group.schedule.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.umc.component.R
import com.umc.component.component.UText
import com.umc.component.theme.*
import com.umc.component.theme.UmcTypographyTokens.Body
import com.umc.component.theme.UmcTypographyTokens.HeadlineBold

@Composable
fun GroupScheduleAttendanceBox(
    checkInStartText: String?,
    onTimeEndText: String?,
    lateEndText: String?,
    onCheckInStartClick: () -> Unit,
    onOnTimeEndClick: () -> Unit,
    onLateEndClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, grey300(), RoundedCornerShape(12.dp))
            .background(grey000()),
    ) {
        AttendanceRow(
            title = "체크인 시작",
            value = checkInStartText,
            onClick = onCheckInStartClick,
        )

        DividerLine()

        AttendanceRow(
            title = "정시 종료",
            value = onTimeEndText,
            onClick = onOnTimeEndClick,
        )

        DividerLine()

        AttendanceRow(
            title = "지각 종료",
            value = lateEndText,
            onClick = onLateEndClick,
        )
    }
}

@Composable
private fun AttendanceRow(
    title: String,
    value: String?,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        UText(
            text = title,
            style = Body,
            color = grey600(),
            modifier = Modifier.weight(1f),
        )

        if (value == null) {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_next),
                contentDescription = null,
                tint = grey500(),
                modifier = Modifier.size(22.dp),
            )
        } else {
            Box(
                modifier = Modifier
                    .background(indigo100(), RoundedCornerShape(999.dp))
                    .padding(horizontal = 14.dp, vertical = 7.dp),
            ) {
                UText(
                    text = value,
                    style = HeadlineBold,
                    color = indigo500(),
                )
            }
        }
    }
}

@Composable
private fun DividerLine() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(grey200()),
    )
}