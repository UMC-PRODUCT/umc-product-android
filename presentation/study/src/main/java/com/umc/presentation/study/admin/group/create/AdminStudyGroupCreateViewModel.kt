package com.umc.presentation.study.admin.group.create

import com.umc.component.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AdminStudyGroupCreateViewModel @Inject constructor() :
    BaseViewModel<AdminStudyGroupCreateState, AdminStudyGroupCreateEvent>(
        AdminStudyGroupCreateState()
    ) {

    fun onAction(action: AdminStudyGroupCreateAction) {
        when (action) {
            is AdminStudyGroupCreateAction.OnGroupNameChanged -> {
                updateState { copy(groupName = action.value) }
            }

            AdminStudyGroupCreateAction.OnPartClick -> {
                updateState { copy(showPartBottomSheet = true) }
            }

            AdminStudyGroupCreateAction.OnPartLeaderClick -> {
                updateState { copy(showPartLeaderBottomSheet = true) }
            }

            AdminStudyGroupCreateAction.OnMemberClick -> {
                updateState { copy(showMemberBottomSheet = true) }
            }

            AdminStudyGroupCreateAction.OnRegisterClick -> {
                if (uiState.value.isRegisterEnabled) {
                    emitEvent(AdminStudyGroupCreateEvent.RegisterSuccess)
                }
            }

            AdminStudyGroupCreateAction.OnBackClick -> {
                emitEvent(AdminStudyGroupCreateEvent.NavigateBack)
            }
        }
    }

    fun dismissBottomSheet() {
        updateState {
            copy(
                showPartBottomSheet = false,
                showPartLeaderBottomSheet = false,
                showMemberBottomSheet = false
            )
        }
    }

    fun selectPart(part: AdminStudyGroupCreatePartUiModel) {
        updateState {
            copy(
                selectedPart = part,
                selectedPartLeaders = emptyList(),
                selectedMembers = emptyList(),
                showPartBottomSheet = false
            )
        }
    }

    fun selectPartLeaders(leaders: List<AdminStudyGroupCreateMemberUiModel>) {
        updateState {
            copy(
                selectedPartLeaders = leaders,
                showPartLeaderBottomSheet = false
            )
        }
    }

    fun selectMembers(members: List<AdminStudyGroupCreateMemberUiModel>) {
        updateState {
            copy(
                selectedMembers = members,
                showMemberBottomSheet = false
            )
        }
    }
}