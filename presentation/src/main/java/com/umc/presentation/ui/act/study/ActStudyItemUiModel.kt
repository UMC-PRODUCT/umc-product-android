package com.umc.presentation.ui.act.study

import com.umc.domain.model.enums.StudyStatus
import com.umc.domain.model.enums.SubmitState
import com.umc.domain.model.enums.WorkbookMissionType

data class ActStudyItemUiModel(
    val id: Long,
    val platform: String,
    val title: String,
    val status: StudyStatus,
    val week: Int = id.toInt(),
    val isExpanded: Boolean = false,
    val link: String = "",
    val input: String = "",
    val submitState: SubmitState = SubmitState.IDLE,
    val isLocked: Boolean = false,
    val description: String,
    val isBest: Boolean = false,
    val missionType: WorkbookMissionType = WorkbookMissionType.UNKNOWN,
)
