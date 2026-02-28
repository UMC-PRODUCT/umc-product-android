package com.umc.presentation.ui.act.study.common.model

data class MemberUiModel(
    val challengerId: Long,
    val name: String,
    val nickname: String,
    val partLabel: String,
    val gisuLabel: String,
    val schoolName: String,
    val profileImageUrl: String? = null,
)