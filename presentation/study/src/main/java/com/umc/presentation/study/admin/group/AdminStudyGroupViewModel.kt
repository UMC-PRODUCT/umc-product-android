package com.umc.presentation.study.admin.group

import androidx.lifecycle.viewModelScope
import com.umc.component.base.BaseViewModel
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.request.organization.UpdateStudyGroupRequest
import com.umc.domain.usecase.organization.GetManagedStudyGroupsUseCase
import com.umc.presentation.study.admin.group.create.AdminStudyGroupCreateMemberUiModel
import com.umc.domain.usecase.organization.UpdateStudyGroupUseCase
import com.umc.domain.usecase.organization.DeleteStudyGroupUseCase
import com.umc.domain.usecase.organization.AddStudyGroupMemberUseCase
import com.umc.domain.usecase.organization.DeleteStudyGroupMemberUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminStudyGroupViewModel @Inject constructor(
    private val getManagedStudyGroupsUseCase: GetManagedStudyGroupsUseCase,
    private val updateStudyGroupUseCase: UpdateStudyGroupUseCase,
    private val deleteStudyGroupUseCase: DeleteStudyGroupUseCase,
    private val addStudyGroupMemberUseCase: AddStudyGroupMemberUseCase,
    private val deleteStudyGroupMemberUseCase: DeleteStudyGroupMemberUseCase,
) : BaseViewModel<AdminStudyGroupState, AdminStudyGroupEvent>(
    AdminStudyGroupState()
) {


    init {
        loadManagedStudyGroups()
    }



    fun onAction(action: AdminStudyGroupAction) {
        when (action) {
            is AdminStudyGroupAction.LoadGroups -> {
                loadManagedStudyGroups()
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
                emitEvent(
                    AdminStudyGroupEvent.OpenEditMembers(action.item)
                )
            }

            is AdminStudyGroupAction.OpenEditDialog -> {
                updateState {
                    copy(
                        selectedSettingItem = null,
                        editTargetItem = action.item,
                        editGroupName = action.item.title,
                        editPartLabel = action.item.partLabel
                            .ifBlank { "Web" },
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

                if (newName.isBlank()) return

                viewModelScope.launch {
                    when (
                        val result = updateStudyGroupUseCase(
                            studyGroupId = target.groupId,
                            request = UpdateStudyGroupRequest(
                                name = newName,
                            ),
                        )
                    ) {
                        is ApiState.Success -> {
                            loadManagedStudyGroups()

                            updateState {
                                copy(
                                    editTargetItem = null,
                                    editGroupName = "",
                                    editPartLabel = "Web",
                                )
                            }

                            emitEvent(
                                AdminStudyGroupEvent.ShowToast(
                                    "그룹 정보가 수정됐어요."
                                )
                            )
                        }

                        is ApiState.Fail -> {
                            emitEvent(
                                AdminStudyGroupEvent.ShowToast(
                                    "수정에 실패했어요."
                                )
                            )
                        }
                    }
                }
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
                val target =
                    uiState.value.deleteTargetItem ?: return

                viewModelScope.launch {
                    when (
                        val result = deleteStudyGroupUseCase(
                            target.groupId
                        )
                    ) {
                        is ApiState.Success -> {
                            updateState {
                                copy(deleteTargetItem = null)
                            }

                            loadManagedStudyGroups()

                            emitEvent(
                                AdminStudyGroupEvent.ShowToast(
                                    "그룹이 삭제됐어요."
                                )
                            )
                        }

                        is ApiState.Fail -> {
                            emitEvent(
                                AdminStudyGroupEvent.ShowToast(
                                    "삭제에 실패했어요."
                                )
                            )
                        }
                    }
                }
            }

            is AdminStudyGroupAction.OpenMemberBottomSheet -> {
                val targetGroup = action.item

                val currentMembers = targetGroup.members.map { member ->
                    AdminStudyGroupCreateMemberUiModel(
                        id = member.challengerId,
                        name = member.name,
                        displayName = member.name,
                        partLabel = targetGroup.partLabel,
                        school = "",
                    )
                }

                updateState {
                    copy(
                        memberEditTargetItem = targetGroup,
                        editingMembers = currentMembers,
                    )
                }
            }

            AdminStudyGroupAction.CloseMemberBottomSheet -> {
                updateState {
                    copy(
                        memberEditTargetItem = null,
                        editingMembers = emptyList(),
                    )
                }
            }

            is AdminStudyGroupAction.ConfirmMemberChanges -> {
                val targetGroup =
                    uiState.value.memberEditTargetItem ?: return

                val updatedMembers = action.members

                val oldMemberIds = targetGroup.members
                    .map { member -> member.challengerId }
                    .toSet()

                val newMemberIds = updatedMembers
                    .map { member -> member.id }
                    .toSet()

                val memberIdsToAdd = newMemberIds - oldMemberIds
                val memberIdsToDelete = oldMemberIds - newMemberIds

                viewModelScope.launch {
                    var hasFailed = false

                    memberIdsToDelete.forEach { memberId ->
                        when (
                            deleteStudyGroupMemberUseCase(
                                studyGroupId = targetGroup.groupId,
                                memberId = memberId,
                            )
                        ) {
                            is ApiState.Success -> Unit
                            is ApiState.Fail -> hasFailed = true
                        }
                    }

                    memberIdsToAdd.forEach { memberId ->
                        when (
                            addStudyGroupMemberUseCase(
                                studyGroupId = targetGroup.groupId,
                                memberId = memberId,
                            )
                        ) {
                            is ApiState.Success -> Unit
                            is ApiState.Fail -> hasFailed = true
                        }
                    }

                    if (hasFailed) {
                        emitEvent(
                            AdminStudyGroupEvent.ShowToast(
                                "스터디원 수정에 실패했어요."
                            )
                        )
                        return@launch
                    }

                    updateState {
                        copy(
                            memberEditTargetItem = null,
                            editingMembers = emptyList(),
                        )
                    }

                    loadManagedStudyGroups()

                    emitEvent(
                        AdminStudyGroupEvent.ShowToast(
                            "스터디원이 수정됐어요."
                        )
                    )
                }
            }
        }
    }

    private fun loadManagedStudyGroups() {
        if (uiState.value.isLoading) return

        viewModelScope.launch {
            updateState {
                copy(isLoading = true)
            }

            when (
                val result = getManagedStudyGroupsUseCase(
                    cursor = null,
                    size = PAGE_SIZE,
                )
            ) {
                is ApiState.Success -> {
                    val page = result.data

                    updateState {
                        copy(
                            groups = page.content.map { group ->
                                group.toUiModel()
                            },
                            nextCursor = page.nextCursor,
                            hasNext = page.hasNext,
                            isLoading = false,
                        )
                    }
                }

                is ApiState.Fail -> {
                    updateState {
                        copy(isLoading = false)
                    }

                    emitEvent(
                        AdminStudyGroupEvent.ShowToast(
                            "스터디 그룹 목록을 불러오지 못했어요."
                        )
                    )
                }
            }
        }
    }



    companion object {
        private const val PAGE_SIZE = 20
    }
}