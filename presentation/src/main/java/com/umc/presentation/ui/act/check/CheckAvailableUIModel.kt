package com.umc.presentation.ui.act.check

import com.umc.domain.model.act.check.CheckAvailable

data class CheckAvailableUIModel(
    val session: CheckAvailable,
    val isExpanded: Boolean = false  // 리스트 아이템의 확장 여부 상태 저장
) {
    val formattedTime: String = "${session.startTime} - ${session.endTime}"
}