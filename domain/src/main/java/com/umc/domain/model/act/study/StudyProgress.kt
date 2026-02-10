package com.umc.domain.model.study

import com.umc.domain.model.enums.UserPart
import com.umc.domain.model.enums.WorkbookMissionType
import com.umc.domain.model.enums.WorkbookStatus

data class StudyProgress(
    val curriculumId: Long,
    val curriculumTitle: String,
    val part: UserPart,
    val completedCount: Int,
    val totalCount: Int,
    val workbooks: List<WorkbookProgress>
)

data class WorkbookProgress(
    val challengerWorkbookId: Long,
    val weekNo: Int,
    val title: String,
    val description: String,
    val missionType: WorkbookMissionType,
    val status: WorkbookStatus,
    val isReleased: Boolean,
    val isInProgress: Boolean
)
