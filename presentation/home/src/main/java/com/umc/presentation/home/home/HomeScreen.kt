package com.umc.presentation.home.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import java.time.LocalDate
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.umc.component.R
import com.umc.component.component.HuggText
import com.umc.component.component.UButton
import com.umc.component.component.UText
import com.umc.component.component.getGrowthText
import com.umc.component.theme.AppStrings
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.danger100
import com.umc.component.theme.danger500
import com.umc.component.theme.neutral000
import com.umc.component.theme.neutral100
import com.umc.component.theme.neutral200
import com.umc.component.theme.neutral600
import com.umc.component.theme.neutral700
import com.umc.component.theme.neutral800
import com.umc.component.theme.neutral900
import com.umc.component.theme.primary100
import com.umc.component.theme.primary500
import com.umc.component.theme.primary600
import com.umc.component.theme.success100
import com.umc.component.theme.success500
import com.umc.domain.model.enums.HomeViewMode
import com.umc.domain.model.enums.UserType
import com.umc.domain.model.home.SchedulePlanItem
import kotlinx.coroutines.flow.collectLatest
import java.time.YearMonth

@Composable
fun HomeRoute(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToNotice: () -> Unit,
    onNavigateToNotification: () -> Unit,
    onNavigateToScheduleDetail: (SchedulePlanItem) -> Unit,
    onNavigateToScheduleAdd: () -> Unit,
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(viewModel){
        viewModel.uiEvent.collectLatest{ event ->
            //이벤트 처리
            when(event){
                //공지사항 이동
                is HomeEvent.MoveNoticeEvent -> onNavigateToNotice()
                //알림 이동
                is HomeEvent.MoveNotificationEvent -> onNavigateToNotification()
                //일정 상세 이동
                is HomeEvent.MoveScheduleDetailEvent -> onNavigateToScheduleDetail(event.plan)
                //일정 추가 이동
                is HomeEvent.MoveScheduleAddEvent -> onNavigateToScheduleAdd()
                else -> {}
            }
        }
    }

    HomeScreen(
        uiState = uiState,
        onDateClick = viewModel::setSelectedDate,
        onMonthChange = { month -> viewModel.getScheduleMonth(month.year, month.monthValue) },
        onChangeViewMode = viewModel::onChangeViewMode, //달력 + 일정 or 일정 보여줄 지 결정
        onNotificationClick = viewModel::onClickNotification, //Topbar에서 알람 터치 시
        onScheduleAddClick = viewModel::onClickScheduleAdd, //일정 추가 터치 시
        onScheduleDetailClick = viewModel::onClickScheduleDetail,
        onNoticeClick = viewModel::onClickNotice
    )
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
    onDateClick: (LocalDate) -> Unit,
    onMonthChange: (YearMonth) -> Unit,
    onChangeViewMode: (HomeViewMode) -> Unit,
    onNotificationClick: () -> Unit,
    onScheduleAddClick: () -> Unit,
    onScheduleDetailClick: (SchedulePlanItem) -> Unit,
    onNoticeClick: () -> Unit,
) {

    //LazyColumn을 사용하여 전체 스크롤 관리(중첩 스크롤 방지)
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(neutral100())
    ) {

        //1. 상단 섹션(유저 정보)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(neutral000())
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp)
                    .padding(bottom = 32.dp)
            ) {
                HomeTopBar(
                    alarmExist = uiState.alarmExist,
                    userType = uiState.userType,
                    onNotificationClick = onNotificationClick
                )

                HomeProfileCard(uiState = uiState)

                if (uiState.userType == UserType.ACTIVE) {
                    Spacer(modifier = Modifier
                        .height(16.dp)
                    )
                    HomeActivityStatusCard(uiState = uiState)
                }
            }
        }

        //2. 하단 섹션(일정)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 32.dp)
            ) {
                HomePlanHeader(
                    viewMode = uiState.viewMode,
                    onAddClick = onScheduleAddClick,
                    onChangeViewMode = onChangeViewMode
                )
                Spacer(modifier = Modifier
                    .height(32.dp)
                )
            }
        }

        //3. 달력 및 리스트
        item {
            //달력과 리스트를 하나의 둥근 직사각형 안에 묶음
            Surface(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = neutral000(),
                shadowElevation = 0.dp
            ) {
                Column(modifier = Modifier
                    .fillMaxWidth()
                ) {
                    if (uiState.viewMode == HomeViewMode.CALENDAR) {
                        //달력 모드
                        HomeCalendar(
                            modifier = Modifier
                                .padding(16.dp),
                            selectedDate = uiState.selectedDate,
                            eventDates = uiState.eventDates,
                            onDateClick = onDateClick,
                            onMonthChange = onMonthChange
                        )

                        // 일정 리스트가 있을 때만 구분선과 리스트 표시
                        if (uiState.dailyPlans.isNotEmpty()) {
                            //구분선
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .height(1.dp)
                                    .background(neutral200())
                            )

                            Spacer(modifier = Modifier
                                .height(16.dp)
                            )

                            //달력 아래 일일 일정들
                            uiState.dailyPlans.forEach { plan ->
                                Box(modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                ) {
                                    ScheduleItemCard(
                                        item = plan,
                                        onItemClick = onScheduleDetailClick
                                    )
                                }
                            }
                            Spacer(modifier = Modifier
                                .height(16.dp)
                            )
                        }
                    }

                    else {
                        //리스트 모드
                        Spacer(modifier = Modifier
                            .height(16.dp)
                        )
                        uiState.allPlans.forEach { plan ->
                            Box(modifier = Modifier
                                .padding(horizontal = 16.dp)
                            ) {
                                ScheduleItemCard(
                                    item = plan,
                                    onItemClick = onScheduleDetailClick
                                )
                            }
                        }
                        Spacer(modifier = Modifier
                            .height(16.dp)
                        )
                    }
                }
            }
        }

        // 바닥 여백
        item { Spacer(modifier = Modifier
            .height(64.dp)
        ) }
    }
}

@Composable
fun HomeProfileCard(uiState: HomeUiState) {


    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = primary100()),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(modifier = Modifier
            .padding(16.dp)
        ) {
            Column {
                // 기수 태그 (FlexboxLayout 대응)
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth(),
                ) {
                    uiState.gisuTag.forEach { tag ->
                        UButton(
                            text = tag,
                            backgroundColor = neutral000(),
                            textColor = primary600(),
                            textStyle = UmcTypographyTokens.Caption1Bold,
                            onClick = {},
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .height(24.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier
                    .height(16.dp)
                )

                // 유저 이름 (닉네임 + 실명)
                UText(
                    text = "${uiState.userNickName}(${uiState.userName})",
                    style = UmcTypographyTokens.Title3,
                    color = neutral700()
                )

                //성장 일수 (Spannable 대신 하위 함수 호출)
                HuggText(
                    text = getGrowthText(uiState.growDay),
                    style = UmcTypographyTokens.Title3Bold,
                    modifier = Modifier
                        .padding(top = 8.dp)
                )
            }

            Image(
                painter = painterResource(
                    id = if (uiState.userType == UserType.ACTIVE) R.drawable.ic_home_active else R.drawable.ic_home_ob
                ),
                contentDescription = null,
                modifier = Modifier
                    .size(90.dp)
                    .align(Alignment.CenterEnd)
            )
        }
    }
}


/**
 * 일정 제목 및 뷰 모드(달력/리스트) 전환 헤더
 */

//Arrangement.SpaceBetween = 2개를 양 끝으로 밀어냄
@Composable
fun HomePlanHeader(
    viewMode: HomeViewMode,
    onAddClick: () -> Unit,
    onChangeViewMode: (HomeViewMode) -> Unit
) {
    Row (
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        UText(
            text = AppStrings.HOME_PLAN_TITLE,
            style = UmcTypographyTokens.Title3Bold,
            color = neutral800()
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onAddClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_add_filled),
                    contentDescription = "Add",
                    tint = primary500()
                )
            }

            Spacer(modifier = Modifier
                .width(8.dp)
            )

            //뷰 전환 선택기
            Surface(
                color = neutral000(),
                shape = RoundedCornerShape(8.dp)
            ) {

                Row(modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    //달력 + 일정 버튼
                    Icon(
                        painter = painterResource(
                            id = if (viewMode == HomeViewMode.CALENDAR) R.drawable.ic_home_grid_on else R.drawable.ic_home_grid_off
                        ),
                        contentDescription = "Calendar Mode",
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { onChangeViewMode(HomeViewMode.CALENDAR) },
                        tint = Color.Unspecified
                    )
                    Spacer(modifier = Modifier
                        .width(8.dp)
                    )

                    //일정 리스트 버튼
                    Icon(
                        painter = painterResource(
                            id = if (viewMode == HomeViewMode.LIST) R.drawable.ic_home_list_on else R.drawable.ic_home_list_off
                        ),
                        contentDescription = "List Mode",
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { onChangeViewMode(HomeViewMode.LIST) },
                        tint = Color.Unspecified
                    )
                }
            }
        }
    }
}

/**
 * ACTIVE 유저 전용 상점/벌점/총합 점수판 카드
 */
@Composable
fun HomeActivityStatusCard(uiState: HomeUiState) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = neutral200()),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier
            .padding(16.dp)
        ) {

            //X기 활동 상태
            UText(
                text = uiState.activeString,
                style = UmcTypographyTokens.HeadlineBold,
                color = neutral800()
            )

            Spacer(modifier = Modifier
                .height(12.dp)
            )

            //상벌점 판
            Surface(
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = neutral000()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    //상점
                    ScoreCard(
                        modifier = Modifier
                            .weight(1f),
                        label = AppStrings.REWARD,
                        score = uiState.sangjum,
                        color = success500(),
                        bgColor = success100()
                    )

                    VerticalDivider()

                    //벌점
                    ScoreCard(
                        modifier = Modifier
                            .weight(1f),
                        label = AppStrings.PUNISH,
                        score = uiState.buljum,
                        color = danger500(),
                        bgColor = danger100()
                    )

                    VerticalDivider()

                    //총합
                    ScoreCard(
                        modifier = Modifier
                            .weight(1f),
                        label = AppStrings.HAB,
                        score = uiState.total,
                        color = primary500(),
                        bgColor = primary100()
                    )
                }
            }
        }
    }
}

/**라벨(상점/벌점)과 그 점수가 존재하는 1개의 영역**/
@Composable
private fun ScoreCard(
    modifier: Modifier,
    label: String,
    score: Int,
    color: Color,
    bgColor: Color
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        //UButton 컴포저블을 활용하여 일관된 디자인 유지
        UButton(
            text = label,
            backgroundColor = bgColor,
            textColor = color,
            textStyle = UmcTypographyTokens.Caption1Bold,
            cornerRadius = 4.dp,
            onClick = {},
            modifier = Modifier
                .height(24.dp),
        )
        UText(
            text = score.toString(),
            modifier = Modifier
                .padding(top = 8.dp),
            style = UmcTypographyTokens.HeadlineBold,
            color = neutral900()
        )
    }
}

//카드 분할 Divider
@Composable
private fun VerticalDivider() {
    Box(modifier = Modifier
        .width(1.dp)
        .height(54.dp)
        .background(neutral200())
    )
}



@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    // 1. 테스트용 가짜 UI 상태 생성
    val dummyUiState = HomeUiState(
        userName = "박유수",
        userNickName = "어헛차",
        userType = UserType.ACTIVE,
        growDay = 120,
        activeString = "12기 활동 상태",
        sangjum = 5,
        buljum = -2,
        total = 3,
        gisuTag = listOf("10기", "11기", "12기"),
        viewMode = HomeViewMode.CALENDAR,
        // 필요하다면 가짜 일정 리스트도 추가 가능
        dailyPlans = emptyList()
    )

    // 2. HomeScreen 호출 (모든 파라미터에 빈 람다나 더미 데이터 전달)
    HomeScreen(
        uiState = dummyUiState,
        onDateClick = {},
        onMonthChange = {},
        onChangeViewMode = {},
        onNotificationClick = {},
        onScheduleAddClick = {},
        onScheduleDetailClick = {},
        onNoticeClick = {}
    )
}