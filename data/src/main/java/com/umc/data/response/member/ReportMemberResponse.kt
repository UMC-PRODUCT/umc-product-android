package com.umc.data.response.member

import com.google.gson.annotations.SerializedName

data class ReportMemberResponse(
    @field:SerializedName("reportId") val reportId: String,
    @field:SerializedName("memberId") val memberId: String,
    @field:SerializedName("reasons") val reasons: List<String>,
    @field:SerializedName("createdAt") val createdAt: String,
)
