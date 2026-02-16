package com.umc.presentation.ui.act.study.submit.model

import com.umc.domain.model.curriculum.StudyGroup
import com.umc.presentation.base.UiState

data class AdminActStudySubmitState(
    val nextCursor: Long? = null,
    val hasNext: Boolean = false,


    val studyGroups: List<StudyGroup> = emptyList(),
    val availableWeeks: List<Int> = emptyList(),

    val selectedGroupId: Long? = null,
    val selectedWeek: Int? = null,

    val items: List<AdminActStudySubmitItemUiModel> = emptyList(),

    val bestDialogTarget: AdminActStudySubmitItemUiModel? = null,
    val reviewDialogTarget: AdminActStudySubmitItemUiModel? = null,
) : UiState
