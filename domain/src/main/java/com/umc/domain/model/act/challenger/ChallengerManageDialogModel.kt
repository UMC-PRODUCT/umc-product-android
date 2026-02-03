package com.umc.domain.model.act.challenger

data class UChallengerManageDialogModel(
    val name: String,
    val university: String,
    val part: String,
    val hasNewAbsence: Boolean = false,
    val absenceCount: Int = 0,
    val warningCount: Int = 0,
    val history: List<HistoryItem> = emptyList()
)

data class HistoryItem(
    val date: String,
    val type: String,
    val historyType: HistoryType,
    val count: Double,
    val reason: String? = null
)

enum class HistoryType(val label: String) {
    WARNING("경고"),
    OUT("아웃")
}