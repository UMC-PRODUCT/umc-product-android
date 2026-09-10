package com.umc.domain.model.curriculum

import com.umc.domain.model.enums.UserPart

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
    /** 트랙 기수면 트랙, 파트 기수면 파트 */
    val part: UserPart,
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