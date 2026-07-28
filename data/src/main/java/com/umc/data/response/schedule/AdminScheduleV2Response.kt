package com.umc.data.response.schedule

import java.time.Instant

data class AdminScheduleV2Response(
    val scheduleId: Long,
    val name: String,
    val startsAt: String,
    val endsAt: String,
    val location: ScheduleLocationV2Response? = null,
    val participants: List<AdminScheduleParticipantV2Response> = emptyList()
) {
    fun toLegacy(): ScheduleListResponse {
        val pending = participants.count { it.attendanceStatus?.endsWith("_PENDING") == true }
        val present = participants.count { it.attendanceStatus in completedStatuses }
        val total = participants.size
        return ScheduleListResponse(
            scheduleId = scheduleId,
            name = name,
            status = if (runCatching { Instant.parse(endsAt).isBefore(Instant.now()) }.getOrDefault(false)) {
                "COMPLETED"
            } else {
                "IN_PROGRESS"
            },
            date = startsAt.substringBefore("T"),
            startTime = startsAt.substringAfter("T").take(5),
            endTime = endsAt.substringAfter("T").take(5),
            locationName = location?.locationName.orEmpty(),
            sheetId = scheduleId,
            totalCount = total,
            presentCount = present,
            pendingCount = pending,
            attendanceRate = if (total == 0) 0.0 else present * 100.0 / total
        )
    }

    private companion object {
        val completedStatuses = setOf("PRESENT", "LATE", "EXCUSED")
    }
}

data class AdminScheduleParticipantV2Response(
    val memberId: Long,
    val attendanceStatus: String? = null
)

data class ScheduleLocationV2Response(
    val latitude: Double,
    val longitude: Double,
    val locationName: String
)

data class UpdateScheduleLocationV2Request(
    val location: ScheduleLocationV2Response,
    val isOnline: Boolean = false
)
