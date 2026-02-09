package com.umc.data.request.community

import com.google.gson.annotations.SerializedName

data class CreatePostRequest (
    @SerializedName("title") val title: String,
    @SerializedName("content") val content: String,
    @SerializedName("category") val category: String, //CommunityCategoty의 enum을 이용
    )