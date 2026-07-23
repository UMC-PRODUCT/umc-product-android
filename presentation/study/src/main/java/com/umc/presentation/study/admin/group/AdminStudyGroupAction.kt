package com.umc.presentation.study.admin.group

sealed interface AdminStudyGroupAction {
    data object LoadGroups : AdminStudyGroupAction

    data object ClickCreateGroup : AdminStudyGroupAction
    data class ClickSetting(val item: AdminStudyGroupItemUiModel) : AdminStudyGroupAction
    data object DismissSettingPopup : AdminStudyGroupAction

    data class ClickAddSchedule(val item: AdminStudyGroupItemUiModel) : AdminStudyGroupAction
    data class ClickEditMembers(val item: AdminStudyGroupItemUiModel) : AdminStudyGroupAction

    data class OpenEditDialog(val item: AdminStudyGroupItemUiModel) : AdminStudyGroupAction
    data object CloseEditDialog : AdminStudyGroupAction
    data class OnEditGroupNameChanged(val name: String) : AdminStudyGroupAction
    data class OnEditPartChanged(val partLabel: String) : AdminStudyGroupAction
    data object ConfirmEditGroup : AdminStudyGroupAction

    data class OpenDeleteDialog(val item: AdminStudyGroupItemUiModel) : AdminStudyGroupAction
    data object CloseDeleteDialog : AdminStudyGroupAction
    data object ConfirmDeleteGroup : AdminStudyGroupAction
}