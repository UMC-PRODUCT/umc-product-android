package com.umc.presentation.home.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import java.time.LocalDate
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.umc.component.R
import com.umc.component.component.UText
import com.umc.component.component.UButton
import com.umc.component.component.getGrowthText
import com.umc.component.theme.AppStrings
import com.umc.component.theme.UmcTypography
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.red100
import com.umc.component.theme.red500
import com.umc.component.theme.grey000
import com.umc.component.theme.grey100
import com.umc.component.theme.grey200
import com.umc.component.theme.grey700
import com.umc.component.theme.grey800
import com.umc.component.theme.grey900
import com.umc.component.theme.indigo100
import com.umc.component.theme.indigo500
import com.umc.component.theme.indigo600
import com.umc.component.theme.green100
import com.umc.component.theme.green500
import com.umc.component.theme.grey400
import com.umc.component.theme.grey50
import com.umc.component.theme.grey600
import com.umc.component.theme.grey950
import com.umc.component.theme.indigo900
import com.umc.domain.model.enums.HomeViewMode
import com.umc.domain.model.enums.UserType
import com.umc.domain.model.home.SchedulePlanItem
import kotlinx.coroutines.flow.collectLatest
import java.time.YearMonth
import com.umc.component.base.CollectUiEvents
import kotlinx.collections.immutable.persistentListOf

@Composable
fun HomeRoute(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToNotice: () -> Unit,
    onNavigateToNotification: () -> Unit,
    onNavigateToScheduleDetail: (SchedulePlanItem) -> Unit,
    onNavigateToScheduleAdd: () -> Unit,
    onNavigateToCardShare: () -> Unit
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    CollectUiEvents(viewModel.uiEvent) { event ->
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
            //카드 공유 이동
            is HomeEvent.MoveShareCardEvent -> onNavigateToCardShare()
            else -> {}
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
        onNoticeClick = viewModel::onClickNotice,
        onShareCardClick = viewModel::onClickCardShare
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
    onShareCardClick: () -> Unit,
) {

    //LazyColumn을 사용하여 전체 스크롤 관리(중첩 스크롤 방지)
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(grey100())
    ) {


        //1. 상단 섹션(유저 정보)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(grey100())
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp)
                    .padding(bottom = 16.dp)
            ) {

                HomeTopBar(
                    alarmExist = uiState.alarmExist,
                    onNotificationClick = onNotificationClick
                )

                Spacer(modifier = Modifier
                    .height(16.dp)
                )

                //카드 교환 배너
                if(uiState.isBannerVisible){
                    HomeShareCardsRow(
                        uiState = uiState,
                        memberId = uiState.userMemberId,
                        onCardClick = onShareCardClick
                    )

                    Spacer(modifier = Modifier
                        .height(16.dp)
                    )
                }


                //HomeProfileCard(uiState = uiState)
                HomeProfileCardsRow(uiState = uiState)

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
            ) {
                HomePlanHeader(
                    viewMode = uiState.viewMode,
                    onAddClick = onScheduleAddClick,
                    onChangeViewMode = onChangeViewMode
                )
                Spacer(modifier = Modifier
                   .height(16.dp)
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
                color = if (uiState.viewMode == HomeViewMode.CALENDAR) grey000() else grey100(),
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
                            /*
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .height(1.dp)
                                    .background(grey200())
                            )

                            Spacer(modifier = Modifier
                                .height(16.dp)
                            )

                             */

                            //달력 아래 일일 일정들
                            uiState.dailyPlans.forEach { plan ->

                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp)

                                    ) {
                                        HorizontalDivider(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp), // 카드 사이 구분선의 위아래 여백
                                            thickness = 1.dp,
                                            color = grey200()
                                        )

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
                        uiState.allPlans.forEach { plan ->

                                ScheduleItemCard(
                                    item = plan,
                                    onItemClick = onScheduleDetailClick
                                )

                                Spacer(modifier = Modifier.height(8.dp))

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



/**명함 교환 카드**/
@Composable
fun HomeShareCardsRow(
    uiState: HomeUiState,
    memberId: Long,
    onCardClick: () -> Unit,
) {

    val isTypeA = (memberId % 2 == 0L)

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = indigo600()),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {

        Box(
            modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
        ){

            Column(
                modifier = Modifier
                    .fillMaxWidth()

            ) {

                Column(
                    modifier = Modifier.padding(end = 50.dp)
                ) {
                    UText(
                        text = if (isTypeA) "오늘 만난 인연,\n명함으로 이어가세요" else "디지털 명함,\n이제 UMC 앱에서",
                        color = grey000(),
                        style = UmcTypographyTokens.Title3Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    UText(
                        text = "디지털 명함으로 간편하게 교환하세요",
                        color = grey000(),
                        style = UmcTypographyTokens.Footnote
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))


                UButton(
                    text = "명함 교환하기",
                    onClick = onCardClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp),
                    textColor = grey000(),
                    backgroundColor = indigo900(),
                    textStyle = UmcTypographyTokens.CalloutBold,
                    cornerRadius = 8.dp,
                    contentPadding = PaddingValues(horizontal = 13.dp, vertical = 8.dp)
                )
            }


            Image(
                painter = painterResource(id = R.drawable.ic_home_shard_card),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .wrapContentSize(align = Alignment.TopEnd, unbounded = true)
                    .requiredSize(150.dp)
                    .offset(x = 16.dp, y = (-32.dp))
            )
        }
    }
}

/**
 * 프로필 카드 V2
 *
 * **/
@Composable
fun HomeProfileCardsRow(
    uiState: HomeUiState
) {

    //Row에 2개의 카드를 표시
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        //1. 좌측 누적 활동일 카드
        Card(
            modifier = Modifier
                .weight(1f)
                .height(108.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = grey000())
        ){
            Box(modifier = Modifier.fillMaxSize()) {

                Image(
                    painter = painterResource(id = R.drawable.ic_home_attend_day_background),
                    contentDescription = "누적 활동일 배경",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .height(100.dp)
                        .aspectRatio(724f / 432f) //Figma 이미지의 비율 반영
                        .align(Alignment.BottomEnd)
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                ) {
                    UText(
                        text = AppStrings.HOME_ACTIVATE_DAY,
                        style = UmcTypographyTokens.CalloutBold,
                        color = grey950()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = buildAnnotatedString {
                            withStyle(
                                style = UmcTypographyTokens.Title3Bold.toSpanStyle().copy(
                                    color = indigo500(),
                                )
                            ) {
                                append("${uiState.growDay}")
                                append(" ")
                            }
                            // 나머지 부분
                            withStyle(
                                style = UmcTypographyTokens.Footnote.toSpanStyle().copy(
                                    color = grey700(),
                                )
                            ) {
                                append("Days")
                            }
                        }
                    )
                }

            }
        }

        //2. 우측 참여 기수 카드
        Card(
            modifier = Modifier
                .weight(1f)
                .height(108.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = grey000())
        ){
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = R.drawable.ic_home_attend_gisu_background),
                    contentDescription = "참여 기수 배경",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .height(100.dp)
                        .aspectRatio(724f / 432f)
                        .align(Alignment.BottomEnd)
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                ) {
                    UText(
                        text = AppStrings.HOME_ATTEND_GISU,
                        style = UmcTypographyTokens.CalloutBold,
                        color = grey950()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    GisuGrid(gisuStrings = uiState.gisuTag)


                }
            }
        }



    }
}

/**HomeProfileCardRows에 쓰이는 grid (2*2) 형태
 * GisuChip을 item으로 쓴다.
 *
 * **/
@Composable
fun GisuGrid(gisuStrings: List<String>){

    val latestGisus = gisuStrings.reversed() //최신 꺼 가져오기 위함.

    val displayCount = if (gisuStrings.size > 4) 3 else gisuStrings.size // 5개 부터 마지막 부분에 표시해야 하므로 -2.
    val hasMore = gisuStrings.size > 4 //4개보다 많은지 ( + 표시 )
    val remainingCount = gisuStrings.size - 3 // +3 등 String에 표시될 숫자

    val addItems = mutableListOf<String>() //보여줄 item 리스트
    if(hasMore){
        addItems.add("MORE") //체크 용도(제일 마지막 출력을 위해 제일 먼저 넣는다)
    }
    addItems.addAll(latestGisus.take(displayCount))

    val gridItems = addItems.reversed() //뒤집기

    //2*2 격자 형태
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        //2행
        gridItems.chunked(2).forEach { items ->
            //2열
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    //만약 More이면 남은 개수 표시 (+ N)
                    if (item == "MORE") {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ){
                            Icon(
                                modifier = Modifier
                                    .size(18.dp),
                                painter = painterResource(id = R.drawable.ic_add_filled),
                                contentDescription = "Add",
                                tint = indigo500()
                            )

                            UText(
                                text = "$remainingCount",
                                style = UmcTypographyTokens.Caption1Bold,
                                color = indigo500()
                            )
                        }


                    } else {
                        //일반 기수 칩
                        GisuChip(item)
                    }
                }
            }
        }

    }


}

/**HomeProfileCardRows 전용 기수 칩
 * 기수 int 정보 In시 `10기` 형태로 제공
 * **/
@Composable
fun GisuChip(gisuString: String) {
    Box(
        modifier = Modifier
            .background(color = indigo100(), shape = RoundedCornerShape(4.dp))
            .padding(horizontal = 4.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        UText(
            text = gisuString,
            style = UmcTypographyTokens.Caption1Bold,
            color = indigo500()
        )
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = grey000()),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
        ) {

            //X기 활동 상태
            UText(
                text = uiState.activeString,
                style = UmcTypographyTokens.HeadlineBold,
                color = grey950()
            )

            Spacer(
                modifier = Modifier
                    .height(16.dp)
            )

            //상벌점 UI 표
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                //상벌점 2개판
                Surface(
                    modifier = Modifier.weight(2f),
                    shape = RoundedCornerShape(8.dp),

                ) {
                    Row(
                        modifier = Modifier
                            .height(IntrinsicSize.Min)
                        ,
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        //상점
                        ScoreCard(
                            modifier = Modifier
                                .weight(1f),
                            label = AppStrings.REWARD,
                            score = uiState.sangjum,
                            color = green500(),
                        )



                        //벌점
                        ScoreCard(
                            modifier = Modifier
                                .weight(1f),
                            label = AppStrings.PUNISH,
                            score = uiState.buljum,
                            color = red500(),
                        )
                    }
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
    color: Color
) {

    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = grey50()),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            //UButton 컴포저블을 활용하여 일관된 디자인 유지
            Image(
                painter = if(label == AppStrings.REWARD) painterResource(id = R.drawable.ic_like_color) else painterResource(id = R.drawable.ic_dislike_color),
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
            )
            Spacer(
                modifier = Modifier
                    .width(8.dp)
            )

            UText(
                text = label,
                modifier = Modifier,
                style = UmcTypographyTokens.HeadlineBold,
                color = color
            )

            Spacer(
                modifier = Modifier
                    .weight(1f)
            )

            Text(
                text = buildAnnotatedString {
                    //숫자 부분
                    withStyle(
                        style = UmcTypographyTokens.CalloutBold.toSpanStyle().copy(
                            color = grey900(),
                            fontSize = 16.sp
                        )
                    ) {
                        append(score.toString())
                        append(" ")
                    }
                    // point
                    withStyle(
                        style = UmcTypographyTokens.Caption1.toSpanStyle().copy(
                            color = grey600(),
                            fontSize = 12.sp
                        )
                    ) {
                        append("point")
                    }
                }
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

        Row(verticalAlignment = Alignment.CenterVertically) {

            UText(
                text = AppStrings.HOME_PLAN_TITLE,
                style = UmcTypographyTokens.Title3Bold,
                color = grey950()
            )

            Spacer(modifier = Modifier
                .width(4.dp))

            Icon(
                    painter = painterResource(id = R.drawable.ic_add_filled),
                    contentDescription = "Add",
                    tint = grey950(),
                    modifier = Modifier
                        .clickable { onAddClick() }
                )

        }

        Row(verticalAlignment = Alignment.CenterVertically) {

            //뷰 전환 선택기
            Surface(
                color = grey000(),
                shape = RoundedCornerShape(4.dp)
            ) {

                Row(modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    //달력 + 일정 버튼
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                color = if (viewMode == HomeViewMode.CALENDAR) grey800() else grey000()
                            )
                            .clickable { onChangeViewMode(HomeViewMode.CALENDAR) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_home_grid_base),
                            contentDescription = "Calendar Mode",
                            modifier = Modifier.size(26.dp),
                            tint = if (viewMode == HomeViewMode.CALENDAR) grey000() else grey400()
                        )
                    }
                    Spacer(modifier = Modifier
                        .width(8.dp)
                    )

                    //일정 리스트 버튼
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                color = if (viewMode == HomeViewMode.LIST) grey800() else grey000()
                            )
                            .clickable { onChangeViewMode(HomeViewMode.LIST) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_home_list_base),
                            contentDescription = "List Mode",
                            modifier = Modifier.size(26.dp),
                            tint = if (viewMode == HomeViewMode.LIST) grey000() else grey400()
                        )
                    }
                }
            }
        }
    }
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
        gisuTag = persistentListOf("10기", "11기", "12기"),
        viewMode = HomeViewMode.CALENDAR,
        // 필요하다면 가짜 일정 리스트도 추가 가능
        dailyPlans = persistentListOf()
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
        onNoticeClick = {},
        onShareCardClick = {},
    )
}