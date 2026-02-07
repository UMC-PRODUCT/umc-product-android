package com.umc.data.response.schedule

import com.google.gson.annotations.SerializedName
import com.umc.domain.model.UDomainFormat.parseDateTime
import com.umc.domain.model.act.check.UserCheckAvailable
import com.umc.domain.model.enums.CategoryType
import com.umc.domain.model.enums.CheckAvailableStatus
import com.umc.domain.model.home.PlanDetailItem

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
        fun ScheduleDetailResponse.toModel(): UserCheckAvailable {
            return UserCheckAvailable(
                id = scheduleId,
                title = name,
                tags = tags,
                startTime = startsAt,
                endTime = endsAt,
                status = CheckAvailableStatus.BEFORE,
                latitude = latitude ?: 0.0,
                longitude = longitude ?: 0.0,
                address = locationName ?: "",
                isLocationCertified = null
            )
        }

        fun ScheduleDetailResponse.toPlanDetailDomain(): PlanDetailItem {
            // "T"를 기준으로 날짜와 시간을 분리 (UDomainUtil에 정의)
            val (startDay, startTime) = startsAt.parseDateTime()
            val (endDay, endTime) = endsAt.parseDateTime()

            return PlanDetailItem(
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