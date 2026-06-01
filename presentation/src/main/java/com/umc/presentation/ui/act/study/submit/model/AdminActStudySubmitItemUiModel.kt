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
    val isBestEnabled: Boolean,
    val isReviewEnabled: Boolean,
) {
    val isBest: Boolean get() = status == "BEST"

    val slotStatus: String
        get() = when (status) {
            "PASS", "BEST" -> "PASS"
            "FAIL" -> "FAIL"
            else -> ""
        }

    val markStatus: String? get() = when (status) {
        "PASS", "FAIL", "BEST" -> status
        else -> null
    }
}