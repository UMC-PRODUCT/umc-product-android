package com.umc.presentation.ui.act.study.submit.model

import com.umc.domain.model.curriculum.StudyGroup
import com.umc.presentation.base.UiState

data class AdminActStudySubmitState(
    val nextCursor: Long? = null,
    val hasNext: Boolean = false,

    val selectedWeekText: String = "10주차",

    val studyGroups: List<StudyGroup> = emptyList(),
    val availableWeeks: List<Int> = emptyList(),

    val selectedGroupId: Long? = null,
    val selectedWeek: Int = 10,

    val items: List<AdminActStudySubmitItemUiModel> = emptyList(),

    val bestDialogTarget: AdminActStudySubmitItemUiModel? = null,
    val reviewDialogTarget: AdminActStudySubmitItemUiModel? = null,
) : UiState
