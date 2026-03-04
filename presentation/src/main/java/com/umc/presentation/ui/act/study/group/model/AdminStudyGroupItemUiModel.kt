package com.umc.presentation.ui.act.study.group.model

data class AdminStudyGroupItemUiModel(
    val groupId: Long,
    val title: String,
    val partLabel: String,

    val leaderName: String,
    val leaderChallengerId: Long,

    val members: List<String>,
    val memberChallengerIds: List<Long> = emptyList(),

    val createdAtRaw: String,
    val memberCount: Int,
    val leaderUniv: String,
) {
    val createdAtText: String
        get() = "생성일: " + createdAtRaw.take(10).replace("-", ".")
}