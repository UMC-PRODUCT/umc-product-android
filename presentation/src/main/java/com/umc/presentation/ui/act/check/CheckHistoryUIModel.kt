package com.umc.presentation.ui.act.check

import com.umc.domain.model.act.check.CheckHistory
import com.umc.presentation.util.UTime

data class CheckHistoryUIModel(
    val history: CheckHistory,
    val isFirst: Boolean,
    val isLast: Boolean
) {
    val formattedTime: String = UTime.formatDuration(history.startTime, history.endTime)
}