package com.umc.presentation.ui.act.study.group.create.model

import com.umc.presentation.base.UiState
import com.umc.presentation.ui.act.study.common.model.MemberUiModel

data class AdminStudyGroupAddState(
    val groupName: String = "",
    val part: String = "Web",
    val leader: MemberUiModel? = null,
    val selectedMembers: List<MemberUiModel> = emptyList(),
) : UiState {

    val membersSummary: String
        get() = when (selectedMembers.size) {
            0 -> ""
            1 -> selectedMembers.first().name
            else -> "${selectedMembers.first().name} 외 ${selectedMembers.size - 1}명"
        }
}
