package com.umc.data.response.community

import com.google.gson.annotations.SerializedName
import com.umc.data.response.community.CommunityGetPostResponse.Companion.toLightningDomain
import com.umc.domain.model.community.ContentItem
import com.umc.domain.model.enums.CommunityCategoryType
import com.umc.domain.model.enums.UserPart

data class PostDetailResponse(
    @SerializedName("postId") val postId: Long,
    @SerializedName("title") val title: String,
    @SerializedName("content") val content: String,
    @SerializedName("category") val category: String,
    @SerializedName("region") val region: String?,
    @SerializedName("anonymous") val anonymous: Boolean,
    @SerializedName("lightningInfo") val lightningInfo: LightningInfoResponse?
) {
    companion object{
        fun PostDetailResponse.toCommunityDomain(): ContentItem = ContentItem(
            postId = this.postId,
            title = this.title,
            category = try { CommunityCategoryType.valueOf(this.category) }
            catch (e: Exception) { CommunityCategoryType.FREE },
            content = this.content,
            username = "사용자",
            lightningInfo = this.lightningInfo?.toLightningDomain(),
            // 상세 조회 시 추가로 필요한 필드들 기본값 채우기
            writeTime = "방금 전",
            userPart = UserPart.ANDROID
        )
    }
}