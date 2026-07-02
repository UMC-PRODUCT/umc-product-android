package com.umc.presentation.home.schedule.add

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.umc.component.component.UChip
import com.umc.component.component.USwitch
import com.umc.component.component.UText
import com.umc.component.theme.AppStrings
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.grey000
import com.umc.component.theme.grey200
import com.umc.component.theme.grey300
import com.umc.component.theme.grey500
import com.umc.component.theme.grey600

/**일정 등록에서 일시 (하루 종일) or 시작/종료 날짜를 선택하는 섹션
 *
 * 하루 종일 + DateTimeRow(UChip 같이 존재)
 * **/
@Composable
fun ScheduleDateCard(
    uiState: ScheduleAddUiState,
    onAlldayChanged: (Boolean) -> Unit, //하루종일 선택 여부
    onStartDateClick: () -> Unit,
    onStartTimeClick: () -> Unit,
    onEndDateClick: () -> Unit,
    onEndTimeClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, grey300()),
        color = grey000()
    ) {
        Column {
            //하루 종일 파트
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                UText(text = AppStrings.ALLDAY, style = UmcTypographyTokens.Callout, color = grey600())
                USwitch(
                    checked = uiState.isAllDay,
                    onCheckedChange = onAlldayChanged,
                )
            }

            HorizontalDivider(color = grey300())

            // 시작 일시
            DateTimeRow(
                label = AppStrings.START,
                dateText = uiState.startDateText,
                timeText = uiState.startTimeText,
                showTime = !uiState.isAllDay,
                onDateClick = onStartDateClick,
                onTimeClick = onStartTimeClick
            )

            HorizontalDivider(color = grey300())

            // 종료 일시
            DateTimeRow(
                label = AppStrings.END,
                dateText = uiState.endDateText,
                timeText = uiState.endTimeText,
                showTime = !uiState.isAllDay,
                onDateClick = onEndDateClick,
                onTimeClick = onEndTimeClick
            )
        }
    }
}

/**시작(종료) 날짜와 시간을 UChip 형태로 제공하고 터치 시 Date/Time picker로 선택하는 함수**/
@Composable
fun DateTimeRow(
    label: String,
    dateText: String,
    timeText: String,
    showTime: Boolean,
    onDateClick: () -> Unit,
    onTimeClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        UText(text = label, style = UmcTypographyTokens.Callout, color = grey600())
        Spacer(modifier = Modifier
            .weight(1f)
        )
        UChip(text = dateText,
            onClick = onDateClick,
            backgroundColor = grey000(),
            borderColor = grey200(),
            borderWidth = 1.dp,
            textColor = grey500(),
            textStyle = UmcTypographyTokens.SubheadlineBold)
        if (showTime) {
            Spacer(modifier = Modifier
                .width(8.dp)
            )
            UChip(text = timeText,
                onClick = onTimeClick,
                backgroundColor = grey000(),
                borderColor = grey200(),
                borderWidth = 1.dp,
                textColor = grey500(),
                textStyle = UmcTypographyTokens.SubheadlineBold)
        }
    }
}