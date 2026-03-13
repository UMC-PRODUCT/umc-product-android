package com.umc.data.request.community

import com.google.gson.annotations.SerializedName

data class CreateCommentRequest(
    @SerializedName("content") val content: String,
    @SerializedName("parentId") val parentId: Long // 대댓글일 경우 부모 ID, 일반 댓글은 0
)