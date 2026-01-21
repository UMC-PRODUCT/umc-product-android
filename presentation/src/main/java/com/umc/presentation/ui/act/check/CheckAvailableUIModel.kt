package com.umc.presentation.ui.act.check

import com.umc.domain.model.act.check.UserCheckAvailable
import com.umc.domain.model.enums.CheckAvailableStatus
import com.umc.presentation.util.UFormat

data class CheckAvailableUIModel(
    val session: UserCheckAvailable,
    val isExpanded: Boolean = false,
    val isWithinRange: Boolean = false,
    val address: String = ""
) {
    val formattedTime: String = UFormat.formatDuration(session.startTime, session.endTime)

    val isCertified: Boolean
        get() = if (session.status == CheckAvailableStatus.BEFORE) {
            // 출석 전에는 실시간 거리 기준
            isWithinRange
        } else {
            // 출석 후에는 서버에서 받아온 기록 기준
            session.isLocationCertified ?: false
        }
}