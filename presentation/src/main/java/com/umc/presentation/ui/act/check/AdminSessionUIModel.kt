package com.umc.presentation.ui.act.check

import com.umc.domain.model.act.check.AdminSessionCheck
import com.umc.presentation.util.UFormat

data class AdminSessionUIModel(
    val session: AdminSessionCheck,
    val hasApprovePermission: Boolean = false,
    val hasWritePermission: Boolean = false
) {
    val formattedDate: String = UFormat.formatDate(session.startTime)
    val formattedTime: String = UFormat.formatDuration(session.startTime, session.endTime)
}