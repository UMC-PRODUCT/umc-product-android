package com.umc.data.response.community

import com.google.gson.annotations.SerializedName
import com.umc.data.response.community.LightningInfoResponse.Companion.toLightningInfoDomain
import com.umc.domain.model.UDomainFormat.parseDateTime
import com.umc.domain.model.community.ContentItem
import com.umc.domain.model.enums.CommunityCategoryType
import com.umc.domain.model.enums.UserPart

data class PostSearchSummaryResponse (
    @SerializedName("postId") val postId: Long,
    @SerializedName("title") val title: String,
    @SerializedName("contentPreview") val contentPreview: String,
    @SerializedName("category") val category: String,
    @SerializedName("likeCount") val likeCount: Int,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("matchType") val matchType: String,
    ) {
    companion object{
        fun PostSearchSummaryResponse.toContentItemDomain(): ContentItem {

            val (writeDay, writeTime) = this.createdAt.parseDateTime()

            return ContentItem(
                postId = this.postId,
                title = this.title,
                category = try {
                    CommunityCategoryType.valueOf(this.category)
                } catch (e: Exception) {
                    CommunityCategoryType.FREE
                },
                // 일부는 직접 (추후 서버 요청)
                username = "사용자",
                writeTime = "${writeDay}-${writeTime}", // TODO: 서버 응답에 생성일자가 추가되면 파싱 로직 적용
                likes = likeCount,           // API 미제공 (X)
                comments = 0,        // API 미제공 (X)
                content = this.contentPreview,
                lightningInfo = null,
                userPart = UserPart.ANDROID, // API 미제공 (X)
                isLiked = false,             // 다른 API로 처리 예정 (X)
                isScrapped = false,          // 다른 API로 처리 예정 (X)
                scraps = 0                   // API 미제공 (X)
            )
        }
    }
}

