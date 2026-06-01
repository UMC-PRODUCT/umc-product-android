package com.umc.data.request.challenger

import com.google.gson.annotations.SerializedName

data class AddChallengerPointRequest(
    @SerializedName("pointType")
    val pointType: String,
    @SerializedName("pointValue")
    val pointValue: Int,
    @SerializedName("description")
    val description: String
)
