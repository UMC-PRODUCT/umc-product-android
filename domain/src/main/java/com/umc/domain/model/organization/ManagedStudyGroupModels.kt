package com.umc.domain.model.organization

data class ManagedStudyGroupPage(
    val content: List<ManagedStudyGroup>,
    val nextCursor: Long?,
    val hasNext: Boolean,
)

data class ManagedStudyGroup(
    val studyGroupId: Long,
    val name: String,
    val gisuId: Long,
    val studyPart: String,
    val createdAt: String,
    val mentors: List<ManagedStudyGroupMember>,
    val members: List<ManagedStudyGroupMember>,
) {
    val memberCount: Int
        get() = members.size
}

data class ManagedStudyGroupMember(
    val memberId: Long,
    val memberName: String,
    val schoolId: Long,
    val schoolName: String,
    val profileImageUrl: String?,
)