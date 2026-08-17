package com.umc.data.response.curriculum

data class WeeklyBestWorkbooksResponse(
    val content: List<BestWorkbookResponse>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean,
)

data class BestWorkbookResponse(
    val weeklyBestWorkbookEntityId: Long,
    val memberId: Long,
    val studyGroupId: Long,
    val decidedMemberId: Long,
    val reason: String,
    val challengerWorkbooks: List<ChallengerWorkbookResponse>,
)