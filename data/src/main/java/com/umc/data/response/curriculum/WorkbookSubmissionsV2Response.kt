package com.umc.data.response.curriculum

data class WorkbookSubmissionsV2Response(
    val content: List<StudyMemberSubmissionResponse>,
    val nextCursor: Long?,
    val hasNext: Boolean,
)

data class StudyMemberSubmissionResponse(
    val studyGroupMemberId: Long,
    val memberId: Long,
    val memberName: String,
    val nickname: String,
    val schoolName: String,
    val profileImageUrl: String?,
    val studyGroupId: Long,
    val studyGroupName: String,
    val part: String,
    val weeks: List<StudyMemberSubmissionWeekResponse>,
)

data class StudyMemberSubmissionWeekResponse(
    val weekNo: Long,
    val weeklyCurriculumId: Long,
    val challengerWorkbookId: Long?,
    val status: String,
    val isBest: Boolean,
    val content: String?,
)