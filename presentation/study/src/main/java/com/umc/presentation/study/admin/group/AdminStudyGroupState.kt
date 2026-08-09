package com.umc.presentation.study.admin.group

import com.umc.component.base.UiState
import com.umc.presentation.study.admin.group.create.AdminStudyGroupCreateMemberUiModel

data class AdminStudyGroupState(
    val groups: List<AdminStudyGroupItemUiModel> = emptyList(),

    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val nextCursor: Long? = null,
    val hasNext: Boolean = false,

    val selectedSettingItem: AdminStudyGroupItemUiModel? = null,

    val editTargetItem: AdminStudyGroupItemUiModel? = null,
    val editGroupName: String = "",
    val editPartLabel: String = "",

    val deleteTargetItem: AdminStudyGroupItemUiModel? = null,

    // 현재 멤버를 수정 중인 그룹
    val memberEditTargetItem: AdminStudyGroupItemUiModel? = null,

    // 바텀시트에 표시할 현재 스터디원 목록
    val editingMembers: List<AdminStudyGroupCreateMemberUiModel> = emptyList(),
) : UiState {


    val isEditDialogOpen: Boolean
        get() = editTargetItem != null

    val isDeleteDialogOpen: Boolean
        get() = deleteTargetItem != null

    val isMemberBottomSheetOpen: Boolean
        get() = memberEditTargetItem != null

    val canConfirmEdit: Boolean
        get() = editGroupName.isNotBlank()
}