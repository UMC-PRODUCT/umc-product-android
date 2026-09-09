package com.umc.presentation.study.admin.group

import com.umc.domain.model.enums.UserPart
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
import com.umc.component.theme.UmcTypographyTokens.HeadlineBold
import com.umc.component.theme.grey100
import com.umc.component.theme.grey500
import com.umc.presentation.study.admin.group.bottomsheet.AdminStudyGroupMemberEditBottomSheet
import com.umc.presentation.study.admin.group.component.AdminStudyGroupCard
import com.umc.presentation.study.admin.group.component.AdminStudyGroupCreateCard
import com.umc.presentation.study.admin.group.dialog.AdminStudyGroupDeleteDialog
import com.umc.presentation.study.admin.group.dialog.AdminStudyGroupEditDialog
import kotlinx.collections.immutable.persistentListOf

/**
 * 관리자 스터디 그룹 화면
 *
 * 관리자가 담당하는 스터디 그룹 목록을 표시하고
 * 그룹 생성 / 수정 / 삭제 / 멤버 수정 / 일정 등록 기능을 제공합니다.
 *
 * 주요 UI
 * - 스터디 그룹 생성 카드
 * - 관리자 스터디 그룹 목록
 * - 그룹 설정 메뉴
 * - 그룹 정보 수정 Dialog
 * - 그룹 삭제 Dialog
 * - 스터디원 수정 BottomSheet
 */
@Composable
fun AdminStudyGroupScreen(
    state: AdminStudyGroupState,
    onAction: (AdminStudyGroupAction) -> Unit = {},
) {
    /**
     * 그룹 정보 수정 Dialog
     *
     * editTargetItem이 존재할 때 표시합니다.
     */
    if (state.isEditDialogOpen) {
        AdminStudyGroupEditDialog(
            groupName = state.editGroupName,
            selectedPart = state.editPartLabel,
            canConfirm = state.canConfirmEdit,

            // 그룹 이름 변경
            onGroupNameChanged = { name ->
                onAction(
                    AdminStudyGroupAction.OnEditGroupNameChanged(
                        name
                    )
                )
            },

            // 그룹 파트 변경
            onPartChanged = { part ->
                onAction(
                    AdminStudyGroupAction.OnEditPartChanged(
                        part
                    )
                )
            },

            // 수정 완료
            onConfirm = {
                onAction(
                    AdminStudyGroupAction.ConfirmEditGroup
                )
            },

            // 수정 Dialog 닫기
            onDismiss = {
                onAction(
                    AdminStudyGroupAction.CloseEditDialog
                )
            },
        )
    }

    /**
     * 그룹 삭제 확인 Dialog
     */
    if (state.isDeleteDialogOpen) {
        AdminStudyGroupDeleteDialog(
            onDelete = {
                onAction(
                    AdminStudyGroupAction.ConfirmDeleteGroup
                )
            },
            onDismiss = {
                onAction(
                    AdminStudyGroupAction.CloseDeleteDialog
                )
            },
        )
    }

    /**
     * 스터디원 수정 BottomSheet
     *
     * 현재 그룹의 기존 스터디원을 preSelected로 전달합니다.
     *
     * 수정 완료 후 최종 멤버 목록을
     * ConfirmMemberChanges 액션으로 ViewModel에 전달합니다.
     */
    if (state.isMemberBottomSheetOpen) {
        AdminStudyGroupMemberEditBottomSheet(
            preSelected = state.editingMembers,

            onDismissRequest = {
                onAction(
                    AdminStudyGroupAction.CloseMemberBottomSheet
                )
            },

            onConfirm = { members ->
                onAction(
                    AdminStudyGroupAction.ConfirmMemberChanges(
                        members = members
                    )
                )
            },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(grey100())
    ) {
        /**
         * 스터디 그룹 생성 카드
         *
         * 클릭 시 그룹 생성 화면으로 이동합니다.
         */
        AdminStudyGroupCreateCard(
            onClick = {
                onAction(
                    AdminStudyGroupAction.ClickCreateGroup
                )
            },
        )

        /**
         * 관리 중인 그룹이 없는 경우 빈 화면 표시
         */
        if (state.groups.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(grey100()),
                contentAlignment = Alignment.Center,
            ) {
                UText(
                    text = "생성된 스터디 그룹이 없어요",
                    style = HeadlineBold,
                    color = grey500(),
                )
            }
        } else {
            /**
             * 관리 중인 스터디 그룹 목록
             */
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(grey100()),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 16.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(
                    16.dp
                ),
            ) {
                items(
                    items = state.groups,
                    key = { item ->
                        item.groupId
                    },
                ) { item ->
                    AdminStudyGroupCard(
                        item = item,

                        /**
                         * 현재 그룹의 설정 메뉴가 열려 있는지 확인
                         */
                        isSettingOpen =
                            state.selectedSettingItem
                                ?.groupId == item.groupId,

                        // 설정 Popup 열기
                        onSettingClick = {
                            onAction(
                                AdminStudyGroupAction.ClickSetting(
                                    item
                                )
                            )
                        },

                        // 설정 Popup 닫기
                        onDismissSetting = {
                            onAction(
                                AdminStudyGroupAction
                                    .DismissSettingPopup
                            )
                        },

                        // 그룹 정보 수정 Dialog 열기
                        onEditClick = {
                            onAction(
                                AdminStudyGroupAction.OpenEditDialog(
                                    item
                                )
                            )
                        },

                        // 그룹 삭제 Dialog 열기
                        onDeleteClick = {
                            onAction(
                                AdminStudyGroupAction.OpenDeleteDialog(
                                    item
                                )
                            )
                        },

                        // 스터디 일정 등록 화면 이동
                        onAddScheduleClick = {
                            onAction(
                                AdminStudyGroupAction.ClickAddSchedule(
                                    item
                                )
                            )
                        },

                        // 멤버 수정 BottomSheet 열기
                        onAddMemberClick = {
                            onAction(
                                AdminStudyGroupAction.OpenMemberBottomSheet(
                                    item = item
                                )
                            )
                        },
                    )
                }
            }
        }
    }
}

/**
 * 관리자 스터디 그룹 화면 Preview
 */
@Preview(showBackground = true)
@Composable
private fun AdminStudyGroupScreenPreview() {
    AdminStudyGroupScreen(
        state = AdminStudyGroupState(
            groups = persistentListOf(
                AdminStudyGroupItemUiModel(
                    groupId = 1L,
                    title = "React A팀",
                    partLabel = UserPart.WEB.label,
                    leaderName = "홍길동",
                    leaderChallengerId = 1L,
                    leaderProfileImageUrl = null,
                    members = persistentListOf(
                        AdminStudyGroupMemberUiModel(
                            challengerId = 1L,
                            name = "홍길동",
                        ),
                        AdminStudyGroupMemberUiModel(
                            challengerId = 2L,
                            name = "홍길동",
                        ),
                        AdminStudyGroupMemberUiModel(
                            challengerId = 3L,
                            name = "홍길동",
                        ),
                    ),
                    memberChallengerIds =
                        persistentListOf(1L, 2L, 3L),
                    createdAtRaw =
                        "2024-03-01T00:00:00",
                    memberCount = 3,
                    leaderUniv = "중앙대",
                    studyPart = UserPart.WEB.serverValue,
                )
            )
        )
    )
}

/**
 * 여러 그룹이 존재하는 상태를 확인하기 위한 Preview용 데이터
 */
private fun previewState() =
    AdminStudyGroupState(
        groups = persistentListOf(
            AdminStudyGroupItemUiModel(
                groupId = 1L,
                title = "React A팀",
                partLabel = UserPart.WEB.label,
                leaderName = "홍길동",
                leaderChallengerId = 1L,
                leaderProfileImageUrl = null,
                members = persistentListOf(
                    AdminStudyGroupMemberUiModel(
                        1L,
                        "홍길동"
                    ),
                    AdminStudyGroupMemberUiModel(
                        2L,
                        "홍길동"
                    ),
                    AdminStudyGroupMemberUiModel(
                        3L,
                        "홍길동"
                    ),
                ),
                memberChallengerIds =
                    persistentListOf(1L, 2L, 3L),
                createdAtRaw =
                    "2024-03-01T00:00:00",
                memberCount = 3,
                leaderUniv = "중앙대",
                studyPart = UserPart.WEB.serverValue,
            ),

            AdminStudyGroupItemUiModel(
                groupId = 2L,
                title = "React B팀",
                partLabel = UserPart.ANDROID.label,
                leaderName = "김철수",
                leaderChallengerId = 2L,
                leaderProfileImageUrl = null,
                members = persistentListOf(
                    AdminStudyGroupMemberUiModel(
                        4L,
                        "김철수"
                    ),
                    AdminStudyGroupMemberUiModel(
                        5L,
                        "이영희"
                    ),
                ),
                memberChallengerIds =
                    persistentListOf(4L, 5L),
                createdAtRaw =
                    "2024-03-01T00:00:00",
                memberCount = 2,
                leaderUniv = "서울여대",
                studyPart = UserPart.ANDROID.serverValue,
            )
        )
    )