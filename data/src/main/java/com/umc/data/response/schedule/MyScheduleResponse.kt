package com.umc.data.response.schedule

import com.google.gson.annotations.SerializedName
import com.umc.domain.model.home.schedule.ScheduleMonthModel
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

data class MyScheduleItemResponse(
    @SerializedName("scheduleId") val scheduleId: Long,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String?,
    @SerializedName("tags") val tags: List<String>?,
    @SerializedName("authorMemberId") val authorMemberId: Long?,
    @SerializedName("startsAt") val startsAt: String, // "2026-06-06T21:00:00Z"
    @SerializedName("endsAt") val endsAt: String,     // "2026-08-20T03:00:00Z"
    @SerializedName("isOnline") val isOnline: Boolean?,
    @SerializedName("attendanceStatus") val attendanceStatus: String?,
    @SerializedName("isAttendanceChecked") val isAttendanceChecked: Boolean?,
    @SerializedName("isParticipant") val isParticipant: Boolean?
) {
    companion object {
        // ISO-8601 UTC 문자열을 로컬 날짜/시간으로 안전하게 파싱하는 확장 함수
        fun MyScheduleItemResponse.toDomain(): ScheduleMonthModel {
            val startZdt = ZonedDateTime.parse(startsAt)
            val endZdt = ZonedDateTime.parse(endsAt)

            val dayFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd", Locale.KOREA)
            val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.KOREA)

            return ScheduleMonthModel(
                scheduleId = scheduleId,
                name = name,
                startDay = startZdt.format(dayFormatter), // "2026.06.06"
                startTime = startZdt.format(timeFormatter), // "21:00"
                endDay = endZdt.format(dayFormatter),     // "2026.08.20"
                endTime = endZdt.format(timeFormatter),   // "03:00"
                status = attendanceStatus ?: "PENDING",
                dDay = 0 // 필요시 UI 단에서 계산
            )
        }
    }
}
