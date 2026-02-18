package com.umc.data.response.community

import com.google.gson.annotations.SerializedName
import com.umc.data.response.community.LightningInfoResponse.Companion.toLightningInfoDomain
import com.umc.domain.model.UDomainFormat.parseDateTime
import com.umc.domain.model.community.ContentItem
import com.umc.domain.model.enums.CommunityCategoryType
import com.umc.domain.model.enums.UserPart

/**게시글 상세 조회 용도**/
data class PostDetailResponse(
    @SerializedName("postId") val postId: Long,                // 게시글 고유 ID
    @SerializedName("title") val title: String,                // 게시글 제목
    @SerializedName("content") val content: String,            // 게시글 본문
    @SerializedName("category") val category: String,          // 게시글 카테고리
    @SerializedName("authorId")val authorId: Long,             // 게시글 작성자 ID
    @SerializedName("authorName")val authorName: String,       // 게시글 작성자 이름
    @SerializedName("authorProfileImage") val authorProfileImage: String?, // 게시글 작성자 프로필 이미지
    @SerializedName("authorPart") val authorPart: String,           // 게시글 작성자 유저파트
    @SerializedName("lightningInfo") val lightningInfo: LightningInfoResponse?, // 번개 모임 상세 정보 (일반글은 null)
    @SerializedName("commentCount") val commentCount: Int,      // 댓글 수
    @SerializedName("writeTime") val writeTime: String?,        // 게시글 생성 시간
    @SerializedName("likeCount") val likeCount: Int,            // 좋아요 수
    @SerializedName("isLiked") val isLiked: Boolean,            // 좋아요 여부
    @SerializedName("scrapCount") val scrapCount: Int,          // 스크랩 수
    @SerializedName("isScrapped") val isScrapped: Boolean,      // 스크랩 여부

) {
    companion object {
        fun PostDetailResponse.toContentItemDomain(): ContentItem {
            val formatTime = if (this.writeTime.isNullOrBlank()) {
                ""
            } else {
                val (writeDay, writeClock) = this.writeTime.parseDateTime()
                "${writeDay} ${writeClock}"
            }

            return ContentItem(

                postId = this.postId,
                title = this.title,
                category = try {
                    CommunityCategoryType.valueOf(this.category)
                } catch (e: Exception) {
                    CommunityCategoryType.FREE
                },
                username = this.authorName,
                userId = this.authorId,
                writeTime = formatTime,
                likes = this.likeCount,
                comments = this.commentCount,
                content = this.content,
                lightningInfo = this.lightningInfo?.toLightningInfoDomain(),
                userProfileImage = this.authorProfileImage ?: "",
                userPart = try {
                    UserPart.valueOf(this.authorPart.uppercase())
                } catch (e: Exception) {
                    UserPart.ANDROID
                },
                isLiked = this.isLiked,
                isScrapped = this.isScrapped,
                scraps = this.scrapCount
            )
        }
    }
}