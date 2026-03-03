package com.umc.presentation.ui.act.study.group.create.model

import com.umc.presentation.base.UiState
import com.umc.presentation.ui.act.study.common.model.MemberUiModel

data class AdminStudyGroupAddState(
    val groupName: String = "",
    val partLabel: String = "Web", // UI 표시용
    val leader: MemberUiModel? = null,
    val selectedMembers: List<MemberUiModel> = emptyList(),

    // 로딩/에러
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
) : UiState {

    val membersSummary: String
        get() = when (selectedMembers.size) {
            0 -> ""
            1 -> selectedMembers.first().name
            else -> "${selectedMembers.first().name} 외 ${selectedMembers.size - 1}명"
        }

    val isRegisterEnabled: Boolean
        get() = groupName.isNotBlank() && leader != null
}