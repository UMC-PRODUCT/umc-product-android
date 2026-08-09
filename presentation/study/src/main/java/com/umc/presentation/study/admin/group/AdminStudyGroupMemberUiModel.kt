package com.umc.presentation.study.admin.group

data class AdminStudyGroupMemberUiModel(
    val challengerId: Long,
    val name: String,
    val school: String = "",
    val profileImageUrl: String? = null,
)