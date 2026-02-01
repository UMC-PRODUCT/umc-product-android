package com.umc.presentation.ui.act.study.group

data class AdminStudyGroupItemUiModel(
    val groupId: Long,
    val title: String,
    val partLabel: String,
    val leaderName: String,
    val members: List<String>,
    val createdAtText: String,
    val memberCount: Int,
    val leaderUniv: String,
)
