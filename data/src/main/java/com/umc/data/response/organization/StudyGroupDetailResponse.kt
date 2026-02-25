package com.umc.data.response.organization

import kotlinx.serialization.Serializable
import com.umc.domain.model.organization.*


fun StudyGroupDetailResponse.toModel(): StudyGroupDetail =
    StudyGroupDetail(
        groupId = groupId,
        name = name,
        part = part,
        partDisplayName = partDisplayName,
        schools = schools.map { StudyGroupSchool(it.schoolId, it.schoolName) },
        createdAt = createdAt,
        memberCount = memberCount,
        leader = leader.toModel(),
        members = members.map { it.toModel() }
    )
@Serializable
data class StudyGroupDetailResponse(
    val groupId: Long = 0,
    val name: String = "",
    val part: String = "",
    val partDisplayName: String = "",
    val schools: List<StudyGroupSchoolResponse> = emptyList(),
    val createdAt: String = "",
    val memberCount: Int = 0,
    val leader: GroupMemberResponse = GroupMemberResponse(),
    val members: List<GroupMemberResponse> = emptyList(),
)

@Serializable
data class StudyGroupSchoolResponse(
    val schoolId: Long = 0,
    val schoolName: String = "",
    val logoImageId: String? = null,
    val totalStudyGroupCount: Int = 0,
    val totalMemberCount: Int = 0,
)