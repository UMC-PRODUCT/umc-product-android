package com.umc.presentation.study.admin.group

import com.umc.component.base.UiState

data class AdminStudyGroupState(
    val groups: List<AdminStudyGroupItemUiModel> = emptyList(),

    val selectedSettingItem: AdminStudyGroupItemUiModel? = null,

    val editTargetItem: AdminStudyGroupItemUiModel? = null,
    val editGroupName: String = "",
    val editPartLabel: String = "Web",

    val deleteTargetItem: AdminStudyGroupItemUiModel? = null,
) : UiState {
    val isSettingPopupOpen: Boolean get() = selectedSettingItem != null
    val isEditDialogOpen: Boolean get() = editTargetItem != null
    val isDeleteDialogOpen: Boolean get() = deleteTargetItem != null

    val canConfirmEdit: Boolean
        get() = editGroupName.isNotBlank()
}