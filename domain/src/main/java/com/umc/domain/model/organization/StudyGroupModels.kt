package com.umc.domain.model.organization

data class StudyGroupPage(
    val content: List<StudyGroupSummary>,
    val nextCursor: Long,
    val hasNext: Boolean,
)

data class StudyGroupSummary(
    val groupId: Long,
    val name: String,
    val memberCount: Int,
    val leader: StudyGroupMember,
    val members: List<StudyGroupMember>,
)

data class StudyGroupMember(
    val challengerId: Long,
    val memberId: Long,
    val name: String,
    val profileImageUrl: String?,
)

data class StudyGroupDetail(
    val groupId: Long,
    val name: String,
    val part: String,
    val partDisplayName: String,
    val schools: List<StudyGroupSchool>,
    val createdAt: String,
    val memberCount: Int,
    val leader: StudyGroupMember,
    val members: List<StudyGroupMember>,
)

data class StudyGroupSchool(
    val schoolId: Long,
    val schoolName: String,
)