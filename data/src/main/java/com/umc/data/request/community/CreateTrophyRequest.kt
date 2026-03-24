package com.umc.data.request.community

import com.google.gson.annotations.SerializedName

data class CreateTrophyRequest(
    //@SerializedName("challengerId") val challengerId: Long,
    @SerializedName("week") val week: Int,
    @SerializedName("title") val title: String,
    @SerializedName("content") val content: String,
    @SerializedName("url") val url: String
)
