package com.umc.presentation.study.admin.group.create

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.umc.presentation.study.admin.group.create.bottomsheet.GroupCreateMemberBottomSheet
import com.umc.presentation.study.admin.group.create.bottomsheet.GroupCreatePartBottomSheet
import com.umc.presentation.study.admin.group.create.bottomsheet.GroupCreatePartLeaderBottomSheet
import com.umc.presentation.study.admin.group.create.component.GroupCreateSelectRow
import com.umc.presentation.study.admin.group.create.component.GroupCreateTextField
import com.umc.presentation.study.admin.group.create.component.GroupCreateTopBar

@Composable
fun AdminStudyGroupCreateScreen(
    state: AdminStudyGroupCreateState,
    onAction: (AdminStudyGroupCreateAction) -> Unit,
    onDismissBottomSheet: () -> Unit,
    onPartSelected: (AdminStudyGroupCreatePartUiModel) -> Unit,
    onPartLeaderSelected: (List<AdminStudyGroupCreateMemberUiModel>) -> Unit,
    onMembersSelected: (List<AdminStudyGroupCreateMemberUiModel>) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp)
            .padding(top = 20.dp)
    ) {

        GroupCreateTopBar(
            isRegisterEnabled = state.isRegisterEnabled,
            onBackClick = {
                onAction(AdminStudyGroupCreateAction.OnBackClick)
            },
            onRegisterClick = {
                onAction(AdminStudyGroupCreateAction.OnRegisterClick)
            },

            onNotificationClick = {
                // 알림 버튼 클릭 시 동작
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        GroupCreateTextField(
            title = "그룹 이름",
            value = state.groupName,
            placeholder = "예: React 실습 A팀",
            onValueChange = {
                onAction(AdminStudyGroupCreateAction.OnGroupNameChanged(it))
            }
        )

        Spacer(modifier = Modifier.height(22.dp))

        GroupCreateSelectRow(
            title = "해당 파트",
            value = state.selectedPart?.label.orEmpty(),
            placeholder = "파트를 선택하세요",
            onClick = {
                onAction(AdminStudyGroupCreateAction.OnPartClick)
            }
        )

        Spacer(modifier = Modifier.height(22.dp))

        GroupCreateSelectRow(
            title = "담당 파트장",
            value = state.partLeaderSummary,
            placeholder = "담당 파트장을 선택하세요",
            onClick = {
                onAction(AdminStudyGroupCreateAction.OnPartLeaderClick)
            }
        )

        Spacer(modifier = Modifier.height(22.dp))

        GroupCreateSelectRow(
            title = "스터디원 추가",
            value = state.memberSummary,
            placeholder = "스터디원을 선택하세요",
            onClick = {
                onAction(AdminStudyGroupCreateAction.OnMemberClick)
            }
        )
    }

    if (state.showPartBottomSheet) {
        GroupCreatePartBottomSheet(
            selectedPart = state.selectedPart,
            onDismissRequest = onDismissBottomSheet,
            onPartSelected = onPartSelected
        )
    }

    if (state.showPartLeaderBottomSheet) {
        GroupCreatePartLeaderBottomSheet(
            preSelected = state.selectedPartLeaders,
            onDismissRequest = onDismissBottomSheet,
            onConfirm = onPartLeaderSelected
        )
    }

    if (state.showMemberBottomSheet) {
        GroupCreateMemberBottomSheet(
            preSelected = state.selectedMembers,
            onDismissRequest = onDismissBottomSheet,
            onConfirm = onMembersSelected
        )
    }
}