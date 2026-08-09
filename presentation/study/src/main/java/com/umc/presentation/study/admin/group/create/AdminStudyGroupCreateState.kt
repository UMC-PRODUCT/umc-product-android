package com.umc.presentation.study.admin.group.create

import com.umc.component.base.UiState

data class AdminStudyGroupCreateState(
    val gisuId: Long? = null,

    val groupName: String = "",
    val selectedPart: AdminStudyGroupCreatePartUiModel? = null,

    val selectedPartLeaders: List<AdminStudyGroupCreateMemberUiModel> = emptyList(),
    val selectedMembers: List<AdminStudyGroupCreateMemberUiModel> = emptyList(),

    val showPartBottomSheet: Boolean = false,
    val showPartLeaderBottomSheet: Boolean = false,
    val showMemberBottomSheet: Boolean = false,

    val isLoading: Boolean = false,
) : UiState {

    val memberSummary: String
        get() = when {
            selectedMembers.isEmpty() -> ""
            selectedMembers.size == 1 -> selectedMembers.first().name
            else -> "${selectedMembers.first().name} 외 ${selectedMembers.size - 1}명"
        }

    val partLeaderSummary: String
        get() = selectedPartLeaders.joinToString(", ") { it.name }

    val isRegisterEnabled: Boolean
        get() =
                groupName.isNotBlank() &&
                selectedPart != null &&
                selectedPartLeaders.isNotEmpty() &&
                selectedMembers.isNotEmpty() &&
                !isLoading
}