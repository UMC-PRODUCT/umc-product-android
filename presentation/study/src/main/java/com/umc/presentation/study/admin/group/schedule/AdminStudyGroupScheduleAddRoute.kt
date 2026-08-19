package com.umc.presentation.study.admin.group.schedule

import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.umc.component.component.UDateTimePickerDialog
import com.umc.presentation.study.admin.group.schedule.bottomsheet.GroupScheduleChallengerBottomSheet
import com.umc.presentation.study.admin.group.schedule.bottomsheet.GroupScheduleLocationBottomSheet
import com.umc.presentation.study.admin.submit.bottomsheet.AdminSubmitWeekBottomSheet
import kotlinx.coroutines.flow.collectLatest

/**
 * 스터디 그룹 일정 등록 화면의 Route
 *
 * ViewModel과 Screen을 연결하고,
 * 날짜/시간 다이얼로그 및 각종 BottomSheet 표시 상태를 관리합니다.
 */
@Composable
fun AdminStudyGroupScheduleRoute(
    groupId: Long,
    groupTitle: String,
    groupPart: String,
    viewModel: AdminStudyGroupScheduleViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()

    val context = LocalContext.current

    /**
     * 전달받은 스터디 그룹 정보를 ViewModel에 초기화
     */
    LaunchedEffect(
        groupId,
        groupTitle,
        groupPart,
    ) {
        viewModel.initializeGroup(
            groupId = groupId,
            groupTitle = groupTitle,
            groupPart = groupPart,
        )
    }

    // 일정 시작 날짜/시간 선택 Dialog
    var showStartPicker by remember {
        mutableStateOf(false)
    }

    // 일정 종료 날짜/시간 선택 Dialog
    var showEndPicker by remember {
        mutableStateOf(false)
    }

    // 출석 시작 시간 선택 Dialog
    var showCheckInStartPicker by remember {
        mutableStateOf(false)
    }

    // 정상 출석 종료 시간 선택 Dialog
    var showOnTimeEndPicker by remember {
        mutableStateOf(false)
    }

    // 지각 인정 종료 시간 선택 Dialog
    var showLateEndPicker by remember {
        mutableStateOf(false)
    }

    // 장소 선택 BottomSheet
    var showLocationBottomSheet by remember {
        mutableStateOf(false)
    }

    // 챌린저 선택 BottomSheet
    var showChallengerBottomSheet by remember {
        mutableStateOf(false)
    }

    // 주차 선택 BottomSheet
    var showWeekBottomSheet by remember {
        mutableStateOf(false)
    }

    /**
     * ViewModel에서 발생하는 일회성 이벤트 처리
     */
    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                AdminStudyGroupScheduleEvent.NavigateBack -> {
                    onNavigateBack()
                }

                is AdminStudyGroupScheduleEvent.ShowToast -> {
                    Toast.makeText(
                        context,
                        event.message,
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
        }
    }

    /**
     * 실제 일정 등록 화면
     */
    AdminStudyGroupScheduleScreen(
        state = state,
        onAction = { action ->
            when (action) {
                // 일정 시작일 선택
                AdminStudyGroupScheduleAction.ClickStartDateTime -> {
                    showStartPicker = true
                }

                // 일정 종료일 선택
                AdminStudyGroupScheduleAction.ClickEndDateTime -> {
                    showEndPicker = true
                }

                // 출석 시작 시간 선택
                AdminStudyGroupScheduleAction.ClickCheckInStart -> {
                    showCheckInStartPicker = true
                }

                // 정상 출석 종료 시간 선택
                AdminStudyGroupScheduleAction.ClickOnTimeEnd -> {
                    showOnTimeEndPicker = true
                }

                // 지각 인정 종료 시간 선택
                AdminStudyGroupScheduleAction.ClickLateEnd -> {
                    showLateEndPicker = true
                }

                // 장소 선택
                AdminStudyGroupScheduleAction.ClickPlace -> {
                    showLocationBottomSheet = true
                }

                // 챌린저 선택
                AdminStudyGroupScheduleAction.ClickChallenger -> {
                    showChallengerBottomSheet = true
                }

                // 주차 선택
                AdminStudyGroupScheduleAction.ClickWeek -> {
                    showWeekBottomSheet = true
                }

                /**
                 * 위의 UI 표시 액션을 제외한 나머지는
                 * ViewModel에서 처리
                 */
                else -> {
                    viewModel.onAction(action)
                }
            }
        }
    )

    /**
     * 일정 시작 날짜/시간 선택
     *
     * 하루 종일 OFF
     * -> 날짜 + 시간 입력
     *
     * 하루 종일 ON
     * -> 날짜만 입력
     * -> 내부 시간은 00:00:00.000으로 설정
     */
    if (showStartPicker) {
        UDateTimePickerDialog(
            isAllday = state.isAllDay,
            isStartTime = true,
            onConfirm = { utcDateTime ->
                viewModel.updateStartDateTime(
                    utcDateTime
                )

                showStartPicker = false
            },
            onDismiss = {
                showStartPicker = false
            }
        )
    }

    /**
     * 일정 종료 날짜/시간 선택
     *
     * 하루 종일 OFF
     * -> 날짜 + 시간 입력
     *
     * 하루 종일 ON
     * -> 날짜만 입력
     * -> 내부 시간은 23:59:59.999로 설정
     */
    if (showEndPicker) {
        UDateTimePickerDialog(
            isAllday = state.isAllDay,
            isStartTime = false,
            onConfirm = { utcDateTime ->
                viewModel.updateEndDateTime(
                    utcDateTime
                )

                showEndPicker = false
            },
            onDismiss = {
                showEndPicker = false
            }
        )
    }

    /**
     * 출석 시작 시간 선택
     *
     * 출석 시간은 하루 종일 여부와 상관없이
     * 날짜 + 시간을 직접 선택합니다.
     */
    if (showCheckInStartPicker) {
        UDateTimePickerDialog(
            onConfirm = { utcDateTime ->
                viewModel.updateCheckInStartDateTime(
                    utcDateTime
                )

                showCheckInStartPicker = false
            },
            onDismiss = {
                showCheckInStartPicker = false
            }
        )
    }

    /**
     * 정상 출석 종료 시간 선택
     */
    if (showOnTimeEndPicker) {
        UDateTimePickerDialog(
            onConfirm = { utcDateTime ->
                viewModel.updateOnTimeEndDateTime(
                    utcDateTime
                )

                showOnTimeEndPicker = false
            },
            onDismiss = {
                showOnTimeEndPicker = false
            }
        )
    }

    /**
     * 지각 인정 종료 시간 선택
     */
    if (showLateEndPicker) {
        UDateTimePickerDialog(
            onConfirm = { utcDateTime ->
                viewModel.updateLateEndDateTime(
                    utcDateTime
                )

                showLateEndPicker = false
            },
            onDismiss = {
                showLateEndPicker = false
            }
        )
    }

    /**
     * 장소 선택 BottomSheet
     */
    if (showLocationBottomSheet) {
        GroupScheduleLocationBottomSheet(
            onDismissRequest = {
                showLocationBottomSheet = false
            },
            onLocationSelected = { location ->
                viewModel.onAction(
                    AdminStudyGroupScheduleAction.SelectPlace(
                        place = location.title,
                        latitude = location.latitude,
                        longitude = location.longitude,
                    )
                )

                showLocationBottomSheet = false
            }
        )
    }

    /**
     * 챌린저 선택 BottomSheet
     */
    if (showChallengerBottomSheet) {
        GroupScheduleChallengerBottomSheet(
            preSelected = state.selectedChallengers,
            onDismissRequest = {
                showChallengerBottomSheet = false
            },
            onConfirm = { challengers, summaryText ->
                viewModel.onAction(
                    AdminStudyGroupScheduleAction.SelectChallengers(
                        challengers = challengers,
                        summaryText = summaryText,
                    )
                )

                showChallengerBottomSheet = false
            }
        )
    }

    /**
     * 주차 선택 BottomSheet
     */
    if (showWeekBottomSheet) {
        AdminSubmitWeekBottomSheet(
            weeks = state.weeks,
            onSelect = { weekItem ->
                viewModel.onAction(
                    AdminStudyGroupScheduleAction.SelectWeek(
                        week = weekItem.week,
                        weeklyCurriculumId =
                            weekItem.weeklyCurriculumId,
                    )
                )

                showWeekBottomSheet = false
            },
            onDismiss = {
                showWeekBottomSheet = false
            },
        )
    }
}