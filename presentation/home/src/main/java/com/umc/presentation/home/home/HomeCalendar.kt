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
import androidx.compose.material3.HorizontalDivider
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
import com.umc.component.theme.red500
import com.umc.component.theme.grey000
import com.umc.component.theme.grey100
import com.umc.component.theme.grey600
import com.umc.component.theme.grey800
import com.umc.component.theme.indigo100
import com.umc.component.theme.indigo500
import com.umc.component.theme.indigo600
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.Instant
import com.umc.component.util.UTimeFormat.toLocalDate
import com.umc.component.util.UTimeFormat.toMillis
import com.umc.component.R
import com.umc.component.theme.AppStrings


/*
 * 한 주 칸의 세로 구성. 위에서부터 아래로 이 순서대로 쌓인다.
 *
 *   ── 구분선 ──
 *      16dp        [DAY_DIVIDER_TO_CIRCLE]
 *      ● 32dp      [DAY_CIRCLE_SIZE]  — 날짜 숫자는 이 원의 한가운데
 *       6dp        [DAY_CIRCLE_TO_DOT]
 *      · 4dp       [DAY_DOT_SIZE]
 *      14dp        [DAY_DOT_TO_DIVIDER]
 *   ── 구분선 ──
 *
 * 원과 점은 오늘·선택·일정 여부와 상관없이 **항상 같은 자리를 차지한다.**
 * 배경 원만 조건부로 그리고 크기는 늘 32dp 라, 어떤 날이든 숫자와 점의 위치가 흔들리지 않는다.
 */

/** 날짜 원 지름 */
private val DAY_CIRCLE_SIZE = 32.dp

/** 위 구분선 → 원 */
private val DAY_DIVIDER_TO_CIRCLE = 16.dp

/** 원 → 점 */
private val DAY_CIRCLE_TO_DOT = 6.dp

/** 일정이 있는 날 아래에 찍는 점의 지름 */
private val DAY_DOT_SIZE = 4.dp

/** 점 → 아래 구분선 */
private val DAY_DOT_TO_DIVIDER = 14.dp

/** 한 주 칸의 높이. 위 항목들의 합이므로 따로 적지 않는다. */
private val DAY_CELL_HEIGHT =
    DAY_DIVIDER_TO_CIRCLE + DAY_CIRCLE_SIZE + DAY_CIRCLE_TO_DOT + DAY_DOT_SIZE + DAY_DOT_TO_DIVIDER

/** 주와 주 사이 구분선 두께 */
private val WEEK_DIVIDER_THICKNESS = 1.dp

/**
 * 홈 화면에서 보여주는 커스텀 달력
 *
 * HorizontalPager 기반 무한 스크롤 및 DatePicker 연동을 지원하는 월간 달력 컴포넌트입니다.
 * [주요 기능 및 작동 원리]:
 * 1. `initialPage = 5000`을 중앙(현재 월)으로 설정하여, 좌우 스와이프 시 과거/미래 월을 양방향으로 스크롤할 수 있습니다.
 * 2. `selectedDate` 변경 시 해당 월 페이지로 자동 애니메이션 스크롤(`animateScrollToPage`)되며,
 *    페이지 이동 완료 시 `onMonthChange` 콜백을 발생시켜 해당 월의 일정 데이터(Event)를 재조회하도록 유도합니다.
 * 3. 화살표 버튼, 스와이프 gesture, 또는 중앙 타이틀("YYYY.MM") 클릭 시 뜨는 DatePicker를 통해 원하는 날짜로 빠르게 이동 가능합니다.
 * 4. 일요일(Red 컬러), 오늘 날짜 하이라이트, 선택된 날짜 테두리 강조, 일정 보유 날짜 하단 Dot 표시를 지원합니다.
 *
 * @param selectedDate 현재 사용자가 선택한 날짜
 * @param eventDates 일정이 등록된 날짜 목록 (하단에 빨간 Dot 표시)
 * @param onDateClick 날짜 셀 클릭 시 호출되는 콜백
 * @param onMonthChange 달력의 월(Month)이 변경되었을 때 호출되는 콜백 (서버 일정 데이터 재조회 트리거)
 *
 *
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

    // 달력 pager 상태 및 페이징 오프셋 설정
    val initialPage = 5000 // (최대 좌우 페이징 총합 5000개월 가능)
    val pagerState = rememberPagerState(initialPage = initialPage) { 10000 }
    val coroutineScope = rememberCoroutineScope()

    // DatePicker 다이얼로그 노출 여부 상태
    var showDatePicker by remember { mutableStateOf(false) }

    // 현재 Pager 페이지에 해당하는 YearMonth 계산
    // 이번달에서 (현재 페이지 - 초기 페이지) 값을 증가해서 바꿈
    val currentMonth = remember(pagerState.currentPage) {
        YearMonth.from(LocalDate.now()).
        plusMonths((pagerState.currentPage - initialPage).toLong())
    }

    //달력 DatePickerDialog 노출 (날짜 이동 지원)
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

    //외부 selectedDate 변경(DatePicker 등) 시 해당 월 Pager 페이지로 스크롤
    LaunchedEffect(selectedDate) {
        val targetMonth = YearMonth.from(selectedDate)
        val currentMonth = YearMonth.from(LocalDate.now()).plusMonths((pagerState.currentPage - initialPage).toLong())

        if (targetMonth != currentMonth) {
            val diff = (targetMonth.year - YearMonth.now().year) * 12 + (targetMonth.monthValue - YearMonth.now().monthValue)
            pagerState.animateScrollToPage(initialPage + diff)
        }
    }

    //월 변경 감지 시 상위 화면으로 이벤트 전달 (일정 재조회용)
    LaunchedEffect(currentMonth) {
        onMonthChange(currentMonth)
    }


    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(grey000())
            // 아래쪽은 비워 둔다. 마지막 주 칸이 이미 점 아래 14dp(날짜 아래 24dp)를 갖고 있어,
            // 바로 이어지는 일정 리스트와의 간격이 그 값 그대로 유지된다.
            .padding(start = 16.dp, end = 16.dp, top = 16.dp)
    ) {
        // 상단 헤더: 이전/다음 월 이동 화살표 및 "YYYY.MM" 타이틀
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

        // 달력 몸통 (요일 + 날짜)
        CalendarBody(
            pagerState = pagerState,
            initialPage = initialPage,
            selectedDate = selectedDate,
            eventDates = eventDates,
            onDateClick = onDateClick
        )
    }
}

/**
 * 달력 상단 헤더
 *
 * 이전/다음 달 이동 화살표 버튼과 현재 연/월("YYYY.MM") 정보를 표시합니다.
 * 연/월 텍스트 클릭 시 DatePicker 다이얼로그를 트리거합니다.
 */
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


        // 년.월 타이틀 (클릭 시 DatePicker 오픈)
        UText(
            text = "${currentMonth.year}. ${String.format("%02d", currentMonth.monthValue)}",
            color = grey800(),
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

/**
 * 달력 몸통 영역 (요일 헤더 + 날짜 HorizontalPager)
 *
 * 요일 라인("일", "월", ... "토")을 고정 노출하고, 하단에 HorizontalPager를 배치해
 * 각 월별 날짜 그리드를 7개씩 Chunk하여 주(Week) 단위 Row로 그려냅니다.
 */
@Composable
private fun CalendarBody(
    pagerState: PagerState,
    initialPage: Int,
    selectedDate: LocalDate,
    eventDates: Set<LocalDate>,
    onDateClick: (LocalDate) -> Unit
) {
    Column {
        // 요일 헤더 (일~토, 일요일은 Red500 컬러 적용)
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
                    color = if (index == 0) red500() else grey600(),
                    style = UmcTypographyTokens.Caption1Bold
                )
            }
        }

        Spacer(modifier = Modifier
            .height(12.dp)
        )

        // 월별 날짜 페이저 (HorizontalPager)
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) { page ->
            val pageMonth = YearMonth.from(LocalDate.now()).plusMonths((page - initialPage).toLong())

            //해당 월의 날짜 리스트 생성 (시작 요일 이전 빈칸은 null로 채워짐)
            val days = remember(pageMonth) { getDaysInMonth(pageMonth) }

            //Column으로 1주 표현
            Column(modifier = Modifier
                .fillMaxWidth()
            ) {
                // 7개 단위로 주(Week)를 분할하여 렌더링
                // (null,null,null,1,2,3,4 .. 31)을 7개씩
                days.chunked(7).forEachIndexed { weekIndex, week ->
                    // 구분선은 주와 주 사이에만 넣는다. 첫 주 위와 마지막 주 아래에는 긋지 않는다.
                    if (weekIndex > 0) {
                        HorizontalDivider(
                            thickness = WEEK_DIVIDER_THICKNESS,
                            color = grey100(),
                        )
                    }

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
                                    // 월 시작 전 빈 공간
                                    Spacer(modifier = Modifier
                                        .height(DAY_CELL_HEIGHT)
                                    )
                                }
                            }
                        }
                        // 마지막 줄 빈칸 채우기
                        if (week.size < 7) {
                            repeat(7 - week.size) {
                                Spacer(modifier = Modifier
                                    .weight(1f)
                                    .height(DAY_CELL_HEIGHT)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


/**
 * 개별 날짜 1개에 대한 컴포지블
 *
 * 오늘 날짜, 선택된 날짜, 일요일, 일정이 있는 날짜(Dot) 상태에 따라 배경 및 텍스트 스타일을 다르게 표현합니다.
 *
 * [스타일 규칙]:
 * - 오늘(`isToday`): Indigo500 원형 배경 + 흰색 텍스트
 * - 선택됨(`isSelected`): Indigo100 배경 + Indigo600 테두리 + Indigo500 텍스트
 * - 일요일(`dayOfWeek == SUNDAY`): Red500 텍스트
 * - 일정 보유(`hasEvent`): 날짜 원과 바로 아래 행 원 사이의 정가운데에 Red500 점(Dot) 표시
 *
 * [점을 두 원 사이 정가운데에 놓는 방법]
 * 칸은 `정사각 + 점 지름` 높이이고 원은 정사각 한가운데에 있다. 이때 내 원의 아래 테두리와
 * 아래 행 원의 위 테두리를 잇는 구간의 중점은 항상 `정사각 아래 경계 + 점 반지름` 이 된다.
 *
 *   중점 = ((W/2 + r) + (W + DOT + W/2 - r)) / 2 = W + DOT/2
 *
 * r(원 반지름)이 식에서 지워지므로, 원 크기나 화면 너비가 달라져도 정사각 바로 아래 [DAY_DOT_SIZE]
 * 높이의 자리에 점을 넣기만 하면 언제나 정확히 가운데에 온다. 그래서 별도 계산 없이 아래 칸만 하나 둔다.
 */
@Composable
private fun DayItem(
    date: LocalDate,
    isToday: Boolean,
    isSelected: Boolean,
    hasEvent: Boolean,
    onClick: () -> Unit
) {
    //날짜 공간
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null //기본 사각 리플 제거
            ) { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        //위 구분선과의 간격
        Spacer(modifier = Modifier
            .height(DAY_DIVIDER_TO_CIRCLE)
        )

        //날짜 원. 오늘·선택이 아니어도 32dp 자리는 그대로 차지해 숫자와 점 위치가 고정된다.
        Box(
            modifier = Modifier
                .size(DAY_CIRCLE_SIZE)
                .then(
                    when {
                        isToday -> Modifier
                            .background(indigo500(), CircleShape)
                        isSelected -> Modifier
                            .background(indigo100(), CircleShape)
                            .border(1.dp, indigo600(), CircleShape)
                        else -> Modifier
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            UText(
                text = date.dayOfMonth.toString(),
                color = when {
                    isToday -> Color.White
                    isSelected -> indigo500()
                    date.dayOfWeek.value == 7 -> red500()
                    else -> grey800()
                },
                style = UmcTypographyTokens.Footnote,
            )
        }

        //원과 점 사이 간격
        Spacer(modifier = Modifier
            .height(DAY_CIRCLE_TO_DOT)
        )

        //점 자리. 일정이 없는 날도 같은 높이를 차지해야 행 높이가 흔들리지 않는다.
        Box(
            modifier = Modifier
                .size(DAY_DOT_SIZE)
        ) {
            //일정이 있을 때 dot(점) 보여주는 BOX
            if (hasEvent) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(red500(), CircleShape)
                )
            }
        }

        //점과 아래 구분선 사이 간격
        Spacer(modifier = Modifier
            .height(DAY_DOT_TO_DIVIDER)
        )
    }
}

/**
 * Material3 DatePickerDialog
 *
 * 빠른 날짜 이동을 위해 연/월/일 선택 모달 시스템 다이얼로그를 띄웁니다.
 *
 * [유의사항]:
 * Material3 DatePicker는 Epoch Milliseconds를 사용하므로 확장함수(`toMillis()`, `toLocalDate()`)를 통해
 * java.time.LocalDate`와의 상호 변환을 수행하고 있습니다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarDatePickerDialog(
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
            containerColor = grey000(),
        )
    ) {
        DatePicker(
            state = datePickerState,
            colors = DatePickerDefaults.colors(
                containerColor = grey000()
            )
        )
    }
}


/**
 * 특정 월(YearMonth)의 1일 시작 요일 오프셋이 적용된 날짜 리스트 생성 함수
 *
 * @param month 계산 타겟 월 (`YearMonth`)
 * @return `List<LocalDate?>` 시작 요일 이전의 빈칸은 `null`로 채워지며, 1일부터 말일까지의 `LocalDate` 객체를 포함합니다.
 *
 * [계산 예시 (2026년 4월 - 수요일 시작 가정)]:
 * -> `[null, null, null, 2026-04-01, 2026-04-02, ... 2026-04-30]`
 */
private fun getDaysInMonth(month: YearMonth): List<LocalDate?> {
    //이번 달 1일이 무슨 요일인지 체크
    val firstDayOfWeek = month.atDay(1).dayOfWeek.value % 7
    val daysInMonth = month.lengthOfMonth()
    //리스트 반환 (null null null 1 2 3 .. 31)
    return List(firstDayOfWeek) { null } + List(daysInMonth) { month.atDay(it + 1) }
}
