package com.umc.data.response.organization

import kotlinx.serialization.Serializable

@Serializable
data class StudyGroupDetailResponse(
    val groupId: Int,
    val name: String,
    val part: String,
    val partDisplayName: String,
    val schools: List<StudySchoolResponse>,
    val createdAt: String,
    val memberCount: Int,
    val leader: GroupMemberResponse,
    val members: List<GroupMemberResponse>
)

@Serializable
data class StudySchoolResponse(
    val schoolId: Int,
    val schoolName: String,
    val logoImageId: String,
    val totalStudyGroupCount: Int,
    val totalMemberCount: Int
)