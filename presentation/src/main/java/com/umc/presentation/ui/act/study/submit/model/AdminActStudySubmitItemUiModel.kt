package com.umc.presentation.ui.act.study.submit.model

data class AdminActStudySubmitItemUiModel(
    val challengerWorkbookId: Long,
    val userId: Long,
    val name: String,
    val nickname: String,
    val partLabel: String,
    val weekText: String,
    val studyTitle: String,
    val submitUrl: String,
    val schoolName: String,
    val profileImageUrl: String?,
    val status: String,
) {
    val isBest: Boolean get() = status == "BEST"
    val isBestEnabled: Boolean get() = status == "PASS"

    val isReviewEnabled: Boolean get() = status == "SUBMITTED"

    val markStatus: String? get() = when (status) {
        "PASS", "FAIL" -> status
        "BEST" -> "PASS"
        else -> null
    }
}