package com.umc.data.response.organization

import com.umc.domain.model.organization.ManagedStudyGroup
import com.umc.domain.model.organization.ManagedStudyGroupMember
import com.umc.domain.model.organization.ManagedStudyGroupPage

fun ManagedStudyGroupListResponse.toModel(): ManagedStudyGroupPage {
    return ManagedStudyGroupPage(
        content = content.orEmpty().map { it.toModel() },
        nextCursor = nextCursor,
        hasNext = hasNext ?: false,
    )
}

fun ManagedStudyGroupResponse.toModel(): ManagedStudyGroup {
    return ManagedStudyGroup(
        studyGroupId = studyGroupId ?: 0L,
        name = name.orEmpty(),
        gisuId = gisuId ?: 0L,
        studyPart = studyPart.orEmpty(),
        createdAt = createdAt.orEmpty(),
        mentors = mentors.orEmpty().map { it.toModel() },
        members = members.orEmpty().map { it.toModel() },
    )
}

fun ManagedStudyGroupMemberResponse.toModel(): ManagedStudyGroupMember {
    return ManagedStudyGroupMember(
        memberId = memberId ?: 0L,
        memberName = memberName.orEmpty(),
        schoolId = schoolId ?: 0L,
        schoolName = schoolName.orEmpty(),
        profileImageUrl = profileImageUrl,
    )
}