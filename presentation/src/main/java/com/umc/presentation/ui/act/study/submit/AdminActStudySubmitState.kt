package com.umc.presentation.ui.act.study.submit

import com.umc.presentation.base.UiState

data class AdminActStudySubmitState(
    val selectedWeek: Int = 1,
    val selectedGroupId: Long? = null,
    val items: List<AdminActStudySubmitItemUiModel> = emptyList(),

    val bestDialogTarget: AdminActStudySubmitItemUiModel? = null,
    val reviewDialogTarget: AdminActStudySubmitItemUiModel? = null,
) : UiState
