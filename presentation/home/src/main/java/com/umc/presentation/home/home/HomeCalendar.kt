package com.umc.presentation.home.home

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.ui.unit.sp
import com.umc.component.component.UText
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.danger500
import com.umc.component.theme.neutral000
import com.umc.component.theme.neutral600
import com.umc.component.theme.neutral800
import com.umc.component.theme.primary100
import com.umc.component.theme.primary500
import com.umc.component.theme.primary600
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.Instant
import com.umc.component.util.UTimeFormat.toLocalDate
import com.umc.component.util.UTimeFormat.toMillis
import com.umc.component.R
import com.umc.component.theme.AppStrings


/**
 * 홈 화면에서 보여주는 달력 composible 함수
 * **/
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeCalendar(
    selectedDate: LocalDate, //선택 날짜
    eventDates: Set<LocalDate>, //일정이 있는 날짜들
    onDateClick: (LocalDate) -> Unit, //날짜 선택 시 함수
    onMonthChange: (YearMonth) -> Unit, //월별 바꿀 시 함수
    modifier: Modifier = Modifier
) {

    //달력 좌우 스크롤을 위한 값들
    val initialPage = 5000
    val pagerState = rememberPagerState(initialPage = initialPage) { 10000 }
    val coroutineScope = rememberCoroutineScope()

    //달력 DatePicker를 여는 변수
    var showDatePicker by remember { mutableStateOf(false) }

    //현재 월 (pagerState가 바뀌면 날짜도 같이 바뀌게 하기)
    //이번달에서 (현재 페이지 - 초기 페이지) 값을 증가해서 바꾸기
    val currentMonth = remember(pagerState.currentPage) {
        YearMonth.from(LocalDate.now()).
        plusMonths((pagerState.currentPage - initialPage).toLong())
    }

    //달력 DatePickerDialog를 통해 만들기
    if (showDatePicker) {
        CalendarDatePickerDialog(
            selectedDate = selectedDate,
            onDateSelected = { date ->
                onDateClick(date)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }

    //외부에서 selectedDate 변경(DatePicker) 시 페이지 이동
    LaunchedEffect(selectedDate) {
        val targetMonth = YearMonth.from(selectedDate)
        val currentMonth = YearMonth.from(LocalDate.now()).plusMonths((pagerState.currentPage - initialPage).toLong())

        if (targetMonth != currentMonth) {
            val diff = (targetMonth.year - YearMonth.now().year) * 12 + (targetMonth.monthValue - YearMonth.now().monthValue)
            pagerState.animateScrollToPage(initialPage + diff)
        }
    }

    //이동 끝난 후 (월이 바뀌었을 때, 달이 바뀌었음을 호출)
    LaunchedEffect(currentMonth) {
        onMonthChange(currentMonth)
    }


    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(neutral000())
            .padding(16.dp)
    ) {
        //헤더 - 월정보(2026.04) 및 화살표
        CalendarHeader(
            currentMonth = currentMonth,
            //누르면 달 - 1
            onPrevClick = {
                coroutineScope.launch {
                    pagerState.animateScrollToPage(pagerState.currentPage - 1,
                        animationSpec = tween(500))
                }
            },
            //누르면 달 + 1
            onNextClick = {
                coroutineScope.launch {
                    pagerState.animateScrollToPage(pagerState.currentPage + 1,
                        animationSpec = tween(500))
                }
            },
            //누르면 DatePicker 열도록
            onTitleClick = { showDatePicker = true }
        )

        Spacer(modifier = Modifier
            .height(24.dp)
        )

        //달력 몸통 (요일 + 날짜)
        CalendarBody(
            pagerState = pagerState,
            initialPage = initialPage,
            selectedDate = selectedDate,
            eventDates = eventDates,
            onDateClick = onDateClick
        )
    }
}

//달력 상단 헤더( < 2026.04.09 > )
@Composable
private fun CalendarHeader(
    currentMonth: YearMonth, //현재 월 정보
    onPrevClick: () -> Unit, //이전 달 이동 터치 시 로직
    onNextClick: () -> Unit, //다음 달 이동 터치 시 로직
    onTitleClick: () -> Unit //제목 누를 시 로직 (DatePicker)
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_calendar_arrow_left),
                contentDescription = null,
                modifier = Modifier
                    .clip(CircleShape) // 클릭 영역을 원형으로 제한
                    .clickable { onPrevClick() }
                    .padding(12.dp)
            )


        UText(
            text = "${currentMonth.year}.${String.format("%02d", currentMonth.monthValue)}",
            //style =, /**임시 폰트**/
            color = neutral800(),
            style = UmcTypographyTokens.HeadlineBold,
            modifier = Modifier
                .clickable { onTitleClick() }
        )

        Icon(
            painter = painterResource(id = R.drawable.ic_calendar_arrow_right),
            contentDescription = null,
            modifier = Modifier
                .clip(CircleShape) // 클릭 영역을 원형으로 제한
                .clickable { onNextClick() }
                .padding(12.dp) // IconButton의 기본 터치 영역 확보를 위해 패딩 추가
        )
    }
}

//달력 몸통 (요일 라인 + 날짜 뷰페이저)
@Composable
private fun CalendarBody(
    pagerState: PagerState,
    initialPage: Int,
    selectedDate: LocalDate,
    eventDates: Set<LocalDate>,
    onDateClick: (LocalDate) -> Unit
) {
    Column {
        // 요일 헤더
        Row(modifier = Modifier
            .fillMaxWidth()
        ) {
            val daysOfWeek = listOf("일", "월", "화", "수", "목", "금", "토")
            daysOfWeek.forEachIndexed { index, day ->
                UText(
                    text = day,
                    modifier = Modifier
                        .weight(1f),
                    textAlign = TextAlign.Center,
                    color = if (index == 0) danger500() else neutral600(),
                    style = UmcTypographyTokens.Caption1
                )
            }
        }

        Spacer(modifier = Modifier
            .height(12.dp)
        )

        //날짜 표시 페이저
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) { page ->
            val pageMonth = YearMonth.from(LocalDate.now()).plusMonths((page - initialPage).toLong())
            val days = remember(pageMonth) { getDaysInMonth(pageMonth) }

            //Column으로 1주 표현
            Column(modifier = Modifier
                .fillMaxWidth()
            ) {
                //리스트를 7개씩 잘라서 만들자 (null,null,null,1,2,3,4 .. 31)을 7개씩
                days.chunked(7).forEach { week ->
                    Row(modifier = Modifier
                        .fillMaxWidth()
                    ) {
                        week.forEach { date ->
                            Box(modifier = Modifier
                                .weight(1f)
                            ) {
                                if (date != null) {
                                    DayItem(
                                        date = date,
                                        isToday = date == LocalDate.now(),
                                        isSelected = date == selectedDate,
                                        hasEvent = eventDates.contains(date),
                                        onClick = { onDateClick(date) }
                                    )
                                } else {
                                    Spacer(modifier = Modifier
                                        .aspectRatio(1f)
                                    )
                                }
                            }
                        }
                        // 마지막 줄 빈칸 채우기
                        if (week.size < 7) {
                            repeat(7 - week.size) {
                                Spacer(modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


//날짜 1칸에 대한 컴포지블 함수 : 날짜 색깔과 선택 원 및 일정 시 색깔 표시
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
            isToday -> Modifier
                .fillMaxSize(0.7f)
                .background(primary500(), CircleShape)
            isSelected -> Modifier.fillMaxSize(0.7f)
                .background(primary100(), CircleShape)
                .border(1.dp, primary600(), CircleShape)
            else -> Modifier
        }

        //원 형태 보여주는 BOX
        Box(
            modifier = backgroundModifier,
            contentAlignment = Alignment.Center
        ) {
            UText(
                text = date.dayOfMonth.toString(),
                color = when {
                    isToday -> Color.White
                    isSelected -> primary500()
                    date.dayOfWeek.value == 7 -> danger500()
                    else -> neutral800()
                },
                style = UmcTypographyTokens.Footnote,
            )
        }

        //일정이 있을 때 dot(점) 보여주는 BOX
        if (hasEvent) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 2.dp)
                    .size(4.dp)
                    .background(danger500(), CircleShape)
            )
        }
    }
}

//DatePicker 다이얼로그
@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun CalendarDatePickerDialog(
    selectedDate: LocalDate, //선택한 날짜
    onDateSelected: (LocalDate) -> Unit, //날짜를 선택했을 때 콜백 함수
    onDismiss: () -> Unit //DatePicker 사라질 때 콜백 함수 (없애)
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate.toMillis()
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let { millis ->
                    onDateSelected(millis.toLocalDate())
                }
            }) { UText(AppStrings.CONFIRM) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { UText(AppStrings.CANCEL) }
        },
        colors = DatePickerDefaults.colors(
            containerColor = neutral000(),
        )
    ) {
        DatePicker(state = datePickerState)
    }
}


//이번 달에 날짜들을 반환(시작 요일에 맞춰 출력)
private fun getDaysInMonth(month: YearMonth): List<LocalDate?> {
    //이번 달 1일이 무슨 요일인지 체크
    val firstDayOfWeek = month.atDay(1).dayOfWeek.value % 7
    val daysInMonth = month.lengthOfMonth()
    //리스트 반환 (null null null 1 2 3 .. 31)
    return List(firstDayOfWeek) { null } + List(daysInMonth) { month.atDay(it + 1) }
}
