package com.umc.presentation.ui.act.check

import com.umc.domain.model.act.check.AdminSessionCheck

data class AdminSessionUIModel(
    val session: AdminSessionCheck,
    val hasApprovePermission: Boolean = false,
    val hasWritePermission: Boolean = false
) {
    val formattedDate: String = session.date
    val formattedTime: String = "${session.startTime} - ${session.endTime}"
}