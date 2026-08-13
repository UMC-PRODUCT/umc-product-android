package com.umc.presentation.study.admin.group.schedule

import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.umc.component.component.UDateTimePickerDialog
import com.umc.presentation.study.admin.group.schedule.bottomsheet.GroupScheduleChallengerBottomSheet
import com.umc.presentation.study.admin.group.schedule.bottomsheet.GroupScheduleLocationBottomSheet
import kotlinx.coroutines.flow.collectLatest
import com.umc.presentation.study.admin.submit.bottomsheet.AdminSubmitWeekBottomSheet
import com.umc.component.component.UDatePickerDialog
import com.umc.presentation.study.admin.submit.bottomsheet.AdminSubmitWeekUiModel

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

    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }
    var showCheckInStartPicker by remember { mutableStateOf(false) }
    var showOnTimeEndPicker by remember { mutableStateOf(false) }
    var showLateEndPicker by remember { mutableStateOf(false) }

    var showLocationBottomSheet by remember { mutableStateOf(false) }
    var showChallengerBottomSheet by remember { mutableStateOf(false) }

    var showWeekBottomSheet by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                AdminStudyGroupScheduleEvent.NavigateBack -> onNavigateBack()

                is AdminStudyGroupScheduleEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    AdminStudyGroupScheduleScreen(
        state = state,
        onAction = { action ->
            when (action) {
                AdminStudyGroupScheduleAction.ClickStartDateTime -> {
                    showStartPicker = true
                }

                AdminStudyGroupScheduleAction.ClickEndDateTime -> {
                    showEndPicker = true
                }

                AdminStudyGroupScheduleAction.ClickCheckInStart -> {
                    showCheckInStartPicker = true
                }

                AdminStudyGroupScheduleAction.ClickOnTimeEnd -> {
                    showOnTimeEndPicker = true
                }

                AdminStudyGroupScheduleAction.ClickLateEnd -> {
                    showLateEndPicker = true
                }

                AdminStudyGroupScheduleAction.ClickPlace -> {
                    showLocationBottomSheet = true
                }

                AdminStudyGroupScheduleAction.ClickChallenger -> {
                    showChallengerBottomSheet = true
                }

                AdminStudyGroupScheduleAction.ClickWeek -> {
                    showWeekBottomSheet = true
                }

                else -> {
                    viewModel.onAction(action)
                }
            }
        }
    )

    if (showStartPicker) {
        if (state.isAllDay) {
            UDatePickerDialog(
                initialDate = state.startDateTime,
                onConfirm = {
                    viewModel.updateStartDateTime(it)
                    showStartPicker = false
                },
                onDismiss = {
                    showStartPicker = false
                }
            )
        } else {
            UDateTimePickerDialog(
                onConfirm = {
                    viewModel.updateStartDateTime(it)
                    showStartPicker = false
                },
                onDismiss = {
                    showStartPicker = false
                }
            )
        }
    }

    if (showEndPicker) {
        if (state.isAllDay) {
            UDatePickerDialog(
                initialDate = state.endDateTime ?: state.startDateTime,
                onConfirm = {
                    viewModel.updateEndDateTime(it)
                    showEndPicker = false
                },
                onDismiss = {
                    showEndPicker = false
                }
            )
        } else {
            UDateTimePickerDialog(
                onConfirm = {
                    viewModel.updateEndDateTime(it)
                    showEndPicker = false
                },
                onDismiss = {
                    showEndPicker = false
                }
            )
        }
    }




    if (showCheckInStartPicker) {
        UDateTimePickerDialog(
            onConfirm = {
                viewModel.updateCheckInStartDateTime(it)
                showCheckInStartPicker = false
            },
            onDismiss = { showCheckInStartPicker = false }
        )
    }

    if (showOnTimeEndPicker) {
        UDateTimePickerDialog(
            onConfirm = {
                viewModel.updateOnTimeEndDateTime(it)
                showOnTimeEndPicker = false
            },
            onDismiss = { showOnTimeEndPicker = false }
        )
    }

    if (showLateEndPicker) {
        UDateTimePickerDialog(
            onConfirm = {
                viewModel.updateLateEndDateTime(it)
                showLateEndPicker = false
            },
            onDismiss = { showLateEndPicker = false }
        )
    }

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
                        summaryText = summaryText
                    )
                )
                showChallengerBottomSheet = false
            }
        )
    }

    if (showWeekBottomSheet) {
        AdminSubmitWeekBottomSheet(
            weeks = state.weeks,
            onSelect = { weekItem ->
                viewModel.onAction(
                    AdminStudyGroupScheduleAction.SelectWeek(
                        week = weekItem.week,
                        weeklyCurriculumId = weekItem.weeklyCurriculumId,
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