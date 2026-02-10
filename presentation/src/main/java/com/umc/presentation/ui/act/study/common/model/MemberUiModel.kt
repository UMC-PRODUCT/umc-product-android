package com.umc.presentation.ui.act.study.common.model

data class MemberUiModel(
    val id: Long,
    val name: String,
    val part: String,
    val generationText: String,
    val university: String? = null,
)
