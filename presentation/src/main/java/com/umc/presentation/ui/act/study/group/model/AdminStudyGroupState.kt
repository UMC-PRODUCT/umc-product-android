package com.umc.presentation.ui.act.study.group.model

import com.umc.presentation.base.UiState

data class AdminStudyGroupState(
    val groups: List<AdminStudyGroupItemUiModel> = emptyList()

) : UiState
