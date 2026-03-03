package com.umc.presentation.ui.act.study.group.create.model

import com.umc.domain.model.enums.UserPart
import com.umc.presentation.base.UiState
import com.umc.presentation.ui.act.study.common.model.MemberUiModel

data class AdminStudyGroupAddState(
    val groupName: String = "",
    val partLabel: String = "Web", // UI 표시용
    val leader: MemberUiModel? = null,
    val selectedMembers: List<MemberUiModel> = emptyList(),

    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
) : UiState {


    val canRegister: Boolean
        get() = groupName.isNotBlank()
                && leader != null
                && selectedMembers.isNotEmpty()
                && !isSubmitting
                && UserPart.from(partLabel) != UserPart.UNKNOWN
}