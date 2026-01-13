package com.umc.presentation.ui.act.check

import com.umc.domain.model.act.check.AttendanceHistory

data class CheckHistoryUIModel(
    val history: AttendanceHistory,
    val isFirst: Boolean,
    val isLast: Boolean
) {
    val formattedTime: String = "${history.startTime} - ${history.endTime}"
}