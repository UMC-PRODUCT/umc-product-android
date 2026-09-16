package com.umc.domain.model.organization

import com.umc.domain.model.enums.UserPart

data class ManagedStudyGroupPage(
    val content: List<ManagedStudyGroup>,
    val nextCursor: Long?,
    val hasNext: Boolean,
)

data class ManagedStudyGroup(
    val studyGroupId: Long,
    val name: String,
    val gisuId: Long,
    /** 서버 원본 파트 값. 수정 요청에 그대로 돌려보내야 해서 가공하지 않는다. */
    val studyPart: String,
    val createdAt: String,
    val mentors: List<ManagedStudyGroupMember>,
    val members: List<ManagedStudyGroupMember>,
) {
    val memberCount: Int
        get() = members.size

    /** 화면에 보여줄 파트 */
    val displayPart: UserPart
        get() = UserPart.from(studyPart)
}

data class ManagedStudyGroupMember(
    val memberId: Long,
    val memberName: String,
    val schoolId: Long,
    val schoolName: String,
    val profileImageUrl: String?,
)