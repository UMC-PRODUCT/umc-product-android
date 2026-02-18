package com.umc.data.response.attendance

import com.umc.domain.model.act.challenger.ChallengerInfoHistory
import com.umc.domain.model.enums.CheckHistoryStatus

data class ChallengerAttendanceHistoryResponse(
    val attendanceId: Long,
    val scheduleId: Long,
    val scheduleName: String,
    val tag: List<String>,
    val scheduledDate: String,
    val startTime: String,
    val endTime: String,
    val status: CheckHistoryStatus,
    val statusDisplay: String,
    val sheetId: Long,
    val locationName: String,
    val locationVerified: Boolean,
    val memo: String?,
    val checkedAt: String?
) {
    companion object {
        fun ChallengerAttendanceHistoryResponse.toChallengerInfoHistory() = ChallengerInfoHistory(
            title = this.scheduleName,
            status = this.status
        )
    }
}