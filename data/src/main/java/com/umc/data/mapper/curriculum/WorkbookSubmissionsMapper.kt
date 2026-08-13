package com.umc.data.mapper.curriculum

import com.umc.data.remote.response.curriculum.WorkbookSubmissionItemResponse
import com.umc.data.remote.response.curriculum.WorkbookSubmissionsResponse
import com.umc.data.response.curriculum.ChallengerWorkbookResponse
import com.umc.data.response.curriculum.MissionFeedbackResponse
import com.umc.data.response.curriculum.MissionSubmissionResponse
import com.umc.domain.model.base.CursorPage
import com.umc.domain.model.curriculum.ChallengerWorkbook
import com.umc.domain.model.curriculum.MissionFeedback
import com.umc.domain.model.curriculum.MissionSubmission
import com.umc.domain.model.curriculum.WorkbookSubmissionItem
import com.umc.domain.model.enums.UserPart

fun WorkbookSubmissionItemResponse.toDomain(): WorkbookSubmissionItem =
    WorkbookSubmissionItem(
        challengerWorkbookId = challengerWorkbookId,
        challengerId = challengerId,
        memberName = memberName.orEmpty(),
        challengerName = challengerName,
        profileImageUrl = profileImageUrl.orEmpty(),
        schoolName = schoolName,
        part = UserPart.from(part),
        workbookTitle = workbookTitle,
        status = status,
    )

fun WorkbookSubmissionsResponse.toDomain(): CursorPage<WorkbookSubmissionItem> =
    CursorPage(
        content = content.map { it.toDomain() },
        nextCursor = nextCursor,
        hasNext = hasNext,
    )

fun ChallengerWorkbookResponse.toDomain(): ChallengerWorkbook =
    ChallengerWorkbook(
        challengerWorkbookId = challengerWorkbookId ?: 0L,
        originalWorkbookId = originalWorkbookId ?: 0L,
        receivedStudyGroupId = receivedStudyGroupId ?: 0L,
        memberId = memberId ?: 0L,
        isExcused = isExcused ?: false,
        excusedReason = excusedReason,
        content = content,
        isBestWorkbook = isBestWorkbook ?: false,
        status = status.orEmpty(),
        hasSubmission = hasSubmission ?: false,
        submission = submission?.toDomain(),
    )

fun MissionSubmissionResponse.toDomain(): MissionSubmission =
    MissionSubmission(
        missionSubmissionId = missionSubmissionId ?: 0L,
        originalWorkbookMissionId = originalWorkbookMissionId ?: 0L,
        submittedAsType = submittedAsType.orEmpty(),
        submittedContent = submittedContent,
        submittedAt = submittedAt,
        lastEditedAt = lastEditedAt,
        status = status.orEmpty(),
        hasFeedback = hasFeedback ?: false,
        feedbacks = feedbacks.orEmpty().map { it.toDomain() },
    )

fun MissionFeedbackResponse.toDomain(): MissionFeedback =
    MissionFeedback(
        missionFeedbackId = missionFeedbackId ?: 0L,
        reviewerMemberId = reviewerMemberId ?: 0L,
        content = content.orEmpty(),
        feedbackResult = feedbackResult.orEmpty(),
    )