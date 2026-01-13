package com.umc.presentation.ui.act.check

import com.umc.domain.model.act.check.AvailableSession

data class CheckAvailableUIModel(
    val session: AvailableSession,
    val isExpanded: Boolean = false
) {
    val formattedTime: String = "${session.startTime} - ${session.endTime}"
}