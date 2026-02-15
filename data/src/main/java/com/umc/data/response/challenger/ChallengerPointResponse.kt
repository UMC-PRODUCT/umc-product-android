package com.umc.data.response.challenger

import com.google.gson.annotations.SerializedName

data class ChallengerPointResponse(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("pointType") val pointType: String? = null,
    @SerializedName("point") val point: Double? = null,
    @SerializedName("description") val description: String? = null
)