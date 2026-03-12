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
    @SerializedName("content") val content: String,
    @SerializedName("category") val category: String,
    @SerializedName("authorChallengerId") val authorChallengerId: Long?,             // 게시글 작성자 ID
    @SerializedName("authorMemberId") val authorMemberId: Long?,             // 게시글 작성자 ID
    @SerializedName("authorName") val authorName: String?,       // 게시글 작성자 이름
    @SerializedName("authorNickname") val authorNickName: String?,
    @SerializedName("likeCount") val likeCount: Int,
    @SerializedName("commentCount") val commentCount: Int,
    @SerializedName("authorPart") val authorPart: String,
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
                username = this.authorName ?: "",
                userNickName = this.authorNickName ?: "",
                userId = this.authorChallengerId ?: -1L,
                userMemberId = this.authorMemberId ?: -1L,
                writeTime = "${writeDay} ${writeTime}", // TODO: 서버 응답에 생성일자가 추가되면 파싱 로직 적용
                likes = likeCount,
                comments = commentCount,
                content = this.content,
                lightningInfo = null,
                userPart = try {
                    UserPart.valueOf(this.category) }
                catch (e: Exception) { UserPart.ANDROID},
                isLiked = false,             // 다른 API로 처리 예정 (X)
                isScrapped = false,          // 다른 API로 처리 예정 (X)
                scraps = 0                   // API 미제공 (X)
            )
        }
    }
}

