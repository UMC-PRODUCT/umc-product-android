package com.umc.presentation.study.admin.submit

data class AdminSubmitItemUiModel(
    val id: Long,
    val name: String,
    val nickname: String,
    val partLabel: String,
    val weekText: String,
    val studyTitle: String,
    val schoolName: String,
    val status: String,
) {
    val isBest: Boolean get() = status == "BEST"
    val markStatus: String? get() = when (status) {
        "PASS", "FAIL", "BEST" -> status
        else -> null
    }
}