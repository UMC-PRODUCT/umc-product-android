package com.umc.data.remote.response.curriculum

data class WorkbookSubmissionsResponse(
    val content: List<WorkbookSubmissionItemResponse>,
    val nextCursor: Long?,
    val hasNext: Boolean,
)

data class WorkbookSubmissionItemResponse(
    val challengerWorkbookId: Long,
    val challengerId: Long,
    val challengerName: String,
    val profileImageUrl: String?,
    val schoolName: String,
    val part: String,
    val workbookTitle: String,
    val status: String,
)


