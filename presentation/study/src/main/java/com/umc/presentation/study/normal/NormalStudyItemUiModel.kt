package com.umc.presentation.study.normal

import com.umc.domain.model.enums.StudyStatus

data class NormalStudyItemUiModel(
    val id: Long,
    val title: String,
    val status: StudyStatus,
    val week: Int,
    val description: String,
    val platform: String = "Github",
    val isExpanded: Boolean = false,
    val isLocked: Boolean = false,
    val isBest: Boolean = false,
    val feedbackMessage: String? = null,
)