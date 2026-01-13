package com.umc.presentation.ui.act.check

import com.umc.domain.model.act.check.CheckHistory

data class CheckHistoryUIModel(
    val history: CheckHistory,
    val isFirst: Boolean,
    val isLast: Boolean
) {
    val formattedTime: String = "${history.startTime} - ${history.endTime}"
}