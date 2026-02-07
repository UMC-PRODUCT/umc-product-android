package com.umc.data.response.schedule

import com.google.gson.annotations.SerializedName
import com.umc.domain.model.act.check.AdminSessionCheck
import com.umc.domain.model.enums.AdminSessionStatus
import com.umc.domain.model.home.schedule.ScheduleListModel


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

        fun ScheduleListResponse.toAdminDomain(): AdminSessionCheck {
            return AdminSessionCheck(
                id = scheduleId.toIntOrNull() ?: 0,
                title = name,
                date = date,
                startTime = startTime,
                endTime = endTime,
                status = AdminSessionStatus.fromServerValue(status),
                attendanceRate = attendanceRate.replace("%", "").toDoubleOrNull()?.toInt() ?: 0,
                totalChallengers = totalCount.toIntOrNull() ?: 0,
                attendedChallengers = presentCount.toIntOrNull() ?: 0,
                pendingCount = pendingCount.toIntOrNull() ?: 0,
                pendingUsers = emptyList()
            )
        }
    }
}

