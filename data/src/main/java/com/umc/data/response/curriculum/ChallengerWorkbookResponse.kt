package com.umc.data.response.curriculum

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChallengerWorkbookResponse(
    @SerialName("challengerWorkbookId")
    val challengerWorkbookId: Long,

    @SerialName("originalWorkbookId")
    val originalWorkbookId: Long,

    @SerialName("receivedStudyGroupId")
    val receivedStudyGroupId: Long,

    @SerialName("memberId")
    val memberId: Long,

    @SerialName("isExcused")
    val isExcused: Boolean,

    @SerialName("excusedReason")
    val excusedReason: String?,

    @SerialName("content")
    val content: String?,

    @SerialName("isBestWorkbook")
    val isBestWorkbook: Boolean,

    @SerialName("status")
    val status: String,

    @SerialName("hasSubmission")
    val hasSubmission: Boolean,

    @SerialName("submission")
    val submission: MissionSubmissionResponse?,
)

@Serializable
data class MissionSubmissionResponse(
    @SerialName("missionSubmissionId")
    val missionSubmissionId: Long,

    @SerialName("originalWorkbookMissionId")
    val originalWorkbookMissionId: Long,

    @SerialName("submittedAsType")
    val submittedAsType: String,

    @SerialName("submittedContent")
    val submittedContent: String?,

    @SerialName("submittedAt")
    val submittedAt: String?,

    @SerialName("lastEditedAt")
    val lastEditedAt: String?,

    @SerialName("status")
    val status: String,

    @SerialName("hasFeedback")
    val hasFeedback: Boolean,

    @SerialName("feedbacks")
    val feedbacks: List<MissionFeedbackResponse>,
)

@Serializable
data class MissionFeedbackResponse(
    @SerialName("missionFeedbackId")
    val missionFeedbackId: Long,

    @SerialName("reviewerMemberId")
    val reviewerMemberId: Long,

    @SerialName("content")
    val content: String,

    @SerialName("feedbackResult")
    val feedbackResult: String,
)