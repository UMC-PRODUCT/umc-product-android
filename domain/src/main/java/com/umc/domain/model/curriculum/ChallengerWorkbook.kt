package com.umc.domain.model.curriculum

data class ChallengerWorkbook(
    val challengerWorkbookId: Long,
    val originalWorkbookId: Long,
    val receivedStudyGroupId: Long,
    val memberId: Long,
    val isExcused: Boolean,
    val excusedReason: String?,
    val content: String?,
    val isBestWorkbook: Boolean,
    val status: String,
    val hasSubmission: Boolean,
    val submission: MissionSubmission?,
)

data class MissionSubmission(
    val missionSubmissionId: Long,
    val originalWorkbookMissionId: Long,
    val submittedAsType: String,
    val submittedContent: String?,
    val submittedAt: String?,
    val lastEditedAt: String?,
    val status: String,
    val hasFeedback: Boolean,
    val feedbacks: List<MissionFeedback>,
)

data class MissionFeedback(
    val missionFeedbackId: Long,
    val reviewerMemberId: Long,
    val content: String,
    val feedbackResult: String,
)