package com.umc.data.response.schedule

import com.umc.domain.model.UDomainFormat.parseDateTime
import com.umc.domain.model.act.check.AdminPendingUser
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
        val (startDate, startTime) = startsAt.parseDateTime()
        val (_, endTime) = endsAt.parseDateTime()
        val pending = participants.count { it.attendanceStatus?.endsWith("_PENDING") == true }
        val present = participants.count { it.attendanceStatus in completedStatuses }
        val total = participants.size
        val pendingUsers = participants
            .filter { it.attendanceStatus?.endsWith("_PENDING") == true }
            .map { participant ->
                AdminPendingUser(
                    id = participant.memberId,
                    name = participant.name,
                    nickname = participant.nickname,
                    university = participant.schoolName,
                    profileImageUrl = participant.profileImageUrl,
                    requestTime = "",
                    hasLateReason = !participant.excuseReason.isNullOrBlank(),
                    lateReason = participant.excuseReason,
                )
            }
        return ScheduleListResponse(
            scheduleId = scheduleId,
            name = name,
            status = if (runCatching { Instant.parse(endsAt).isBefore(Instant.now()) }.getOrDefault(false)) {
                "COMPLETED"
            } else {
                "IN_PROGRESS"
            },
            date = startDate,
            startTime = startTime,
            endTime = endTime,
            locationName = location?.locationName.orEmpty(),
            sheetId = scheduleId,
            totalCount = total,
            presentCount = present,
            pendingCount = pending,
            attendanceRate = if (total == 0) 0.0 else present * 100.0 / total,
            pendingUsers = pendingUsers,
        )
    }

    private companion object {
        val completedStatuses = setOf("PRESENT", "LATE", "EXCUSED")
    }
}

data class AdminScheduleParticipantV2Response(
    val memberId: Long,
    val name: String,
    val nickname: String,
    val schoolName: String,
    val profileImageUrl: String? = null,
    val excuseReason: String? = null,
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
