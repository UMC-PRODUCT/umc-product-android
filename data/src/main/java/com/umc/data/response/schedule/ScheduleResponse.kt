package com.umc.data.response.schedule

import com.google.gson.annotations.SerializedName
import com.umc.domain.model.enums.CategoryType
import com.umc.domain.model.home.schedule.ScheduleDetailModel
import com.umc.domain.model.home.schedule.ScheduleListModel
import com.umc.domain.model.home.schedule.ScheduleMonthModel

//일정 목록 조회
data class ScheduleListResponse (
    @SerializedName("scheduleId") val scheduleId: String,
    @SerializedName("name") val name: String,
    @SerializedName("status") val status: String,
    @SerializedName("date") val date: String,
    @SerializedName("startTime") val startTime: String,
    @SerializedName("endTime") val endTime: String,
    @SerializedName("locationName") val locationName: String,
    @SerializedName("totalCount") val totalCount: String,
    @SerializedName("presentCount") val presentCount: String,
    @SerializedName("pendingCount") val pendingCount: String,
    @SerializedName("attendanceRate") val attendanceRate: String
) {
    companion object {
        fun ScheduleListResponse.toDomain(): ScheduleListModel {
            // "2026.02.05 (Thu)" -> ["2026.02.05", "(Thu)"] 분리
            val dateParts = date.split(" ")
            val pureDate = dateParts.getOrNull(0) ?: ""

            // "(Thu)" -> "THU" 변환
            val pureDayOfWeek = dateParts.getOrNull(1)
                ?.replace("(", "")
                ?.replace(")", "")
                ?.uppercase() ?: ""

            return ScheduleListModel(
                scheduleId = scheduleId.toIntOrNull() ?: 0,
                name = name,
                status = status,
                date = pureDate,
                dayOfWeek = pureDayOfWeek,
                startTime = startTime,
                endTime = endTime,
                locationName = locationName,
                attendanceRate = attendanceRate
            )
        }
    }
}

//일정 상세 조회
data class ScheduleDetailResponse(
    @SerializedName("scheduleId") val scheduleId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String?,
    @SerializedName("tags") val tags: List<CategoryType>?,
    @SerializedName("startsAt") val startsAt: String,
    @SerializedName("endsAt") val endsAt: String,
    @SerializedName("isAllDay") val isAllDay: Boolean,
    @SerializedName("locationName") val locationName: String?,
    @SerializedName("latitude") val latitude: Double?,
    @SerializedName("longitude") val longitude: Double?,
    @SerializedName("status") val status: String?,
    @SerializedName("dDay") val dDay: Int?,
    @SerializedName("requiresAttendanceApproval") val requiresAttendanceApproval: Boolean?
) {
    companion object {
        fun ScheduleDetailResponse.toHomeDomain(): ScheduleDetailModel {
            // "T"를 기준으로 날짜와 시간을 분리
            fun String.parseDateTime(): Pair<String, String> {
                val dateTimeParts = this.split("T")
                val date = dateTimeParts.getOrNull(0)?.replace("-", ".") ?: ""
                val time = dateTimeParts.getOrNull(1)?.substring(0, 5) ?: ""
                return Pair(date, time)
            }

            val (startDay, startTime) = startsAt.parseDateTime()
            val (endDay, endTime) = endsAt.parseDateTime()

            return ScheduleDetailModel(
                scheduleId = scheduleId,
                name = name,
                description = description ?: "",
                tags = tags ?: emptyList(),
                startDay = startDay,
                startTime = startTime,
                endDay = endDay,
                endTime = endTime,
                isAllDay = isAllDay,
                locationName = locationName ?: "",
                latitude = latitude?: 0.0,
                longitude = longitude?: 0.0,
                status = status ?: "",
                dDay = dDay ?: -1,
                requiresAttendanceApproval = requiresAttendanceApproval ?: false
            )
        }
    }
}

//월별 일정 조회
data class ScheduleMonthResponse(
    @SerializedName("scheduleId") val scheduleId: String,
    @SerializedName("name") val name: String,
    @SerializedName("startsAt") val startsAt: String,
    @SerializedName("endsAt") val endsAt: String,
    @SerializedName("status") val status: String,
    @SerializedName("dDay") val dDay: String
) {
    companion object {
        fun ScheduleMonthResponse.toDomain(): ScheduleMonthModel {
            // "T"를 기준으로 날짜와 시간을 분리
            fun String.parseDateTime(): Pair<String, String> {
                val dateTimeParts = this.split("T")
                val date = dateTimeParts.getOrNull(0)?.replace("-", ".") ?: ""
                val time = dateTimeParts.getOrNull(1)?.substring(0, 5) ?: ""
                return Pair(date, time)
            }

            val (startDay, startTime) = startsAt.parseDateTime()
            val (endDay, endTime) = endsAt.parseDateTime()


            return ScheduleMonthModel(
                scheduleId = scheduleId.toIntOrNull() ?: 0,
                name = name,
                startDay = startDay,
                startTime = startTime,
                endDay = endDay,
                endTime = endTime,
                status = status,
                dDay = dDay
            )
        }
    }
}