package com.umc.presentation.ui.act.study.group.model

data class AdminStudyGroupItemUiModel(
    val groupId: Long,
    val title: String,
    val partLabel: String,

    val leaderName: String,
    val leaderChallengerId: Long,
    val leaderProfileImageUrl: String?,
    val members: List<AdminStudyGroupMemberUiModel>,
    val memberChallengerIds: List<Long> = emptyList(),

    val createdAtRaw: String,
    val memberCount: Int,
    val leaderUniv: String,
) {
    val createdAtText: String
        get() = createdAtRaw.takeIf { it.length >= 10 }
            ?.let { "생성일: " + it.take(10).replace("-", ".") }
            ?: "생성일: -"
}