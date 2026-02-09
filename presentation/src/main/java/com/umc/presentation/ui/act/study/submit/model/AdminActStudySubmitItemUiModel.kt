package com.umc.presentation.ui.act.study.submit.model

data class AdminActStudySubmitItemUiModel(
    val userId: Long,
    val name: String,
    val nickname: String,
    val partLabel: String,
    val weekText: String,
    val studyTitle: String,
    val submitUrl: String,
    val isBest: Boolean = false,
)


