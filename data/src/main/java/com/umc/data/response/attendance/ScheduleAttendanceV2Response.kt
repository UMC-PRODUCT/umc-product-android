package com.umc.data.response.attendance

import com.umc.domain.model.UDomainFormat.parseDateTime
import com.umc.domain.model.act.check.AdminPendingUser
import com.umc.domain.model.act.check.UserCheckAvailable
import com.umc.domain.model.act.check.UserCheckHistory
import com.umc.domain.model.enums.CategoryType
import com.umc.domain.model.enums.CheckAvailableStatus
import com.umc.domain.model.enums.CheckHistoryStatus

data class ScheduleAttendanceV2Response(
    val scheduleId: Long,
    val name: String,
    val tags: List<String> = emptyList(),
    val startsAt: String,
    val endsAt: String,
    val isOnline: Boolean = false,
    val location: ScheduleLocationV2Response? = null,
    val attendanceStatus: String? = null,
    val isAttendanceChecked: Boolean = false
) {
    fun toAvailable(): UserCheckAvailable {
        val (_, startTime) = startsAt.parseDateTime()
        val (_, endTime) = endsAt.parseDateTime()
        return UserCheckAvailable(
            id = scheduleId,
            sheetId = scheduleId,
            title = name,
            tags = tags.mapNotNull { runCatching { CategoryType.valueOf(it) }.getOrNull() },
            startTime = startTime,
            endTime = endTime,
            status = CheckAvailableStatus.fromServerValue(attendanceStatus),
            latitude = location?.latitude ?: 0.0,
            longitude = location?.longitude ?: 0.0,
            address = location?.locationName.orEmpty(),
            isLocationCertified = null,
            isOnline = isOnline,
        )
    }

    fun toHistory(index: Int): UserCheckHistory {
        val (_, startTime) = startsAt.parseDateTime()
        val (_, endTime) = endsAt.parseDateTime()
        return UserCheckHistory(
            id = scheduleId.toInt(),
            title = name,
            startTime = startTime,
            endTime = endTime,
            status = when (attendanceStatus) {
                "PRESENT", "EXCUSED" -> CheckHistoryStatus.PRESENT
                "LATE" -> CheckHistoryStatus.LATE
                else -> CheckHistoryStatus.ABSENT
            },
            tags = tags.mapNotNull { runCatching { CategoryType.valueOf(it) }.getOrNull() }
        )
    }
}

data class ScheduleLocationV2Response(
    val latitude: Double,
    val longitude: Double,
    val locationName: String
)

data class AdminScheduleAttendanceV2Response(
    val scheduleId: Long,
    val participants: List<AdminScheduleParticipantV2Response> = emptyList()
) {
    fun pendingUsers() = participants
        .filter { it.attendanceStatus?.endsWith("_PENDING") == true }
        .map {
            AdminPendingUser(
                id = it.memberId,
                name = it.name,
                nickname = it.nickname,
                university = it.schoolName,
                profileImageUrl = it.profileImageUrl,
                requestTime = "",
                hasLateReason = !it.excuseReason.isNullOrBlank(),
                lateReason = it.excuseReason
            )
        }
}

data class AdminScheduleParticipantV2Response(
    val memberId: Long,
    val name: String,
    val nickname: String,
    val schoolName: String,
    val profileImageUrl: String? = null,
    val attendanceStatus: String? = null,
    val excuseReason: String? = null
)

data class AttendanceDecisionV2Request(
    val participantMemberId: Long,
    val isApproved: Boolean,
    val reason: String? = null
)

data class ScheduleParticipantAttendanceV2Response(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val status: String? = null,
    val excuseReason: String? = null,
    val isPendingDecision: Boolean = false,
    val hasDecisionMakerMember: Boolean = false,
    val decidedAt: String? = null,
    val decisionReason: String? = null,
)

data class AttendanceCheckV2Request(
    val locationVerified: Boolean,
    val latitude: Double? = null,
    val longitude: Double? = null
)

data class AttendanceExcuseV2Request(
    val isVerified: Boolean = false,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val excuseReason: String
)
