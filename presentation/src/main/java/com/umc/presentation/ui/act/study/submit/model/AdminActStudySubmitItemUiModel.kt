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

    val uiStatus: String get() = if (status == "BEST") "PASS" else status

    // 다음주 수정사항 일단은 false
    val isBest: Boolean get() = false

//    val isBestEnabled: Boolean get() = status == "PASS"
//
//    val isReviewEnabled: Boolean get() = status == "SUBMITTED"

    val isBestEnabled: Boolean get() = uiStatus == "PASS"
    val isReviewEnabled: Boolean get() = uiStatus == "SUBMITTED"

    val markStatus: String? get() = when (status) {
        "PASS", "FAIL" -> status
//        "BEST" -> "PASS"
        else -> null
    }
}