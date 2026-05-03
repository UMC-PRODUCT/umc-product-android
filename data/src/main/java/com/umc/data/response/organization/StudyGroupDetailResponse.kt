package com.umc.data.response.organization

import com.umc.domain.model.enums.UserPart
import kotlinx.serialization.Serializable
import com.umc.domain.model.organization.*


fun StudyGroupDetailResponse.toModel(): StudyGroupDetail {
    val memberModels = members.map { it.toModel() }

    val leaderModel =
        mentors.firstOrNull()?.toModel()
            ?: memberModels.firstOrNull()
            ?: StudyGroupMember(
                challengerId = 0L,
                memberId = 0L,
                name = "",
                profileImageUrl = null
            )


    //기존 schools 대신 members를 통해 학교 정보 생성
    val schoolsInfo = members.map { StudyGroupSchool(it.schoolId, it.schoolName) }


    return StudyGroupDetail(
        groupId = studyGroupId,
        name = name,
        part = studyPart,
        partDisplayName = UserPart.from(studyPart).label,
        schools = schoolsInfo,
        createdAt = createdAt,
        memberCount = memberModels.size,
        leader = leaderModel,
        members = memberModels
    )
}
@Serializable
data class StudyGroupDetailResponse(
    val studyGroupId: Long = 0,
    val name: String = "",
    val gisuId: Long = 0,
    val studyPart: String = "",
    //val partDisplayName: String = "",
    //val schools: List<StudyGroupSchoolResponse> = emptyList(),
    val createdAt: String = "",
    //val memberCount: Int = 0,
    val mentors: List<GroupMemberResponse> = emptyList(),
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