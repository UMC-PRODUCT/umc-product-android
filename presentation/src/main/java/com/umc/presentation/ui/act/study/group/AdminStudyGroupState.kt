package com.umc.presentation.ui.act.study.group

import com.umc.presentation.base.UiState

data class AdminStudyGroupState(
    val groups: List<AdminStudyGroupItemUiModel> = emptyList()
) : UiState
