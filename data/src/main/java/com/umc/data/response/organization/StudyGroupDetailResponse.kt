package com.umc.data.response.organization

import kotlinx.serialization.Serializable
import com.umc.domain.model.organization.*


fun StudyGroupDetailResponse.toModel(): StudyGroupDetail {
    val memberModels = members.map { it.toModel() }

    val leaderModel =
        leader?.toModel()
            ?: memberModels.firstOrNull()
            ?: StudyGroupMember(
                challengerId = 0L,
                memberId = 0L,
                name = "",
                profileImageUrl = null
            )

    return StudyGroupDetail(
        groupId = groupId,
        name = name,
        part = part,
        partDisplayName = partDisplayName,
        schools = schools.map { StudyGroupSchool(it.schoolId, it.schoolName) },
        createdAt = createdAt,
        memberCount = if (memberCount > 0) memberCount else memberModels.size,
        leader = leaderModel,
        members = memberModels
    )
}
@Serializable
data class StudyGroupDetailResponse(
    val groupId: Long = 0,
    val name: String = "",
    val part: String = "",
    val partDisplayName: String = "",
    val schools: List<StudyGroupSchoolResponse> = emptyList(),
    val createdAt: String = "",
    val memberCount: Int = 0,
    val leader: GroupMemberResponse? = null,
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