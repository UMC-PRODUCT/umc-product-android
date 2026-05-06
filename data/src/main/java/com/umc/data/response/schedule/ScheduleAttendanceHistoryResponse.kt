package com.umc.data.response.schedule

import com.google.gson.annotations.SerializedName
import com.umc.domain.model.UDomainFormat.parseDateTime
import com.umc.domain.model.act.check.AdminPendingUser
import com.umc.domain.model.act.check.AdminSessionCheck
import com.umc.domain.model.act.check.UserCheckHistory
import com.umc.domain.model.enums.AdminSessionStatus
import com.umc.domain.model.enums.CategoryType
import com.umc.domain.model.enums.CheckHistoryStatus

data class ScheduleAttendanceHistoryResponse(
    @SerializedName("scheduleId") val scheduleId: Long,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String?,
    @SerializedName("tags") val tags: List<String>?,
    @SerializedName("authorMemberId") val authorMemberId: Long,
    @SerializedName("startsAt") val startsAt: String,
    @SerializedName("endsAt") val endsAt: String,
    @SerializedName("isOnline") val isOnline: Boolean,
    @SerializedName("location") val location: LocationResponse?,
    @SerializedName("attendancePolicy") val attendancePolicy: AttendancePolicyResponse?,
    @SerializedName("participants") val participants: List<ParticipantResponse>?
) {
    data class LocationResponse(
        @SerializedName("latitude") val latitude: Double,
        @SerializedName("longitude") val longitude: Double,
        @SerializedName("locationName") val locationName: String?
    )

    data class AttendancePolicyResponse(
        @SerializedName("checkInStartAt") val checkInStartAt: String,
        @SerializedName("onTimeEndAt") val onTimeEndAt: String,
        @SerializedName("lateEndAt") val lateEndAt: String
    )

    data class ParticipantResponse(
        @SerializedName("memberId") val memberId: Long,
        @SerializedName("name") val name: String,
        @SerializedName("nickname") val nickname: String,
        @SerializedName("schoolId") val schoolId: Long,
        @SerializedName("schoolName") val schoolName: String,
        @SerializedName("profileImageUrl") val profileImageUrl: String?,
        @SerializedName("attendanceStatus") val attendanceStatus: String?,
        @SerializedName("isLocationVerified") val isLocationVerified: Boolean,
        @SerializedName("excuseReason") val excuseReason: String?
    )

    companion object {
        fun ScheduleAttendanceHistoryResponse.toUserCheckHistory(): UserCheckHistory {
            val myStatus = participants
                ?.mapNotNull { it.attendanceStatus }
                ?.firstOrNull()
            return UserCheckHistory(
                id = scheduleId,
                title = name,
                startTime = startsAt,
                endTime = endsAt,
                status = CheckHistoryStatus.fromServerValue(myStatus),
                tags = tags?.mapNotNull { runCatching { CategoryType.valueOf(it) }.getOrNull() }
            )
        }

        fun ScheduleAttendanceHistoryResponse.toAdminDomain(): AdminSessionCheck {
            val (date, startTime) = startsAt.parseDateTime()
            val (_, endTime) = endsAt.parseDateTime()

            val pendingStatuses = setOf("PRESENT_PENDING", "LATE_PENDING", "EXCUSED_PENDING")
            val doneStatuses = setOf("PRESENT", "LATE", "EXCUSED")
            val pendingParticipants = participants?.filter { it.attendanceStatus in pendingStatuses } ?: emptyList()
            val attendedCount = participants?.count { it.attendanceStatus in doneStatuses } ?: 0
            val totalCount = participants?.size ?: 0
            val rate = if (totalCount > 0) attendedCount * 100 / totalCount else 0

            return AdminSessionCheck(
                id = scheduleId,
                title = name,
                date = date,
                startTime = startTime,
                endTime = endTime,
                status = AdminSessionStatus.fromScheduleTimes(startsAt, endsAt),
                attendanceRate = rate,
                totalChallengers = totalCount,
                attendedChallengers = attendedCount,
                pendingCount = pendingParticipants.size,
                pendingUsers = pendingParticipants.map { p ->
                    AdminPendingUser(
                        id = p.memberId,
                        memberId = p.memberId,
                        name = p.name,
                        nickname = p.nickname,
                        university = p.schoolName,
                        profileImageUrl = p.profileImageUrl,
                        requestTime = "",
                        hasLateReason = p.excuseReason != null,
                        lateReason = p.excuseReason
                    )
                },
                sheetId = scheduleId
            )
        }
    }
}
