package com.umc.presentation.study.admin.group.create

data class AdminStudyGroupCreateMemberUiModel(
    val id: Long,
    val name: String,
    val displayName: String,
    val partLabel: String,
    val school: String,
)