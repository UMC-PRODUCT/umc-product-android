package com.umc.presentation.home

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.YearMonth
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun HomeCalendar(
    selectedDate: LocalDate, //선택 날짜
    eventDates: Set<LocalDate>, //일정이 있는 날짜들
    onDateClick: (LocalDate) -> Unit, //날짜 선택 시 함수
    onMonthChange: (YearMonth) -> Unit, //월별 바꿀 시 함수
    modifier: Modifier = Modifier
) {

    //달력 좌우 스크롤을 위한 값들
    val initialPage = 500
    val pagerState = rememberPagerState(initialPage = initialPage) { 1000 }
    val coroutineScope = rememberCoroutineScope()

    //현재 월
    val currentMonth = remember(pagerState.currentPage) {
        YearMonth.from(LocalDate.now()).
        plusMonths((pagerState.currentPage - initialPage).toLong())
    }

    //이동 끝난 후 (월이 바뀌었을 때, 달이 바뀌었음을 호출)
    LaunchedEffect(currentMonth) {
        onMonthChange(currentMonth)
    }


    //달력 뼈대
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFFFFFFF)) /**임시 색깔 neutral000**/
            .padding(16.dp)
    ) {
        //헤더 - 월정보(2026.04) 및 화살표
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            //누르면 달 -1
            IconButton(onClick = {
                coroutineScope.launch {
                    pagerState.animateScrollToPage(pagerState.currentPage - 1,
                        animationSpec = tween(500))
                }
            }) {
                Icon(painterResource(id = R.drawable.ic_calendar_arrow_left), contentDescription = null)
            }

            //월 정보
            Text(
                text = "${currentMonth.year}.${String.format("%02d", currentMonth.monthValue)}",
                //style =, /**임시 폰트**/
                color = Color(0xFF34363E), /**임시 색깔 neutral800**/
                modifier = Modifier.clickable { /* DatePicker 팝업 */ }
            )

            //누르면 달 +1
            IconButton(onClick = {
                coroutineScope.launch {
                    pagerState.animateScrollToPage(pagerState.currentPage + 1,
                        animationSpec = tween(500))
                }
            }) {
                Icon(painterResource(id = R.drawable.ic_calendar_arrow_right), contentDescription = null)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        //요일 라인
        val daysOfWeek = listOf("일", "월", "화", "수", "목", "금", "토")
        Row(modifier = Modifier.fillMaxWidth()) {
            daysOfWeek.forEachIndexed { index, day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    //style = UmcTypographyTokens.Caption1,
                    color = if (index == 0) Color(0xFFF14437) else Color(0xFF6D7882) /**danger500 / neutral 600**/
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        //날짜 표시(스크롤을 위해 LazyVerticalGrid -> HorizontalPager)
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top //높이 자동 조절을 위해 상단 정렬
        ) { page ->
            val pageMonth = YearMonth.from(LocalDate.now()).plusMonths((page - initialPage).toLong())
            val days = remember(pageMonth) { getDaysInMonth(pageMonth) }

            //Column/Row 조합으로 wrap_content 구현
            //Column으로 주 표현
            Column(modifier = Modifier.fillMaxWidth()) {
                val chunks = days.chunked(7)
                //Row를 통해, 월~일 표현
                chunks.forEach { week ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        week.forEach { date ->
                            Box(modifier = Modifier.weight(1f)) {
                                if (date != null) {
                                    DayItem(
                                        date = date,
                                        isToday = date == LocalDate.now(),
                                        isSelected = date == selectedDate,
                                        hasEvent = eventDates.contains(date),
                                        onClick = { onDateClick(date) }
                                    )
                                } else {
                                    Spacer(modifier = Modifier.aspectRatio(1f))
                                }
                            }
                        }
                        if (week.size < 7) {
                            repeat(7 - week.size) {
                                Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

//날짜 1칸에 대한 컴포지블 함수
//날짜 색깔과 선택 원 및 일정 시 색깔 표시
@Composable
private fun DayItem(
    date: LocalDate,
    isToday: Boolean,
    isSelected: Boolean,
    hasEvent: Boolean,
    onClick: () -> Unit
) {
    //날짜 공간
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null //기본 사각 리플 제거
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        //원 크기를 칸의 70% 정도로 조절
        val backgroundModifier = when {
            isToday -> Modifier.fillMaxSize(0.7f).background(Color(0xFF4869F0), CircleShape)
            isSelected -> Modifier.fillMaxSize(0.7f)
                .background(Color(0xFFEBEFFF), CircleShape)
                .border(1.dp, Color(0xFF3A5AD9), CircleShape)
            else -> Modifier
        }

        //원 형태 보여주는 BOX
        Box(
            modifier = backgroundModifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                color = when {
                    isToday -> Color.White
                    isSelected -> Color(0xFF3A5AD9)
                    date.dayOfWeek.value == 7 -> Color(0xFFF14437)
                    else -> Color(0xFF34363E)
                },
                fontSize = 14.sp
            )
        }

        //일정이 있을 때 dot(점) 보여주는 BOX
        if (hasEvent) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 2.dp)
                    .size(4.dp)
                    .background(Color(0xFFE55900), CircleShape)
            )
        }
    }
}

//이번 달에 날짜들을 반환(시작 요일에 맞춰 출력)
private fun getDaysInMonth(month: YearMonth): List<LocalDate?> {
    val firstDayOfWeek = month.atDay(1).dayOfWeek.value % 7
    val daysInMonth = month.lengthOfMonth()
    return List(firstDayOfWeek) { null } + List(daysInMonth) { month.atDay(it + 1) }
}