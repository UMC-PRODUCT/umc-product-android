package com.umc.data.request.member

import com.google.gson.annotations.SerializedName

data class ReportMemberRequest(
    @field:SerializedName("reasons") val reasons: List<String>,
    @field:SerializedName("detail") val detail: String,
)
