package com.umc.domain.model.curriculum

import com.umc.domain.model.enums.UserPart

data class WorkbookSubmissionItem(
    val challengerWorkbookId: Long,
    val challengerId: Long,
    val memberName: String,
    val challengerName: String,
    val profileImageUrl: String,
    val schoolName: String,
    val part: UserPart,
    val workbookTitle: String,
    val status: String,
)

data class StudyGroup(
    val id: Long,
    val name: String
)

data class ChallengerWorkbookSubmission(
    val challengerWorkbookId: Long?,
    val submission: String?
)