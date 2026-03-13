package com.umc.data.remote.response.curriculum

import com.umc.domain.model.curriculum.ChallengerWorkbookSubmission

data class WorkbookSubmissionsResponse(
    val content: List<WorkbookSubmissionItemResponse>,
    val nextCursor: Long?,
    val hasNext: Boolean,
)

data class WorkbookSubmissionItemResponse(
    val challengerWorkbookId: Long,
    val challengerId: Long,
    val memberName: String?,
    val challengerName: String,
    val profileImageUrl: String?,
    val schoolName: String,
    val part: String,
    val workbookTitle: String,
    val status: String,
)

data class ChallengerWorkbookSubmissionResponse(
    val challengerWorkbookId: String?,
    val submission: String?
) {
    fun toModel(): ChallengerWorkbookSubmission {
        return ChallengerWorkbookSubmission(
            challengerWorkbookId = challengerWorkbookId?.toLongOrNull(),
            submission = submission
        )
    }
}
