package com.umc.presentation.study.admin.group

import com.umc.domain.model.organization.ManagedStudyGroup

fun ManagedStudyGroup.toUiModel(): AdminStudyGroupItemUiModel {
    val leader = mentors.firstOrNull()

    return AdminStudyGroupItemUiModel(
        groupId = studyGroupId,
        title = name,
        partLabel = studyPart.toPartLabel(),

        leaderName = leader?.memberName.orEmpty(),

        // managed API에는 challengerId가 없으므로 memberId 사용
        leaderChallengerId = leader?.memberId ?: 0L,

        leaderProfileImageUrl = leader?.profileImageUrl,

        members = members.map { member ->
            AdminStudyGroupMemberUiModel(
                challengerId = member.memberId,
                name = member.memberName,
                school = member.schoolName,
                profileImageUrl = member.profileImageUrl,
            )
        },

        memberChallengerIds = members.map { member ->
            member.memberId
        },

        createdAtRaw = createdAt,
        memberCount = members.size,
        leaderUniv = leader?.schoolName.orEmpty(),
    )
}

private fun String.toPartLabel(): String {
    return when (uppercase()) {
        "PLAN" -> "Plan"
        "DESIGN" -> "Design"
        "WEB" -> "Web"
        "ANDROID" -> "Android"
        "IOS" -> "iOS"
        "NODEJS" -> "Node.js"
        "SPRINGBOOT" -> "Spring Boot"
        "ADMIN" -> "Admin"
        else -> this
    }
}