package com.umc.data.response.organization

import com.umc.domain.model.organization.StudyGroupMember
import com.umc.domain.model.organization.StudyGroupPage
import com.umc.domain.model.organization.StudyGroupSummary

fun StudyGroupListResponse.toModel(): StudyGroupPage {
    return StudyGroupPage(
        content = content.map { it.toModel() },
        nextCursor = nextCursor ?: 0L,
        hasNext = hasNext
    )
}

fun StudyGroupResponse.toModel(): StudyGroupSummary {
    val memberModels = members.map { it.toModel() }

    val leaderModel = leader?.toModel()
        ?: memberModels.firstOrNull()
        ?: StudyGroupMember(
            challengerId = 0L,
            memberId = 0L,
            name = "",
            profileImageUrl = null
        )

    return StudyGroupSummary(
        groupId = groupId,
        name = name,
        memberCount = if (memberCount > 0) memberCount else memberModels.size,
        leader = leaderModel,
        members = memberModels
    )
}


fun GroupMemberResponse.toModel(): StudyGroupMember {
    return StudyGroupMember(
        challengerId = challengerId,
        memberId = memberId,
        name = name,
        profileImageUrl = profileImageUrl
    )
}