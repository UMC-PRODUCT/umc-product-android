package com.umc.data.response.attendance

import com.google.gson.annotations.SerializedName
import com.umc.domain.model.act.check.AdminPendingUser

data class AdminPendingUserResponse(
    @SerializedName("attendanceId") val attendanceId: String,
    @SerializedName("memberId") val memberId: String,
    @SerializedName("memberName") val memberName: String,
    @SerializedName("nickname") val nickname: String,
    @SerializedName("schoolName") val schoolName: String,
    @SerializedName("status") val status: String,
    @SerializedName("reason") val reason: String?,
    @SerializedName("requestedAt") val requestedAt: String
) {
    companion object {
        fun AdminPendingUserResponse.toAdminPendingUser(): AdminPendingUser {
            return AdminPendingUser(
                id = attendanceId.toIntOrNull() ?: 0,
                name = memberName,
                nickname = nickname,
                university = schoolName,
                requestTime = requestedAt.substringAfter("T", "").let {
                    if (it.length >= 5) it.substring(0, 5) else it
                },
                hasLateReason = !reason.isNullOrBlank(),
                lateReason = reason
            )
        }
    }
}