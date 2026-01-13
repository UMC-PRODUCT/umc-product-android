package com.umc.presentation.ui.act.check

import com.umc.domain.model.act.check.CheckAvailable

data class CheckAvailableUIModel(
    val session: CheckAvailable,
    val isExpanded: Boolean = false
) {
    val formattedTime: String = "${session.startTime} - ${session.endTime}"
}