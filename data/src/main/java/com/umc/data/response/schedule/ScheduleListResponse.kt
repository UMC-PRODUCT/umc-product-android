package com.umc.data.response.schedule

import com.google.gson.annotations.SerializedName
import com.umc.domain.model.act.check.AdminPendingUser
import com.umc.domain.model.act.check.AdminSessionCheck
import com.umc.domain.model.enums.AdminSessionStatus
import com.umc.domain.model.home.schedule.ScheduleListModel


//일정 목록 조회
data class ScheduleListResponse (
    @SerializedName("scheduleId") val scheduleId: Long,
    @SerializedName("name") val name: String,
    @SerializedName("status") val status: String,
    @SerializedName("date") val date: String,
    @SerializedName("startTime") val startTime: String,
    @SerializedName("endTime") val endTime: String,
    @SerializedName("locationName") val locationName: String,
    @SerializedName("sheetId") val sheetId: Long?,
    @SerializedName("totalCount") val totalCount: Int,
    @SerializedName("presentCount") val presentCount: Int,
    @SerializedName("pendingCount") val pendingCount: Int,
    @SerializedName("attendanceRate") val attendanceRate: Double,
    val pendingUsers: List<AdminPendingUser> = emptyList(),
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
                scheduleId = scheduleId,
                name = name,
                status = status,
                date = pureDate,
                dayOfWeek = pureDayOfWeek,
                startTime = startTime,
                endTime = endTime,
                locationName = locationName,
                totalCount = totalCount,
                presentCount = presentCount,
                pendingCount = pendingCount,
                attendanceRate = attendanceRate
            )
        }

        /** 서버는 출석률을 Double 로 내려주고 관리자 출석 화면은 정수로 보여 주므로, 소수점 아래를 버린다. **/
        fun ScheduleListResponse.toAdminDomain(): AdminSessionCheck {
            return AdminSessionCheck(
                id = scheduleId,
                title = name,
                date = date,
                startTime = startTime,
                endTime = endTime,
                status = AdminSessionStatus.fromServerValue(status),
                attendanceRate = attendanceRate.toInt(),
                totalChallengers = totalCount,
                attendedChallengers = presentCount,
                pendingCount = pendingCount,
                pendingUsers = pendingUsers,
                sheetId = sheetId
            )
        }
    }
}

