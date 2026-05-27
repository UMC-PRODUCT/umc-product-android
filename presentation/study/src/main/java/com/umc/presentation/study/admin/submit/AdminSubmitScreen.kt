package com.umc.presentation.study.admin.submit

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.umc.component.component.DialogType
import com.umc.component.component.UBasicDialog
import com.umc.component.component.UText
import com.umc.component.theme.*
import com.umc.component.theme.UmcTypographyTokens.FootnoteBold
import com.umc.presentation.study.admin.submit.bottomsheet.AdminSubmitBottomSheet
import com.umc.presentation.study.admin.submit.bottomsheet.AdminSubmitGroupBottomSheet
import com.umc.presentation.study.admin.submit.bottomsheet.AdminSubmitWeekBottomSheet
import com.umc.presentation.study.admin.submit.component.AdminSubmitFilterBar
import com.umc.presentation.study.admin.submit.component.AdminSubmitItem
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AdminSubmitRoute(
    viewModel: AdminSubmitViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is AdminSubmitEvent.ShowToast ->
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                is AdminSubmitEvent.ShowApproveDialog -> {  }
                is AdminSubmitEvent.ShowRejectDialog -> {  }
            }
        }
    }

    AdminSubmitScreen(
        state = state,
        onAction = viewModel::onAction
    )
}

@Composable
fun AdminSubmitScreen(
    state: AdminSubmitState,
    onAction: (AdminSubmitAction) -> Unit = {},
) {
    // 승인 다이얼로그
    if (state.showApproveDialog) {
        UBasicDialog(
            title = "스터디 제출을 승인하시겠습니까?",
            type = DialogType.SUCCESS,
            positiveText = "승인하기",
            negativeText = "취소",
            onPositive = { onAction(AdminSubmitAction.ConfirmApprove) },
            onNegative = { onAction(AdminSubmitAction.DismissDialog) },
            onDismissRequest = { onAction(AdminSubmitAction.DismissDialog) }
        )
    }

    // 반려 다이얼로그
    if (state.showRejectDialog) {
        UBasicDialog(
            title = "스터디 제출을 반려하시겠습니까?",
            type = DialogType.CANCEL,
            positiveText = "반려하기",
            negativeText = "취소",
            onPositive = { onAction(AdminSubmitAction.ConfirmReject) },
            onNegative = { onAction(AdminSubmitAction.DismissDialog) },
            onDismissRequest = { onAction(AdminSubmitAction.DismissDialog) }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(neutral000())
    ) {
        AdminSubmitFilterBar(
            selectedWeek = state.selectedWeek,
            selectedGroupName = state.selectedGroupName,
            onWeekClick = { onAction(AdminSubmitAction.OpenWeekBottomSheet) },
            onGroupClick = { onAction(AdminSubmitAction.OpenGroupBottomSheet) },
        )

        if (state.items.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                UText(text = "제출 내역이 없어요", style = FootnoteBold, color = neutral500())
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(neutral100()),
                contentPadding = PaddingValues(
                    start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.items, key = { it.id }) { item ->
                    AdminSubmitItem(
                        item = item,
                        onClick = { onAction(AdminSubmitAction.OpenBottomSheet(item)) }
                    )
                }
            }
        }
    }

    // 바텀시트
    if (state.isBottomSheetOpen) {
        AdminSubmitBottomSheet(
            state = state,
            onAction = onAction
        )
    }

    // 주차 바텀시트
    if (state.showWeekBottomSheet) {
        AdminSubmitWeekBottomSheet(
            weeks = state.availableWeeks,
            onSelect = { onAction(AdminSubmitAction.SelectWeek(it)) },
            onDismiss = { onAction(AdminSubmitAction.CloseWeekBottomSheet) }
        )
    }

    // 그룹 바텀시트
    if (state.showGroupBottomSheet) {
        AdminSubmitGroupBottomSheet(
            groups = state.availableGroups,
            onSelect = { onAction(AdminSubmitAction.SelectGroup(it)) },
            onDismiss = { onAction(AdminSubmitAction.CloseGroupBottomSheet) }
        )
    }
}


@Preview(showBackground = true)
@Composable
private fun AdminSubmitScreenPreview() {
    AdminSubmitScreen(
        state = AdminSubmitState(
            items = listOf(
                AdminSubmitItemUiModel(
                    id = 1L,
                    name = "홍길동",
                    nickname = "닉네임",
                    partLabel = "iOS",
                    weekText = "1주차",
                    studyTitle = "SwiftUI 클론 코딩",
                    schoolName = "중앙대",
                    status = "BEST",
                )
            )
        )
    )
}