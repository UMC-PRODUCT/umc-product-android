package com.umc.domain.model.curriculum

data class OriginalWorkbook(
    val originalWorkbookId: Long,
    val title: String,
    val description: String,
    val url: String,
    val content: String,
    val type: String,
    val status: String,
    val releasedAt: String?,
    val releasedMemberId: Long?,
    val missions: OriginalWorkbookMission?,
)

data class OriginalWorkbookMission(
    val originalWorkbookId: Long,
    val originalWorkbookMissionId: Long,
    val title: String,
    val description: String,
    val missionType: String,
    val isNecessary: Boolean,
)