package com.umc.presentation.home.schedule.add

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.umc.component.R
import com.umc.component.component.UChip
import com.umc.component.component.USwitch
import com.umc.component.component.UText
import com.umc.component.theme.AppStrings
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.neutral000
import com.umc.component.theme.neutral200
import com.umc.component.theme.neutral300
import com.umc.component.theme.neutral400
import com.umc.component.theme.neutral500
import com.umc.component.theme.neutral600
import com.umc.component.theme.primary100
import com.umc.component.theme.primary500

/**일정 등록에서 일시 (하루 종일) or 시작/종료 날짜를 선택하는 섹션
 *
 * 하루 종일 + DateTimeRow(UChip 같이 존재)
 * **/
@Composable
fun ScheduleDateCard(
    uiState: ScheduleAddUiState,
    onAlldayChanged: (Boolean) -> Unit, //하루종일 선택 여부
    onStartDateTimeClick: () -> Unit,
    //onStartTimeClick: () -> Unit,
    onEndDateTimeClick: () -> Unit,
    //onEndTimeClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, neutral300()),
        color = neutral000()
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
                UText(text = AppStrings.ALLDAY, style = UmcTypographyTokens.Callout, color = neutral600())
                USwitch(
                    checked = uiState.isAllDay,
                    onCheckedChange = onAlldayChanged,
                )
            }

            HorizontalDivider(color = neutral300())

            // 시작 일시
            DateTimeRow(
                label = AppStrings.START,
                dateText = uiState.startDateText,
                timeText = uiState.startTimeText,
                showTime = !uiState.isAllDay,
                onDateTimeClick = onStartDateTimeClick,
                //onDateClick = onStartDateClick,
                //onTimeClick = onStartTimeClick
            )

            HorizontalDivider(color = neutral300())

            // 종료 일시
            DateTimeRow(
                label = AppStrings.END,
                dateText = uiState.endDateText,
                timeText = uiState.endTimeText,
                showTime = !uiState.isAllDay,
                onDateTimeClick = onEndDateTimeClick,
                //onDateClick = onEndDateClick,
                //onTimeClick = onEndTimeClick
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
    onDateTimeClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onDateTimeClick()}
            .padding(horizontal = 16.dp)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,

    ) {
        UText(text = label, style = UmcTypographyTokens.Callout, color = neutral600())
        Spacer(modifier = Modifier
            .weight(1f)
        )

        Box(
            modifier = Modifier.heightIn(min = 32.dp), // UChip의 일반적인 높이
            contentAlignment = Alignment.Center // 내부 콘텐츠를 항상 가운데 정렬
        ) {
            if (dateText == "") {
                Icon(
                    painter = painterResource(id = R.drawable.ic_next),
                    contentDescription = null,
                    tint = neutral400()
                )
            } else {
                UChip(
                    text = if (showTime) {
                        "${dateText} · ${timeText}"
                    } else dateText,
                    backgroundColor = primary100(),
                    borderColor = neutral200(),
                    borderWidth = 0.dp,
                    textColor = primary500(),
                    textStyle = UmcTypographyTokens.SubheadlineBold
                )
            }
        }

        /*
        if (showTime) {
            Spacer(modifier = Modifier
                .width(8.dp)
            )
            UChip(text = timeText,
                onClick = onTimeClick,
                backgroundColor = neutral000(),
                borderColor = neutral200(),
                borderWidth = 1.dp,
                textColor = neutral500(),
                textStyle = UmcTypographyTokens.SubheadlineBold)
        }

         */
    }
}