package com.umc.presentation.home.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.umc.component.R
import com.umc.component.component.UText
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.grey000
import com.umc.component.theme.grey100
import com.umc.component.theme.grey200
import com.umc.component.theme.grey300
import com.umc.component.theme.grey400
import com.umc.component.theme.grey600
import com.umc.component.theme.grey700
import com.umc.component.theme.grey800
import com.umc.component.theme.main100
import com.umc.component.theme.main500
import com.umc.domain.model.enums.CategoryType
import com.umc.domain.model.home.SchedulePlanItem
import com.umc.presentation.home.schedule.ScheduleTagChips

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

            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = grey000()),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            //날짜 정보
            Surface(
                modifier = Modifier
                    .size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = main100()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    UText(
                        text = item.dayOfWeek,
                        style = UmcTypographyTokens.Caption2Bold,
                        color = main500()
                    )
                    UText(
                        text = item.day,
                        style = UmcTypographyTokens.CalloutBold,
                        color = main500()
                    )
                }
            }

            //제목 및 시간
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                UText(text = item.title,
                    style = UmcTypographyTokens.CalloutBold,
                    color = grey800()
                )
                UText(text = item.time,
                    style = UmcTypographyTokens.Footnote,
                    color = grey600()
                )
                //일정 태그 칩 (최대 3개 + '+N')
                ScheduleTagChips(
                    tags = item.tags,
                    maxVisible = 3,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            //D-Day 칩 (클릭 불가: 터치를 Card 의 clickable 로 그대로 전달)
            if (item.dDay != null) {
                Box(
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .height(24.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(grey100())
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    UText(
                        text = item.dDay!!,
                        style = UmcTypographyTokens.Caption1Bold,
                        color = grey700()
                    )
                }
            }

            //화살표 아이콘
            androidx.compose.material3.Icon(
                painter = painterResource(id = R.drawable.ic_arrow_next),
                contentDescription = null,
                tint = grey400()
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
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = grey000()),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier
                    .size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = grey000(),
                border = BorderStroke(1.dp, grey200())
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    UText(text = item.dayOfWeek,
                        style = UmcTypographyTokens.Caption2Bold,
                        color = grey300()
                    )
                    UText(text = item.day,
                        style = UmcTypographyTokens.CalloutBold,
                        color = grey300()
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                UText(text = item.title,
                    style = UmcTypographyTokens.CalloutBold,
                    color = grey400()
                )
                UText(text = item.time,
                    style = UmcTypographyTokens.Footnote,
                    color = grey400()
                )
                //일정 태그 칩 (최대 3개 + '+N')
                ScheduleTagChips(
                    tags = item.tags,
                    maxVisible = 3,
                    textColor = grey400(),
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_next),
                contentDescription = null,
                tint = grey300()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ScheduleItemCardTagPreview() {
    ScheduleItemCard(
        item = SchedulePlanItem(
            id = 1L,
            title = "스터디",
            time = "10:00",
            date = "2026.10.12",
            dayOfWeek = "MON",
            day = "12",
            dDay = "D-Day",
            isPast = false,
            plusDay = 0,
            tags = listOf(CategoryType.STUDY)
        ),
        onItemClick = {}
    )
}

@Preview(showBackground = true, widthDp = 320)
@Composable
private fun ScheduleItemCardManyTagsPreview() {
    ScheduleItemCard(
        item = SchedulePlanItem(
            id = 2L,
            title = "UMC 10기 전체 오리엔테이션 및 네트워킹 데이",
            time = "10:00",
            date = "2026.10.12",
            dayOfWeek = "MON",
            day = "12",
            dDay = "참여 예정",
            isPast = false,
            plusDay = 0,
            tags = CategoryType.entries.toList()
        ),
        onItemClick = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun ScheduleItemCardPastTagPreview() {
    ScheduleItemCard(
        item = SchedulePlanItem(
            id = 3L,
            title = "스터디",
            time = "10:00",
            date = "2026.10.05",
            dayOfWeek = "MON",
            day = "05",
            dDay = null,
            isPast = true,
            plusDay = 0,
            tags = listOf(CategoryType.STUDY)
        ),
        onItemClick = {}
    )
}
