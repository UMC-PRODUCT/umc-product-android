package com.umc.domain.model.curriculum

data class WeeklyBestWorkbookPage(
    val content: List<WeeklyBestWorkbook>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean,
)

data class WeeklyBestWorkbook(
    val weeklyBestWorkbookId: Long,
    val memberId: Long,
    val studyGroupId: Long,
    val decidedMemberId: Long,
    val reason: String,
    val challengerWorkbookIds: List<Long>,
)