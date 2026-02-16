package com.umc.data.response.base

import com.google.gson.annotations.SerializedName

data class CursorResponse<T>(
    @SerializedName("content")
    val content: List<T>,
    @SerializedName("nextCursor")
    val nextCursor: Long?,
    @SerializedName("hasNext")
    val hasNext: Boolean
)