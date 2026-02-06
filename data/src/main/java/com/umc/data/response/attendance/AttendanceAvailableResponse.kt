package com.umc.data.response.attendance

import com.google.gson.annotations.SerializedName
import com.umc.domain.model.act.check.UserCheckAvailable
import com.umc.domain.model.enums.CategoryType
import com.umc.domain.model.enums.CheckAvailableStatus

data class AttendanceAvailableResponse(
    @SerializedName("scheduleId") val scheduleId: Int,
    @SerializedName("scheduleName") val scheduleName: String,
    @SerializedName("tags") val tags: List<CategoryType>?,
    @SerializedName("startTime") val startTime: String,
    @SerializedName("endTime") val endTime: String,
    @SerializedName("sheetId") val sheetId: Int,
    @SerializedName("recordId") val recordId: Int,
    @SerializedName("status") val status: CheckAvailableStatus,
    @SerializedName("statusDisplay") val statusDisplay: String?
) {
    companion object {
        fun AttendanceAvailableResponse.toModel(): UserCheckAvailable {
            return UserCheckAvailable(
                id = scheduleId,
                title = scheduleName,
                tags = tags,
                startTime = startTime,
                endTime = endTime,
                status = status,
                latitude = 0.0,
                longitude = 0.0,
                address = "",
                isLocationCertified = null
            )
        }
    }
}