package com.umc.data.request.community

import com.google.gson.annotations.SerializedName

data class CreatePostLightningRequest(
    @SerializedName("title") val title: String,
    @SerializedName("content") val content: String,
    @SerializedName("region") val region: String,
    @SerializedName("anonymous") val anonymous: Boolean,
    @SerializedName("meetAt") val meetAt : String,
    @SerializedName("location") val location : String,
    @SerializedName("maxParticipants") val maxParticipants : Int

    )
