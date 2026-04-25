package com.umc.presentation.home.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.umc.component.R
import com.umc.component.component.UButton
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.neutral000
import com.umc.component.theme.neutral200
import com.umc.component.theme.neutral300
import com.umc.component.theme.neutral400
import com.umc.component.theme.neutral600
import com.umc.component.theme.neutral700
import com.umc.component.theme.neutral800
import com.umc.component.theme.primary100
import com.umc.component.theme.primary500
import com.umc.domain.model.home.SchedulePlanItem

/**
 *
 * 홈 일정 리스트에서 1개의 일정 아이템을 담당
 *
 * **/

@Composable
fun ScheduleItemCard(
    item: SchedulePlanItem,
    onItemClick: (SchedulePlanItem) -> Unit
){
    //현재 일정이 과거인지 현재인지에 따라 다른 컴포지블 함수 호출
    if (item.isPast) {
        DefaultScheduleItem(item = item, onClick = { onItemClick(item) })
    } else {
        ActiveScheduleItem(item = item, onClick = { onItemClick(item) })
    }
}

/**
 * 다가오는 일정
 */
@Composable
fun ActiveScheduleItem(
    item: SchedulePlanItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = neutral000()),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            //날짜 정보
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = primary100()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = item.dayOfWeek, style = UmcTypographyTokens.Caption2Bold, color = primary500())
                    Text(text = item.day, style = UmcTypographyTokens.CalloutBold, color = primary500())
                }
            }

            //제목 및 시간
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Text(text = item.title, style = UmcTypographyTokens.CalloutBold, color = neutral800())
                Text(text = item.time, style = UmcTypographyTokens.Footnote, color = neutral600())
            }

            //D-Day 버튼
            if (item.dDay != null) {
                UButton(
                    text = item.dDay!!,
                    cardBackgroundColor = neutral200(),
                    textColor = neutral700(),
                    textAppearance = UmcTypographyTokens.Caption1Bold,
                    modifier = Modifier.padding(end = 16.dp)
                )
            }

            //화살표 아이콘
            androidx.compose.material3.Icon(
                painter = painterResource(id = R.drawable.ic_arrow_next),
                contentDescription = null,
                tint = neutral400()
            )
        }
    }
}

/**
 * DefaultScheduleItem: 지난 일정
 */
@Composable
fun DefaultScheduleItem(
    item: SchedulePlanItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = neutral000()),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = neutral000(),
                border = BorderStroke(1.dp, neutral200())
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = item.dayOfWeek, style = UmcTypographyTokens.Caption2Bold, color = neutral300())
                    Text(text = item.day, style = UmcTypographyTokens.CalloutBold, color = neutral300())
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Text(text = item.title, style = UmcTypographyTokens.CalloutBold, color = neutral400())
                Text(text = item.time, style = UmcTypographyTokens.Footnote, color = neutral400())
            }

            androidx.compose.material3.Icon(
                painter = painterResource(id = R.drawable.ic_arrow_next),
                contentDescription = null,
                tint = neutral300()
            )
        }
    }
}