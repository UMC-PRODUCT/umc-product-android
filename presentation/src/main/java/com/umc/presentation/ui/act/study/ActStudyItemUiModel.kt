package com.umc.presentation.ui.act.study

import com.umc.domain.model.enums.StudyStatus
import com.umc.domain.model.enums.SubmitState

data class ActStudyItemUiModel(
    val id: Long,
    val platform: String,
    val title: String,
    val status: StudyStatus,
    val week: Int = id.toInt(),
    val isExpanded: Boolean = false,
    val link: String = "",
    val submitState: SubmitState = SubmitState.IDLE,
    val isLocked: Boolean = false,
    val description: String,
)
