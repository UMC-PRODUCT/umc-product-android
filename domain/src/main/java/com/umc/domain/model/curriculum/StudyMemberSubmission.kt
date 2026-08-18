package com.umc.domain.model.curriculum

data class StudyMemberSubmissionPage(
    val content: List<StudyMemberSubmission>,
    val nextCursor: Long?,
    val hasNext: Boolean,
)

data class StudyMemberSubmission(
    val studyGroupMemberId: Long,
    val memberId: Long,
    val memberName: String,
    val nickname: String,
    val schoolName: String,
    val profileImageUrl: String?,
    val studyGroupId: Long,
    val studyGroupName: String,
    val part: String,
    val weeks: List<StudyMemberSubmissionWeek>,
)

data class StudyMemberSubmissionWeek(
    val weekNo: Long,
    val weeklyCurriculumId: Long,
    val challengerWorkbookId: Long?,
    val status: String,
    val isBest: Boolean,
    val content: String?,
)