package com.umc.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeCompilerApi
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.umc.component.theme.neutral100
import com.umc.domain.model.enums.HomeViewMode
import com.umc.domain.model.home.SchedulePlanItem
import kotlinx.coroutines.flow.collectLatest
import java.time.YearMonth

@Composable
fun HomeRoute(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToNotice: () -> Unit,
    onNavigateToNotification: () -> Unit,
    onNavigateToPlanDetail: (SchedulePlanItem) -> Unit,
    onNavigateToPlanAdd: () -> Unit,
) {

    val uiState by viewModel.uiState.collectAsState()

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
                is HomeEvent.MovePlanDetailEvent -> onNavigateToPlanDetail(event.plan)
                //일정 추가 이동
                is HomeEvent.MovePlanAddEvent -> onNavigateToPlanAdd()
                else -> {}
            }
        }
    }

    HomeScreen(
        uiState = uiState,
        onDateClick = viewModel::setSelectedDate,
        onMonthChange = { month -> viewModel.getScheduleMonth(month.year, month.monthValue) },
        onChangeViewMode = viewModel::onChangeViewMode,
        onNotificationClick = viewModel::onClickNotification,
        onPlanAddClick = viewModel::onClickPlanAdd,
        onPlanDetailClick = viewModel::onClickPlanDetail,
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
    onPlanAddClick: () -> Unit,
    onPlanDetailClick: (SchedulePlanItem) -> Unit,
    onNoticeClick: () -> Unit,
) {

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(neutral100())
            .verticalScroll(scrollState)
    ){
        //1. 상단 섹션 (Topbar, userCard, 활동 상태)
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    HomeScreen()
}