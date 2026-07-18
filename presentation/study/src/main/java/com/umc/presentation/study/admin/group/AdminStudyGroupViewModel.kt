package com.umc.presentation.study.admin.group

import com.umc.component.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AdminStudyGroupViewModel @Inject constructor() :
    BaseViewModel<AdminStudyGroupState, AdminStudyGroupEvent>(AdminStudyGroupState()) {

    init {
        loadDummy()
    }

    fun onAction(action: AdminStudyGroupAction) {
        when (action) {
            is AdminStudyGroupAction.LoadGroups -> {
                loadDummy()
            }

            is AdminStudyGroupAction.ClickCreateGroup -> {
                emitEvent(AdminStudyGroupEvent.NavigateCreateGroup)
            }

            is AdminStudyGroupAction.ClickSetting -> {
                updateState {
                    copy(selectedSettingItem = action.item)
                }
            }

            is AdminStudyGroupAction.DismissSettingPopup -> {
                updateState {
                    copy(selectedSettingItem = null)
                }
            }

            is AdminStudyGroupAction.ClickAddSchedule -> {
                emitEvent(
                    AdminStudyGroupEvent.NavigateAddSchedule(
                        groupId = action.item.groupId,
                        groupTitle = action.item.title,
                        groupPart = action.item.partLabel,
                    )
                )
            }

            is AdminStudyGroupAction.ClickEditMembers -> {
                emitEvent(AdminStudyGroupEvent.OpenEditMembers(action.item))
            }

            is AdminStudyGroupAction.OpenEditDialog -> {
                updateState {
                    copy(
                        selectedSettingItem = null,
                        editTargetItem = action.item,
                        editGroupName = action.item.title,
                        editPartLabel = action.item.partLabel.ifBlank { "Web" },
                    )
                }
            }

            is AdminStudyGroupAction.CloseEditDialog -> {
                updateState {
                    copy(
                        editTargetItem = null,
                        editGroupName = "",
                        editPartLabel = "Web",
                    )
                }
            }

            is AdminStudyGroupAction.OnEditGroupNameChanged -> {
                updateState {
                    copy(editGroupName = action.name)
                }
            }

            is AdminStudyGroupAction.OnEditPartChanged -> {
                updateState {
                    copy(editPartLabel = action.partLabel)
                }
            }

            is AdminStudyGroupAction.ConfirmEditGroup -> {
                val target = uiState.value.editTargetItem ?: return
                val newName = uiState.value.editGroupName.trim()
                val newPart = uiState.value.editPartLabel

                if (newName.isBlank()) return

                updateState {
                    copy(
                        groups = groups.map {
                            if (it.groupId == target.groupId) {
                                it.copy(
                                    title = newName,
                                    partLabel = newPart,
                                )
                            } else {
                                it
                            }
                        },
                        editTargetItem = null,
                        editGroupName = "",
                        editPartLabel = "Web",
                    )
                }

                emitEvent(AdminStudyGroupEvent.ShowToast("그룹 정보가 수정됐어요."))
            }

            is AdminStudyGroupAction.OpenDeleteDialog -> {
                updateState {
                    copy(
                        selectedSettingItem = null,
                        deleteTargetItem = action.item,
                    )
                }
            }

            is AdminStudyGroupAction.CloseDeleteDialog -> {
                updateState {
                    copy(deleteTargetItem = null)
                }
            }

            is AdminStudyGroupAction.ConfirmDeleteGroup -> {
                val target = uiState.value.deleteTargetItem ?: return

                updateState {
                    copy(
                        groups = groups.filterNot { it.groupId == target.groupId },
                        deleteTargetItem = null,
                    )
                }

                emitEvent(AdminStudyGroupEvent.ShowToast("그룹이 삭제됐어요."))
            }
        }
    }

    private fun loadDummy() {
        updateState {
            copy(
                groups = listOf(
                    AdminStudyGroupItemUiModel(
                        groupId = 1L,
                        title = "React A팀",
                        partLabel = "Web",
                        leaderName = "홍길동",
                        leaderChallengerId = 1001L,
                        leaderProfileImageUrl = null,
                        members = listOf(
                            AdminStudyGroupMemberUiModel(
                                challengerId = 2001L,
                                name = "홍길동",
                            ),
                            AdminStudyGroupMemberUiModel(
                                challengerId = 2002L,
                                name = "홍길동",
                            ),
                            AdminStudyGroupMemberUiModel(
                                challengerId = 2003L,
                                name = "홍길동",
                            ),
                        ),
                        memberChallengerIds = listOf(2001L, 2002L, 2003L),
                        createdAtRaw = "2024-03-01T00:00:00",
                        memberCount = 3,
                        leaderUniv = "중앙대",
                    ),
                    AdminStudyGroupItemUiModel(
                        groupId = 2L,
                        title = "React A팀",
                        partLabel = "Web",
                        leaderName = "홍길동",
                        leaderChallengerId = 1002L,
                        leaderProfileImageUrl = null,
                        members = listOf(
                            AdminStudyGroupMemberUiModel(
                                challengerId = 3001L,
                                name = "홍길동",
                            ),
                            AdminStudyGroupMemberUiModel(
                                challengerId = 3002L,
                                name = "홍길동",
                            ),
                            AdminStudyGroupMemberUiModel(
                                challengerId = 3003L,
                                name = "홍길동",
                            ),
                        ),
                        memberChallengerIds = listOf(3001L, 3002L, 3003L),
                        createdAtRaw = "2024-03-01T00:00:00",
                        memberCount = 3,
                        leaderUniv = "중앙대",
                    ),
                )
            )
        }
    }
}