package com.umc.presentation.study.admin.submit

data class AdminSubmitItemUiModel(
    val id: Long,
    val challengerWorkbookId: Long? = null,
    val memberId: Long,
    val studyGroupId: Long,
    val weeklyCurriculumId: Long,
    val weeklyBestWorkbookId: Long? = null,
    val name: String,
    val nickname: String,
    val partLabel: String,
    val weekText: String,
    val studyTitle: String,
    val schoolName: String,
    val status: String,
    val submitUrl: String = "",
    val bestComment: String = "",
    val isBestRegistered: Boolean = false,
) {
    val isBest: Boolean
        get() = status == "BEST"

    val markStatus: String?
        get() = when (status) {
            "PASS" -> "PASS"
            "FAIL" -> "FAIL"
            "BEST" -> "PASS"
            else -> null
        }
}