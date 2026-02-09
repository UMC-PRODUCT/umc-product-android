package com.umc.data.response.community

import com.google.gson.annotations.SerializedName
import com.umc.domain.model.community.PostLike

data class PostLikeResponse(
    @SerializedName("liked") val liked: Boolean,
    @SerializedName("likeCount") val likeCount: Int,
) {
    companion object {
        fun PostLikeResponse.toPostLikeDomain(): PostLike = PostLike(
            liked = this.liked,
            likeCount = this.likeCount
        )
    }

}