package com.umc.presentation.study.admin.group.create

import com.umc.component.base.UiState

data class AdminStudyGroupCreateState(
    val groupName: String = "",
    val selectedPart: AdminStudyGroupCreatePartUiModel? = null,

    val selectedPartLeaders: List<AdminStudyGroupCreateMemberUiModel> = emptyList(),

    val selectedMembers: List<AdminStudyGroupCreateMemberUiModel> = emptyList(),

    val showPartBottomSheet: Boolean = false,
    val showPartLeaderBottomSheet: Boolean = false,
    val showMemberBottomSheet: Boolean = false,
) : UiState {

    // 스터디원 -> 홍길동 외 2명
    val memberSummary: String
        get() = when {
            selectedMembers.isEmpty() -> ""
            selectedMembers.size == 1 -> selectedMembers.first().name
            else -> "${selectedMembers.first().name} 외 ${selectedMembers.size - 1}명"
        }

    // 담당 파트장 -> 홍길동, 김철수
    val partLeaderSummary: String
        get() = selectedPartLeaders.joinToString(", ") { it.name }

    val isRegisterEnabled: Boolean
        get() = groupName.isNotBlank()
                && selectedPart != null
                && selectedPartLeaders.isNotEmpty()
                && selectedMembers.isNotEmpty()
}