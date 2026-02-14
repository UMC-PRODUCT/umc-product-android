package com.umc.data.response.attendance

import com.umc.domain.model.act.check.UserCheckHistory
import com.umc.domain.model.enums.CategoryType
import com.umc.domain.model.enums.CheckHistoryStatus

data class UserCheckHistoryResponse(
    val attendanceId: Int,
    val scheduleId: Int,
    val scheduleName: String,
    val tag: List<String>,
    val scheduledDate: String,
    val startTime: String,
    val endTime: String,
    val status: String,
    val statusDisplay: String
) {
    companion object {
        fun UserCheckHistoryResponse.toUserCheckHistory(): UserCheckHistory {
            return UserCheckHistory(
                id = attendanceId,
                title = scheduleName,
                startTime = startTime,
                endTime = endTime,
                status = runCatching { CheckHistoryStatus.valueOf(status) }
                    .getOrDefault(CheckHistoryStatus.ABSENT),
                tags = tag.mapNotNull { tagName ->
                    runCatching { CategoryType.valueOf(tagName) }.getOrNull()
                }
            )
        }
    }
}