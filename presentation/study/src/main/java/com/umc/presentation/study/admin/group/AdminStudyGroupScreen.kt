package com.umc.presentation.study.admin.group

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.umc.component.component.UText
import com.umc.component.theme.UmcTypographyTokens.FootnoteBold
import com.umc.component.theme.neutral000
import com.umc.component.theme.neutral100
import com.umc.component.theme.neutral500
import com.umc.presentation.study.admin.group.component.AdminStudyGroupCard
import com.umc.presentation.study.admin.group.component.AdminStudyGroupCreateCard
import com.umc.presentation.study.admin.group.component.AdminStudyGroupHeader
import com.umc.presentation.study.admin.group.dialog.AdminStudyGroupDeleteDialog
import com.umc.presentation.study.admin.group.dialog.AdminStudyGroupEditDialog

@Composable
fun AdminStudyGroupScreen(
    state: AdminStudyGroupState,
    onAction: (AdminStudyGroupAction) -> Unit = {},
) {
    if (state.isEditDialogOpen) {
        AdminStudyGroupEditDialog(
            groupName = state.editGroupName,
            selectedPart = state.editPartLabel,
            canConfirm = state.canConfirmEdit,
            onGroupNameChanged = {
                onAction(AdminStudyGroupAction.OnEditGroupNameChanged(it))
            },
            onPartChanged = {
                onAction(AdminStudyGroupAction.OnEditPartChanged(it))
            },
            onConfirm = {
                onAction(AdminStudyGroupAction.ConfirmEditGroup)
            },
            onDismiss = {
                onAction(AdminStudyGroupAction.CloseEditDialog)
            },
        )
    }

    if (state.isDeleteDialogOpen) {
        AdminStudyGroupDeleteDialog(
            onDelete = {
                onAction(AdminStudyGroupAction.ConfirmDeleteGroup)
            },
            onDismiss = {
                onAction(AdminStudyGroupAction.CloseDeleteDialog)
            },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(neutral100())
    ) {

        AdminStudyGroupCreateCard(
            onClick = {
                onAction(AdminStudyGroupAction.ClickCreateGroup)
            },
        )

        if (state.groups.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(neutral100()),
                contentAlignment = Alignment.Center,
            ) {
                UText(
                    text = "생성된 스터디 그룹이 없어요",
                    style = FootnoteBold,
                    color = neutral500(),
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(neutral100()),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 12.dp,
                    bottom = 16.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(
                    items = state.groups,
                    key = { it.groupId },
                ) { item ->
                    AdminStudyGroupCard(
                        item = item,
                        isSettingOpen = state.selectedSettingItem?.groupId == item.groupId,
                        onSettingClick = {
                            onAction(AdminStudyGroupAction.ClickSetting(item))
                        },
                        onDismissSetting = {
                            onAction(AdminStudyGroupAction.DismissSettingPopup)
                        },
                        onEditClick = {
                            onAction(AdminStudyGroupAction.OpenEditDialog(item))
                        },
                        onDeleteClick = {
                            onAction(AdminStudyGroupAction.OpenDeleteDialog(item))
                        },
                        onAddScheduleClick = {
                            onAction(AdminStudyGroupAction.ClickAddSchedule(item))
                        },
                        onAddMemberClick = {
                            onAction(AdminStudyGroupAction.ClickEditMembers(item))
                        },
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AdminStudyGroupScreenPreview() {
    AdminStudyGroupScreen(
        state = AdminStudyGroupState(
            groups = listOf(
                AdminStudyGroupItemUiModel(
                    groupId = 1L,
                    title = "React A팀",
                    partLabel = "Web",
                    leaderName = "홍길동",
                    leaderChallengerId = 1L,
                    members = listOf(
                        AdminStudyGroupMemberUiModel(1L, "홍길동"),
                        AdminStudyGroupMemberUiModel(2L, "홍길동"),
                        AdminStudyGroupMemberUiModel(3L, "홍길동"),
                    ),
                    memberChallengerIds = listOf(1L, 2L, 3L),
                    createdAtRaw = "2024-03-01T00:00:00",
                    memberCount = 3,
                    leaderUniv = "중앙대",
                )
            )
        )
    )
}

private fun previewState() = AdminStudyGroupState(
    groups = listOf(
        AdminStudyGroupItemUiModel(
            groupId = 1L,
            title = "React A팀",
            partLabel = "Web",
            leaderName = "홍길동",
            leaderChallengerId = 1L,
            leaderProfileImageUrl = null,
            members = listOf(
                AdminStudyGroupMemberUiModel(1, "홍길동"),
                AdminStudyGroupMemberUiModel(2, "홍길동"),
                AdminStudyGroupMemberUiModel(3, "홍길동"),
            ),
            memberChallengerIds = listOf(1, 2, 3),
            createdAtRaw = "2024-03-01T00:00:00",
            memberCount = 3,
            leaderUniv = "중앙대",
        ),
        AdminStudyGroupItemUiModel(
            groupId = 2L,
            title = "React B팀",
            partLabel = "Android",
            leaderName = "김철수",
            leaderChallengerId = 2L,
            leaderProfileImageUrl = null,
            members = listOf(
                AdminStudyGroupMemberUiModel(4, "김철수"),
                AdminStudyGroupMemberUiModel(5, "이영희"),
            ),
            memberChallengerIds = listOf(4, 5),
            createdAtRaw = "2024-03-01T00:00:00",
            memberCount = 2,
            leaderUniv = "서울여대",
        )
    )
)