package com.umc.data.response.schedule

import com.google.gson.annotations.SerializedName
import com.umc.domain.model.UDomainFormat.isAllDayInKst
import com.umc.domain.model.UDomainFormat.parseDateTime
import com.umc.domain.model.act.check.AdminSessionCheck
import com.umc.domain.model.act.check.UserCheckAvailable
import com.umc.domain.model.enums.AdminSessionStatus
import com.umc.domain.model.enums.CategoryType
import com.umc.domain.model.enums.CheckAvailableStatus
import com.umc.domain.model.home.PlanDetailItem
import com.umc.domain.model.home.schedule.ScheduleMonthModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

data class ScheduleMeResponse(
    @SerializedName("scheduleId") val scheduleId: Long,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String?,
    @SerializedName("tags") val tags: List<String>?,
    @SerializedName("authorMemberId") val authorMemberId: Long,
    @SerializedName("startsAt") val startsAt: String,
    @SerializedName("endsAt") val endsAt: String,
    @SerializedName("isOnline") val isOnline: Boolean,
    @SerializedName("location") val location: LocationResponse?,
    @SerializedName("attendanceStatus") val attendanceStatus: String?,
    @SerializedName("isAttendanceChecked") val isAttendanceChecked: Boolean,
    @SerializedName("attendancePolicy") val attendancePolicy: AttendancePolicyResponse?,
    @SerializedName("isParticipant") val isParticipant: Boolean,
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
        @SerializedName("profileImageUrl") val profileImageUrl: String?
    )

    companion object {
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd")

        fun ScheduleMeResponse.toMonthDomain(): ScheduleMonthModel {
            val (startDay, startTime) = startsAt.parseDateTime()
            val (endDay, endTime) = endsAt.parseDateTime()

            val today = LocalDate.now()
            val startDate = runCatching {
                LocalDate.parse(startDay, DATE_FORMATTER)
            }.getOrDefault(today)
            val dDay = ChronoUnit.DAYS.between(today, startDate).toInt()

            return ScheduleMonthModel(
                scheduleId = scheduleId,
                name = name,
                startDay = startDay,
                startTime = startTime,
                endDay = endDay,
                endTime = endTime,
                status = attendanceStatus ?: "",
                dDay = dDay
            )
        }

        fun ScheduleMeResponse.toPlanDetailDomain(): PlanDetailItem {
            val (startDay, startTime) = startsAt.parseDateTime()
            val (endDay, endTime) = endsAt.parseDateTime()

            val today = LocalDate.now()
            val startDate = runCatching {
                LocalDate.parse(startDay, DATE_FORMATTER)
            }.getOrDefault(today)
            val dDay = ChronoUnit.DAYS.between(today, startDate).toInt()

            return PlanDetailItem(
                scheduleId = scheduleId,
                name = name,
                description = description ?: "",
                tags = tags?.mapNotNull { runCatching { CategoryType.valueOf(it) }.getOrNull() } ?: emptyList(),
                startDay = startDay,
                startTime = startTime,
                endDay = endDay,
                endTime = endTime,
                isAllDay = isAllDayInKst(startsAt, endsAt),
                locationName = location?.locationName ?: "",
                latitude = location?.latitude ?: 0.0,
                longitude = location?.longitude ?: 0.0,
                status = attendanceStatus ?: "",
                dDay = dDay,
                participantMemberIds = participants?.map { it.memberId } ?: emptyList(),
                requiresAttendanceApproval = false
            )
        }

        fun ScheduleMeResponse.toModel(): UserCheckAvailable {
            return UserCheckAvailable(
                id = scheduleId,
                title = name,
                tags = tags?.mapNotNull { runCatching { CategoryType.valueOf(it) }.getOrNull() },
                sheetId = scheduleId,
                startTime = startsAt,
                endTime = endsAt,
                status = CheckAvailableStatus.BEFORE,
                latitude = location?.latitude ?: 0.0,
                longitude = location?.longitude ?: 0.0,
                address = location?.locationName ?: "",
                isLocationCertified = null
            )
        }

        fun ScheduleMeResponse.toAdminDomain(): AdminSessionCheck {
            val (date, startTime) = startsAt.parseDateTime()
            val (_, endTime) = endsAt.parseDateTime()

            return AdminSessionCheck(
                id = scheduleId,
                title = name,
                date = date,
                startTime = startTime,
                endTime = endTime,
                status = AdminSessionStatus.fromScheduleTimes(startsAt, endsAt),
                attendanceRate = 0,
                totalChallengers = participants?.size ?: 0,
                attendedChallengers = 0,
                pendingCount = 0,
                pendingUsers = emptyList(),
                sheetId = scheduleId
            )
        }
    }
}
