package com.umc.data.response.organization

import com.umc.domain.model.organization.*

fun StudyGroupListResponse.toModel(): StudyGroupPage =
    StudyGroupPage(
        content = content.map { it.toModel() },
        nextCursor = nextCursor.toLong(),
        hasNext = hasNext
    )

fun StudyGroupResponse.toModel(): StudyGroupSummary =
    StudyGroupSummary(
        groupId = groupId.toLong(),
        name = name,
        memberCount = memberCount,
        leader = leader.toModel(),
        members = members.map { it.toModel() }
    )

fun GroupMemberResponse.toModel(): StudyGroupMember =
    StudyGroupMember(
        challengerId = challengerId.toLong(),
        memberId = memberId.toLong(),
        name = name,
        profileImageUrl = profileImageUrl
    )