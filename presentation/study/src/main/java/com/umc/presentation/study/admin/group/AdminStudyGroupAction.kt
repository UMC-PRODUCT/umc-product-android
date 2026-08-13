package com.umc.presentation.study.admin.group

import com.umc.presentation.study.admin.group.create.AdminStudyGroupCreateMemberUiModel

sealed interface AdminStudyGroupAction {
    data object LoadGroups : AdminStudyGroupAction

    data object ClickCreateGroup : AdminStudyGroupAction
    data class ClickSetting(
        val item: AdminStudyGroupItemUiModel,
    ) : AdminStudyGroupAction

    data object DismissSettingPopup : AdminStudyGroupAction

    data class ClickAddSchedule(
        val item: AdminStudyGroupItemUiModel,
    ) : AdminStudyGroupAction

    data class ClickEditMembers(
        val item: AdminStudyGroupItemUiModel,
    ) : AdminStudyGroupAction

    data class OpenEditDialog(
        val item: AdminStudyGroupItemUiModel,
    ) : AdminStudyGroupAction

    data object CloseEditDialog : AdminStudyGroupAction

    data class OnEditGroupNameChanged(
        val name: String,
    ) : AdminStudyGroupAction

    data class OnEditPartChanged(
        val partLabel: String,
    ) : AdminStudyGroupAction

    data object ConfirmEditGroup : AdminStudyGroupAction

    data class OpenDeleteDialog(
        val item: AdminStudyGroupItemUiModel,
    ) : AdminStudyGroupAction

    data object CloseDeleteDialog : AdminStudyGroupAction
    data object ConfirmDeleteGroup : AdminStudyGroupAction

    // 파란색 + 버튼 클릭
    data class OpenMemberBottomSheet(
        val item: AdminStudyGroupItemUiModel,
    ) : AdminStudyGroupAction

    // 멤버 바텀시트 닫기
    data object CloseMemberBottomSheet : AdminStudyGroupAction

    // 바텀시트에서 수정된 최종 멤버 목록 반영
    data class ConfirmMemberChanges(
        val members: List<AdminStudyGroupCreateMemberUiModel>,
    ) : AdminStudyGroupAction
}