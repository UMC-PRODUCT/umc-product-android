package com.umc.data.response.community

import com.google.gson.annotations.SerializedName
import com.umc.data.response.community.LightningInfoResponse.Companion.toLightningInfoDomain
import com.umc.domain.model.UDomainFormat.parseDateTime
import com.umc.domain.model.community.ContentItem
import com.umc.domain.model.enums.CommunityCategoryType
import com.umc.domain.model.enums.UserPart

/**게시글 목록 조회 용도 (축약)**/
data class PostSummaryResponse(
    @SerializedName("postId") val postId: Long,                // 게시글 고유 ID
    @SerializedName("title") val title: String,                // 게시글 제목
    @SerializedName("content") val content: String,            // 게시글 본문
    @SerializedName("category") val category: String,          // 게시글 카테고리
    @SerializedName("authorId")val authorId: Long,             // 게시글 작성자 ID
    @SerializedName("authorName")val authorName: String,       // 게시글 작성자 이름
    @SerializedName("authorProfileImage")val authorProfileImage: String?,       // 게시글 작성자 프로필 이미지
    @SerializedName("createdAt") val createdAt: String?,        // 게시글 생성 시간
    @SerializedName("commentCount") val commentCount: Int,      // 댓글 수
    @SerializedName("likeCount") val likeCount: Int,            // 좋아요 수
    @SerializedName("isLiked") val isLiked: Boolean,            // 좋아요 여부
    @SerializedName("lightningInfo") val lightningInfo: LightningInfoResponse? // 번개 모임 상세 정보 (일반글은 null)
) {
    companion object {
        fun PostSummaryResponse.toContentItemDomain(): ContentItem {

            val formatTime = if (this.createdAt.isNullOrBlank()) {
                ""
            } else {
                val (writeDay, writeClock) = this.createdAt.parseDateTime()
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
                userId = this.authorId,
                username = this.authorName,
                userProfileImage = this.authorProfileImage ?: "",
                writeTime = formatTime,
                likes = this.likeCount,
                comments = this.commentCount,
                content = this.content,
                lightningInfo = this.lightningInfo?.toLightningInfoDomain(),
                //아래 부분은 더미 채우기 위한 부분으로 체크(summary에는 사용하지 않음)
                userPart = UserPart.ANDROID,
                isLiked = false,
                isScrapped = false,
                )
        }
    }
}
