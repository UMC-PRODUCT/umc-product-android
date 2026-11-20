package com.umc.presentation.home.schedule.add

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.umc.component.R
import com.umc.component.theme.*
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.umc.component.component.UChip
import com.umc.component.component.UDateTimePickerDialog
import com.umc.component.component.USwitch
import com.umc.component.component.UText
import com.umc.component.component.UTextField
import com.umc.component.component.UToastData
import com.umc.component.component.UToastHost
import com.umc.component.component.UToastState
import com.umc.presentation.home.schedule.dialog.LocationSearchBottomSheet
import com.umc.presentation.home.schedule.dialog.LocationSearchEvent
import com.umc.presentation.home.schedule.dialog.LocationSearchViewModel
import com.umc.presentation.home.schedule.dialog.ScheduleCategoryBottomSheet
import com.umc.presentation.home.schedule.dialog.ScheduleChallengerAddBottomSheet
import com.umc.presentation.home.schedule.dialog.ScheduleChallengerAddDialogViewModel
import com.umc.component.base.CollectUiEvents



/**
 * 일정 등록·수정 화면.
 *
 * 수정할 일정 id 는 ViewModel 이 SavedStateHandle 에서 읽으므로 [scheduleId] 와 [onShowAttendanceDialog] 는 쓰지 않는다.
 * 호출부(MainNavHost)가 아직 넘기고 있어 기본값을 두고 시그니처만 남긴다.
 */
@Composable
fun ScheduleAddRoute(
    scheduleId : Long = -1L,
    onNavigateToBack: () -> Unit,
    viewModel: ScheduleAddViewModel = hiltViewModel(),
    participantViewModel: ScheduleChallengerAddDialogViewModel = hiltViewModel(),
    locationViewModel: LocationSearchViewModel = hiltViewModel(),
    onShowAttendanceDialog: (onConfirm: () -> Unit, onReject: () -> Unit) -> Unit = { _, _ -> }
){

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val participantUiState by participantViewModel.uiState.collectAsStateWithLifecycle()

    val locationUiState by locationViewModel.uiState.collectAsStateWithLifecycle()

    //바텀시트 및 다이얼로그 노출 상태 제어 플래그 변수들
    var showCategoryDialog by remember { mutableStateOf(false) }
    var showLocationDialog by remember {mutableStateOf(false)}
    var showParticipantDialog by remember { mutableStateOf(false) }

    //시작 및 종료 날짜 timePicker dialog 플래그
    var showStartDateTimePicker by remember { mutableStateOf(false) }
    var showEndDateTimePicker by remember { mutableStateOf(false) }

    //출석 정책용 날짜 timePicker dialog 플래그
    var showCheckInStartPicker by remember { mutableStateOf(false) }
    var showOnTimeEndPicker by remember { mutableStateOf(false) }
    var showLateEndPicker by remember { mutableStateOf(false) }

    var toastData by remember { mutableStateOf<UToastData?>(null) }

    // 장소 검색 시트는 별도 창이라 화면 토스트가 가려진다. 시트 안에 그릴 토스트를 따로 둔다
    var locationToastData by remember { mutableStateOf<UToastData?>(null) }


    CollectUiEvents(viewModel.uiEvent) { event ->
        when (event){
            is ScheduleAddEvent.MoveBackPressedEvent -> onNavigateToBack()
            is ScheduleAddEvent.ShowErrorToast -> {
                toastData = UToastData(event.message, UToastState.ERROR)
            }
            is ScheduleAddEvent.ShowCheckInAfterStartToast -> {
                toastData = UToastData(AppStrings.HOME_PLAN_ADD_ATTENDANCE_CHECKIN_AFTER_START, UToastState.ERROR)
            }
            is ScheduleAddEvent.ShowDateTimeRequiredToast -> {
                toastData = UToastData(AppStrings.HOME_PLAN_ADD_DATETIME_REQUIRED, UToastState.ERROR)
            }
            is ScheduleAddEvent.ShowAttendanceTimeRequiredToast -> {
                toastData = UToastData(AppStrings.HOME_PLAN_ADD_ATTENDANCE_TIME_REQUIRED, UToastState.ERROR)
            }
            else -> {}
        }
    }

    CollectUiEvents(locationViewModel.uiEvent) { event ->
        // 시트가 닫힌 뒤 늦게 도착한 결과는 다음에 시트를 열 때 뜨지 않도록 버린다
        if (!showLocationDialog) return@CollectUiEvents
        locationToastData = when (event) {
            is LocationSearchEvent.ShowSearchEmptyToast ->
                UToastData(AppStrings.HOME_LOCATION_SEARCH_EMPTY, UToastState.NONE)
            is LocationSearchEvent.ShowSearchFailedToast ->
                UToastData(AppStrings.HOME_LOCATION_SEARCH_FAILED, UToastState.ERROR)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        ScheduleAddScreen(
            uiState = uiState,
            onBackClick = onNavigateToBack,
            onTitleChanged = viewModel::updatePlanTitle,
            onDetailChanged = viewModel::updatePlanDetail,
            onAlldayChanged = viewModel::setAllday,
            onCategoryClick = { showCategoryDialog = true },
            onLocationClick = { showLocationDialog = true },
            onParticipantClick = {
                //일정 추가에 있는 챌린저 정보를 다이얼로그 뷰모델에 전달(챌린저 추가/삭제 유지)
                participantViewModel.setSelectedParticipant(uiState.selectedParticipants)
                showParticipantDialog = true
                                 },
            onStartDateTimeClick = { showStartDateTimePicker = true },
            onEndDateTimeClick = { showEndDateTimePicker = true },
            onOnlineChanged = viewModel::toggleOnlineCheck, //비대면 토글
            onAttendanceChanged = viewModel::toggleAttendanceCheck, //출석부 토글
            onCheckInDateTimeClick = {showCheckInStartPicker = true},
            onOnDateTimeEndClick = {showOnTimeEndPicker = true},
            onLateDateTimeClick = {showLateEndPicker = true},
            onRegisterClick = viewModel::submitPlan
        )

        UToastHost(
            data = toastData,
            onDismiss = { toastData = null },
            // 입력 중에 등록을 눌러도 키보드에 가리지 않게 키보드 위로 올린다
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .imePadding()
                .padding(bottom = 56.dp),
        )
    }

    //카테고리(태그) 선택 바텀시트 컴포저블
    if (showCategoryDialog) {
        ScheduleCategoryBottomSheet(
            onCategoryClick = viewModel::selectCategory,
            categories = uiState.categories,
            onDismissRequest = { showCategoryDialog = false },
            onConfirm = { showCategoryDialog = false }
        )
    }

    // 장소 검색 바텀시트 컴포저블
    if (showLocationDialog) {
        LocationSearchBottomSheet(
            searchQuery = locationUiState.searchQuery,
            recentSearchList = locationUiState.recentSearchList,
            searchResultList = locationUiState.searchResultList,
            toastData = locationToastData,
            onToastDismiss = { locationToastData = null },
            onQueryChanged = locationViewModel::onQueryChanged,
            onSearch = locationViewModel::searchLocation,
            onDismissRequest = {
                showLocationDialog = false
                locationToastData = null
            },
            onLocationSelected = {
                viewModel.updatePlanLocation(it)
                showLocationDialog = false
                locationToastData = null
            }
        )
    }

    // 참여 챌린저 검색 및 다중 선택 바텀시트 컴포저블
    if (showParticipantDialog) {
        ScheduleChallengerAddBottomSheet(
            searchQuery = participantUiState.searchQuery,
            isSearching = participantUiState.isSearching,
            isLoading = participantUiState.isLoading,
            hasNext = participantUiState.hasNext,
            hasSearched = participantUiState.hasSearched,
            isSearchFailed = participantUiState.isSearchFailed,
            selectedParticipants = participantUiState.selectedParticipants,
            selectedParticipantsString = participantUiState.selectedParticipantsString,
            searchResults = participantUiState.searchResults,

            onQueryChanged = participantViewModel::searchParticipants,
            onLoadMore = participantViewModel::loadMoreParticipants,
            onToggleParticipant = participantViewModel::toggleParticipant,
            onConfirm = { finalParticipants, summaryString ->
                //다이얼로그가 들고 있던최종 명단을
                //메인 뷰모델 내부로 전달하며 창을 종료
                viewModel.updateParticipants(finalParticipants, summaryString)
                showParticipantDialog = false
            },
            onDismissRequest = {
                participantViewModel.clearParticipantSearch()
                showParticipantDialog = false
            }
        )
    }

    // 일정 시작 일시 선택 다이얼로그 컴포저블
    if (showStartDateTimePicker) {
        UDateTimePickerDialog(
            onConfirm = { utcDateTime ->
                viewModel.updateStartDateTime(utcDateTime)
                showStartDateTimePicker = false
            },
            onDismiss = { showStartDateTimePicker = false },
            isStartTime = true,
            isAllday = uiState.isAllDay
        )
    }

    // 일정 종료 일시 선택 다이얼로그 컴포저블
    if (showEndDateTimePicker) {
        UDateTimePickerDialog(
            onConfirm = { utcDateTime ->
                viewModel.updateEndDateTime(utcDateTime)
                showEndDateTimePicker = false
            },
            onDismiss = { showEndDateTimePicker = false },
            isStartTime = false,
            isAllday = uiState.isAllDay
        )
    }

    // 출석 체크인 시작 일시 선택 다이얼로그 컴포저블
    if (showCheckInStartPicker) {
        UDateTimePickerDialog(
            onConfirm = { utcDateTime ->
                viewModel.updateCheckInStartDateTime(utcDateTime)
                showCheckInStartPicker = false
            },
            onDismiss = { showCheckInStartPicker = false }
        )
    }

    // 출석 정시 종료 일시 선택 다이얼로그 컴포저블
    if (showOnTimeEndPicker) {
        UDateTimePickerDialog(
            onConfirm = { utcDateTime ->
                viewModel.updateOnTimeEndDateTime(utcDateTime)
                showOnTimeEndPicker = false
            },
            onDismiss = { showOnTimeEndPicker = false }
        )
    }

    // 출석 지각 종료 일시 선택 다이얼로그 컴포저블
    if(showLateEndPicker){
        UDateTimePickerDialog(
            onConfirm = { utcDateTime ->
                viewModel.updateLateEndDateTime(utcDateTime)
                showLateEndPicker = false
            },
            onDismiss = { showLateEndPicker = false }
        )
    }

}

@Composable
fun ScheduleAddScreen(
    uiState: ScheduleAddUiState,
    onBackClick: () -> Unit, //뒤로 가기를 누를 때
    onTitleChanged: (String) -> Unit, //일정 제목이 바뀔 때 (ViewModel)
    onDetailChanged: (String) -> Unit, //일정 상세 내용이 바뀔 때
    onAlldayChanged: (Boolean) -> Unit, //하루 종일 여부가 바뀔 때
    onCategoryClick: () -> Unit, //일정 태그(카테고리)를 누를 때
    onLocationClick: () -> Unit, //일정 장소를 누를 때
    onParticipantClick: () -> Unit, //일정 참여자를 누를 때
    onStartDateTimeClick: () -> Unit, //일정 시작 날짜를 수정 시
    onEndDateTimeClick: () -> Unit, //일정 종료 날짜 수정 시
    onCheckInDateTimeClick: () -> Unit, //체크인 시작 시각 수정 시
    onOnDateTimeEndClick: () -> Unit, //정시 종료 시각 수정 시
    onLateDateTimeClick: () -> Unit, //지각 종료 시각 수정 시

    onOnlineChanged: (Boolean) -> Unit, //온라인 여부가 바뀔 때
    onAttendanceChanged: (Boolean) -> Unit, //출석부 만들기 여부가 바뀔 때

    onRegisterClick: () -> Unit //일정 등록 시
){

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(grey50())

    ) {
        //1. 상단 바
        ScheduleAddTopBar(
            onBackClick = onBackClick,
            registerOk = uiState.isRegisterOk,
            editMode = uiState.editMode,
            onRegisterClick = onRegisterClick
            )

        //2. 일정 입력 영역(스크롤)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .verticalScroll(scrollState)
        ) {

            //3. 일정 제목
            ScheduleInputSection(
                title = AppStrings.HOME_PLAN_ADD_PLAN_NAME,
                required = true
            ) {
                ScheduleOutlinedField(
                    value = uiState.planTitle,
                    onValueChange = onTitleChanged,
                    placeholder = AppStrings.HOME_PLAN_ADD_PLAN_NAME_PLACEHOLDER,
                    // 이전 OutlinedTextField 의 최소 높이(56dp)를 유지한다
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp),
                )
            }

            Spacer(modifier = Modifier
                .height(32.dp)
            )
            
            //4. 카테고리 태그 선택
            ScheduleInputSection(
                title = AppStrings.HOME_PLAN_ADD_PLAN_CATEGORY,
                required = true
            ) {
                SelectableField(
                    text = if (!uiState.isSelectedCategory) AppStrings.HOME_PLAN_ADD_PLAN_TAG_PLACEHOLDER else uiState.selectedCategoriesString,
                    isPlaceholder = !uiState.isSelectedCategory,
                    onClick = onCategoryClick
                )
            }

            Spacer(modifier = Modifier
                .height(32.dp)
            )

            //5. 일시 및 하루종일 선택
            ScheduleInputSection(
                title = AppStrings.HOME_PLAN_DETAIL_CALENDAR,
                required = false
            ) {
                ScheduleDateCard(
                    uiState = uiState,
                    onAlldayChanged = onAlldayChanged,
                    onStartDateTimeClick = onStartDateTimeClick,
                    onEndDateTimeClick = onEndDateTimeClick,
                )
            }

            Spacer(modifier = Modifier
                .height(32.dp)
            )

            //6. 대면/비대면 토글 및 장소 선택
            Column(modifier = Modifier
                .fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    UText(
                        text = AppStrings.HOME_PLAN_DETAIL_LOCATION,
                        style = UmcTypographyTokens.HeadlineBold,
                        color = grey800()
                    )

                    UText(text = "*",
                        style = UmcTypographyTokens.HeadlineBold,
                        color = red500(),
                        modifier = Modifier.padding(start = 4.dp)
                    )

                }

                Spacer(modifier = Modifier
                    .height(8.dp)
                )

                //대면 비대면 여부
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    USwitch(
                        checked = !uiState.isOnlineChecked,
                        onCheckedChange = { isOffline ->
                            onOnlineChanged(!isOffline)
                        },
                    )
                    Spacer(modifier = Modifier.
                    width(8.dp)
                    )
                    UText(text = AppStrings.HOME_PLAN_ADD_ONLINE_TITLE, style = UmcTypographyTokens.Body, color = grey900())

                }

                // 대면 일정일 때만 장소 검색 필드 노출
                if(!uiState.isOnlineChecked) {
                    Spacer(
                        modifier = Modifier
                            .height(8.dp)
                    )
                    
                    SelectableField(
                        text = if (uiState.planLocation.isEmpty()) AppStrings.HOME_PLAN_ADD_PLAN_LOCATION_PLACEHOLDER else uiState.planLocation,
                        isPlaceholder = uiState.planLocation.isEmpty(),
                        onClick = onLocationClick,
                        isDisabled = uiState.isOnlineChecked
                    )
                }
            }


            Spacer(modifier = Modifier
                .height(32.dp)
            )


            //7. 상세 안내
            ScheduleInputSection(title = AppStrings.HOME_PLAN_ADD_PLAN_DETAIL_INFORMATION, required = false) {
                ScheduleOutlinedField(
                    value = uiState.planDetail,
                    onValueChange = onDetailChanged,
                    placeholder = AppStrings.HOME_PLAN_ADD_PLAN_DETAIL_PLACEHOLDER,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(144.dp),
                )
            }

            Spacer(modifier = Modifier
                .height(32.dp)
            )

            //8. 챌린저 명단 선택
            ScheduleInputSection(title = AppStrings.HOME_PLAN_ADD_PLAN_ATTEND, required = false) {
                SelectableField(
                    text = if (!uiState.isSelectedParticipant) AppStrings.HOME_PLAN_ADD_PLAN_CHALLENGER_PLACEHOLDER else uiState.selectedParticipantsString,
                    isPlaceholder = !uiState.isSelectedParticipant,
                    onClick = onParticipantClick
                )
            }

            // 9. 출석부 생성 토글 및 출석 시간 카드 설정
            Spacer(modifier = Modifier
                .height(8.dp)
            )
            //출석부 생성 여부
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                USwitch(
                    checked = uiState.isAttendanceChecked,
                    onCheckedChange = onAttendanceChanged,
                )
                Spacer(
                    modifier = Modifier.width(8.dp)
                )
                UText(
                    text = AppStrings.HOME_PLAN_ADD_ATTENDANCE_TITLE,
                    style = UmcTypographyTokens.Body,
                    color = grey900()
                )

            }

            Spacer(
                modifier = Modifier
                    .height(8.dp)
            )

            // 출석부 생성 활성화 시 체크인/정시/지각 시간 설정 카드 노출
            if(uiState.isAttendanceChecked){
                AttendanceDateCard(
                    uiState = uiState,
                    onCheckInStartClick = onCheckInDateTimeClick,
                    onOnTimeEndClick = onOnDateTimeEndClick,
                    onLateEndClick = onLateDateTimeClick
                )
            }

            Spacer(
                modifier = Modifier
                    .height(64.dp)
            )

        }


    }

}

/**
 * 일정 작성 상단 탑바 컴포저블
 *
 * 뒤로가기 버튼, 일정 등록/수정 완료 버튼을 표시
 */
@Composable
fun ScheduleAddTopBar(
    onBackClick: () -> Unit,
    registerOk: Boolean,
    editMode: Boolean,
    onRegisterClick: () -> Unit
){

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // 여백까지 누를 수 있게 clickable 을 padding 앞에 둔다(48dp 터치 영역)
        Icon(
            modifier = Modifier
                .clickable { onBackClick() }
                .padding(12.dp),
            painter = painterResource(id = R.drawable.ic_back),
            contentDescription = AppStrings.COMMON_BACK,
            tint = Color.Unspecified,
        )


        UText(
            text = AppStrings.HOME_PLAN_ADD_TITLE,
            style = UmcTypographyTokens.Title2Bold,
            color = grey800()
        )

        Spacer(modifier = Modifier.weight(1f))

        UText(
            text = if (editMode) AppStrings.COMMON_EDIT else AppStrings.COMMON_REGISTER,
            style = UmcTypographyTokens.HeadlineBold,
            color = if (registerOk) main500() else grey400(),
            modifier = Modifier
                .clickable(enabled = registerOk) { onRegisterClick() }
                .padding(horizontal = 22.dp)
            ,
        )

    }
}

/**
 * 일정 입력 항목의 제목과 필수 입력 여부를
 * 렌더링하는 공통 wrapper 컴포저블
 */
@Composable
fun ScheduleInputSection(
    title: String,
    required: Boolean,
    content: @Composable () -> Unit //하위 컴포지블 작성
) {
    Column(modifier = Modifier
        .fillMaxWidth()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            UText(
                text = title,
                style = UmcTypographyTokens.HeadlineBold,
                color = grey800()
            )
            if (required) {
                UText(text = "*",
                    style = UmcTypographyTokens.HeadlineBold,
                    color = red500(),
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
        Spacer(modifier = Modifier
            .height(16.dp)
        )

        content()
    }
}

/**
 * 일정 제목·상세 안내 입력칸.
 *
 * 이전 M3 OutlinedTextField 와 같은 모양이 되도록 UTextField 를 맞춘다.
 * 여백 16dp, 위쪽 정렬, 투명 배경, 테마의 onSurface 글자색과 primary 커서, 포커스 시 2dp 테두리다.
 */
@Composable
private fun ScheduleOutlinedField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    UTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        placeholder = placeholder,
        textStyle = UmcTypographyTokens.Body,
        textColor = MaterialTheme.colorScheme.onSurface,
        backgroundColor = Color.Transparent,
        strokeColor = grey300(),
        focusStrokeColor = main500(),
        cornerRadius = 8.dp,
        contentPadding = PaddingValues(16.dp),
        cursorColor = MaterialTheme.colorScheme.primary,
        borderWidth = if (isFocused) 2.dp else 1.dp,
        interactionSource = interactionSource,
        verticalAlignment = Alignment.Top,
    )
}

/**
 * 터치 시 다이얼로그나 바텀시트를 호출하는
 * 텍스트 필드 형태의 컴포저블
 */
@Composable
fun SelectableField(text: String, isPlaceholder: Boolean, onClick: () -> Unit, isDisabled: Boolean = false) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isDisabled) { onClick() },
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, if (isDisabled) grey200() else grey300()),
        color = if (isDisabled) grey100() else grey50()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            UText(
                text = text,
                modifier = Modifier
                    .weight(1f),
                style = UmcTypographyTokens.Body,
                color = if (isPlaceholder) grey400() else grey800(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Icon(painter = painterResource(id = R.drawable.ic_arrow_next), contentDescription = null, tint = grey400())
        }
    }
}

/**
 * 출석부 생성 시 체크인, 정시 종료, 지각 종료 시간을
 * 설정할 수 있는 카드 컴포저블
 */
@Composable
fun AttendanceDateCard(
    uiState: ScheduleAddUiState,
    onCheckInStartClick: () -> Unit,
    onOnTimeEndClick: () -> Unit,
    onLateEndClick: () -> Unit,
){
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, grey300()),
        color = grey50()
    ) {
        Column {
            //체크인 시간
            AttendanceTimeRow(
                label = AppStrings.HOME_PLAN_ADD_ATTENDANCE_CHECKIN_TITLE,
                dateText = uiState.checkInStartDateText,
                timeText = uiState.checkInStartTimeText,
                onDateTimeClick = {onCheckInStartClick()}
            )

            HorizontalDivider(color = grey300())

            //정시 종료 시간
            AttendanceTimeRow(
                label = AppStrings.HOME_PLAN_ADD_ATTENDANCE_ONTIMEEND_TITLE,
                dateText = uiState.onTimeEndDateText,
                timeText = uiState.onTimeEndTimeText,
                onDateTimeClick = {onOnTimeEndClick()}
            )

            //지각 종료 시간
            HorizontalDivider(color = grey300())

            AttendanceTimeRow(
                label = AppStrings.HOME_PLAN_ADD_ATTENDANCE_LATEEND_TITLE,
                dateText = uiState.lateEndDateText,
                timeText = uiState.lateEndTimeText,
                onDateTimeClick = {onLateEndClick()}
            )
        }

    }
}

/**
 * 출석부 설정 카드의 개별 시간 항목(체크인, 정시, 지각)을
 * 표시하는 컴포저블
 */
@Composable
fun AttendanceTimeRow(
    label: String,
    dateText: String,
    timeText: String,
    onDateTimeClick: () -> Unit,
){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onDateTimeClick()}
            .padding(horizontal = 16.dp)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,

        ) {
        UText(text = label, style = UmcTypographyTokens.Callout, color = grey600())

        Spacer(modifier = Modifier
            .weight(1f)
        )

        Box(
            modifier = Modifier.heightIn(min = 32.dp), // UChip의 일반적인 높이
            contentAlignment = Alignment.Center // 내부 콘텐츠를 항상 가운데 정렬
        ){
            if(dateText == ""){
                Icon(
                    painter = painterResource(id = R.drawable.ic_next),
                    contentDescription = null,
                    tint = grey400()
                )
            }
            else{
                UChip(text = AppStrings.HOME_SCHEDULE_DATE_TIME_FORMAT.format(dateText, timeText),
                    backgroundColor = main100(),
                    borderColor = grey200(),
                    borderWidth = 0.dp,
                    textColor = main500(),
                    textStyle = UmcTypographyTokens.SubheadlineBold,
                    onClick = {onDateTimeClick()}

                )
            }
        }



    }
}

@Preview(showBackground = true)
@Composable
internal fun ScheduleAddScreenPreview() {
    ScheduleAddScreen(
        uiState = ScheduleAddUiState(planTitle = "UMC 정기 세션"),
        onBackClick = {},
        onTitleChanged = {},
        onDetailChanged = {},
        onAlldayChanged = {},
        onCategoryClick = {},
        onLocationClick = {},
        onParticipantClick = {},
        onStartDateTimeClick = {},
        onEndDateTimeClick = {},
        onCheckInDateTimeClick = {},
        onOnDateTimeEndClick = {},
        onLateDateTimeClick = {},
        onOnlineChanged = {},
        onAttendanceChanged = {},
        onRegisterClick = {},
    )
}
