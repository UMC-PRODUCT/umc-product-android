package com.umc.presentation.home.schedule.add

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.umc.component.component.UButton
import com.umc.component.theme.*
import kotlinx.coroutines.flow.collectLatest
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.umc.component.component.UChip
import com.umc.component.component.UDateTimePickerDialog
import com.umc.component.component.USwitch
import com.umc.component.component.UText
import com.umc.component.component.UTimePickerDialog
import com.umc.component.component.UToast
import com.umc.presentation.home.home.CalendarDatePickerDialog
import com.umc.presentation.home.schedule.dialog.LocationSearchBottomSheet
import com.umc.presentation.home.schedule.dialog.ScheduleCategoryBottomSheet
import com.umc.presentation.home.schedule.dialog.ScheduleChallengerAddBottomSheet
import com.umc.presentation.home.schedule.dialog.ScheduleChallengerAddDialogViewModel


/**TODO. 할 거
 * 1. time이랑 date 분리된거 dateTime으로 획일화
 * 2. API 보내는거 조나단 껄로 변경
 *
 * **/

@Composable
fun ScheduleAddRoute(
    scheduleId : Long,
    onNavigateToBack: () -> Unit,
    viewModel: ScheduleAddViewModel = hiltViewModel(),
    participantViewModel: ScheduleChallengerAddDialogViewModel = hiltViewModel(),
    onShowAttendanceDialog: (onConfirm: () -> Unit, onReject: () -> Unit) -> Unit
){

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current

    val participantUiState by participantViewModel.uiState.collectAsStateWithLifecycle()

    //뒤로 가기 디스패처
    /**TODO. 삭제 - MainActivity에서 적용할 예정**/
    val onBackPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    //다이얼로그 표시 여부 체크
    var showCategoryDialog by remember { mutableStateOf(false) }
    var showLocationDialog by remember {mutableStateOf(false)}
    var showParticipantDialog by remember { mutableStateOf(false) }

    //시작 및 종료 날짜 플래그
    var showStartDateTimePicker by remember { mutableStateOf(false) }
    var showEndDateTimePicker by remember { mutableStateOf(false) }

    //출석 정책용 view 플래그
    var showCheckInStartPicker by remember { mutableStateOf(false) }
    var showOnTimeEndPicker by remember { mutableStateOf(false) }
    var showLateEndPicker by remember { mutableStateOf(false) }


    LaunchedEffect(viewModel){
        viewModel.uiEvent.collectLatest { event ->
            when (event){
                is ScheduleAddEvent.MoveBackPressedEvent -> onBackPressedDispatcher?.onBackPressed()
                is ScheduleAddEvent.ShowErrorToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }

    ScheduleAddScreen(
        uiState = uiState,
        onBackClick = { onBackPressedDispatcher?.onBackPressed() },
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
        onRegisterClick = {
            //운영진 여부 및 수정 모드에 따른 분기 로직
            viewModel.submitPlan(uiState.editMode)

        }
    )

    //다이얼로그 정의
    if (showCategoryDialog) {
        ScheduleCategoryBottomSheet(
            onCategoryClick = viewModel::selectCategory,
            categories = uiState.categories,
            onDismissRequest = { showCategoryDialog = false },
            onConfirm = { showCategoryDialog = false }
        )
    }

    if (showLocationDialog) {
        LocationSearchBottomSheet(
            onDismissRequest = { showLocationDialog = false },
            onLocationSelected = {
                viewModel.updatePlanLocation(it)
                showLocationDialog = false
            }
        )
    }

    if (showParticipantDialog) {
        ScheduleChallengerAddBottomSheet(
            searchQuery = participantUiState.searchQuery,
            isSearching = participantUiState.isSearching,
            isLoading = participantUiState.isLoading,
            hasNext = participantUiState.hasNext,
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

    if (showStartDateTimePicker) {
        UDateTimePickerDialog(
            onConfirm = { utcDateTime ->
                viewModel.updateStartDateTime(utcDateTime)
                showStartDateTimePicker = false
            },
            onDismiss = { showStartDateTimePicker = false }
        )
    }

    if (showEndDateTimePicker) {
        UDateTimePickerDialog(
            onConfirm = { utcDateTime ->
                viewModel.updateEndDateTime(utcDateTime)
                showEndDateTimePicker = false
            },
            onDismiss = { showEndDateTimePicker = false }
        )
    }

    if (showCheckInStartPicker) {
        UDateTimePickerDialog(
            onConfirm = { utcDateTime ->
                viewModel.updateCheckInStartDateTime(utcDateTime)
                showCheckInStartPicker = false
            },
            onDismiss = { showCheckInStartPicker = false }
        )
    }

    if (showOnTimeEndPicker) {
        UDateTimePickerDialog(
            onConfirm = { utcDateTime ->
                viewModel.updateOnTimeEndDateTime(utcDateTime)
                showOnTimeEndPicker = false
            },
            onDismiss = { showOnTimeEndPicker = false }
        )
    }

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
            .background(grey000())

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
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier
                .height(16.dp)
            )

            //3. 일정 제목
            ScheduleInputSection(
                title = AppStrings.HOME_PLAN_ADD_PLAN_NAME,
                required = true
            ) {
                OutlinedTextField(
                    value = uiState.planTitle,
                    onValueChange = onTitleChanged,
                    modifier = Modifier
                        .fillMaxWidth(),
                    placeholder = { Text(
                        AppStrings.HOME_PLAN_ADD_PLAN_NAME_PLACEHOLDER,
                        color = grey400()
                    ) },
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = grey300(),
                        focusedBorderColor = indigo500()
                    )
                )
            }

            Spacer(modifier = Modifier
                .height(32.dp)
            )
            
            //4. 태그
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

            //5. 일시
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

            //6. 장소
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


                if(!uiState.isOnlineChecked) {
                    Spacer(
                        modifier = Modifier
                            .height(8.dp)
                    )

                    //content()
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
                OutlinedTextField(
                    value = uiState.planDetail,
                    onValueChange = onDetailChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(144.dp),
                    placeholder = { Text(AppStrings.HOME_PLAN_ADD_PLAN_DETAIL_PLACEHOLDER, color = grey400()) },
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = grey300(),
                        focusedBorderColor = indigo500()
                    )
                )
            }

            Spacer(modifier = Modifier
                .height(32.dp)
            )

            //8. 챌린저 명단
            ScheduleInputSection(title = AppStrings.HOME_PLAN_ADD_PLAN_ATTEND, required = false) {
                SelectableField(
                    text = if (!uiState.isSelectedParticipant) AppStrings.HOME_PLAN_ADD_PLAN_CHALLENGER_PLACEHOLDER else uiState.selectedParticipantsString,
                    isPlaceholder = !uiState.isSelectedParticipant,
                    onClick = onParticipantClick
                )
            }

            //9. 출석부 생성
            if(true) {
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

                if(uiState.isAttendanceChecked){
                    AttendanceDateCard(
                        uiState = uiState,
                        onCheckInStartClick = onCheckInDateTimeClick,
                        onOnTimeEndClick = onOnDateTimeEndClick,
                        onLateEndClick = onLateDateTimeClick
                    )
                }


            }

            Spacer(
                modifier = Modifier
                    .height(64.dp)
            )

            //10. 하단 버튼들
            /*
            ScheduleAddActionButtons(
                registerOk = uiState.isRegisterOk,
                editMode = uiState.editMode,
                onCancelClick = onBackClick,
                onRegisterClick = onRegisterClick
            )

            Spacer(modifier = Modifier
                .height(64.dp)
            )

             */

        }


    }

}

/**상단 top bar**/
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

        Icon(
            modifier = Modifier
                .padding(12.dp)
                .clickable { onBackClick() },
            painter = painterResource(id = R.drawable.ic_back),
            contentDescription = null,
            tint = Color.Unspecified,
        )


        UText(
            text = AppStrings.HOME_PLAN_ADD_TITLE,
            style = UmcTypographyTokens.Title2Bold,
            color = grey800()
        )

        Spacer(modifier = Modifier.weight(1f))

        UText(
            text = if (editMode) AppStrings.EDIT else AppStrings.REGISTER,
            style = UmcTypographyTokens.HeadlineBold,
            color = if (registerOk) indigo500() else grey400(),
            modifier = Modifier
                .clickable(enabled = registerOk) { onRegisterClick() }
                .padding(horizontal = 22.dp)
            ,
        )

    }
}

/**스케쥴에서 필요한 제목을 작성하는 섹션**/
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

/**터치하여 선택하는 textField**/
@Composable
fun SelectableField(text: String, isPlaceholder: Boolean, onClick: () -> Unit, isDisabled: Boolean = false) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isDisabled) { onClick() },
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, if (isDisabled) grey200() else grey300()),
        color = if (isDisabled) grey100() else grey000()
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

/**출석부 생성 시 필요 field들의 집합**/
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
        color = grey000()
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

/**출석부 생성 시 1개의 field를 담당하는 컴포지블 함수
 *
 *
 * **/
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
                UChip(text = "${dateText} · ${timeText}",
                    backgroundColor = indigo100(),
                    borderColor = grey200(),
                    borderWidth = 0.dp,
                    textColor = indigo500(),
                    textStyle = UmcTypographyTokens.SubheadlineBold,
                    onClick = {onDateTimeClick()}

                )
            }
        }



    }
}


/**일정 등록&취소 버튼**/
@Composable
fun ScheduleAddActionButtons(
    registerOk: Boolean,
    editMode: Boolean,
    onCancelClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
    Row(modifier = Modifier
        .fillMaxWidth()
    ) {
        UButton(
            text = AppStrings.CANCEL,
            modifier = Modifier
                .weight(1f)
                .height(52.dp),
            backgroundColor = grey000(),
            borderColor = grey300(),
            borderWidth = 1.dp,
            textColor = grey800(),
            onClick = onCancelClick
        )
        Spacer(modifier = Modifier
            .width(16.dp)
        )
        UButton(
            text = if (editMode) AppStrings.EDIT else AppStrings.REGISTER,
            modifier = Modifier
                .weight(1f)
                .height(52.dp),
            backgroundColor = if (registerOk) indigo500() else grey300(),
            textColor = grey000(),
            onClick = onRegisterClick
        )
    }
}



@Preview(showBackground = true)
@Composable
private fun ScheduleAddScreenPreview() {
    //ScheduleAddScreen()
}