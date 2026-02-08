package com.umc.data.response.community

import com.google.gson.annotations.SerializedName
import com.umc.data.response.community.LightningInfoResponse.Companion.toLightningInfoDomain
import com.umc.domain.model.community.ContentItem
import com.umc.domain.model.enums.CommunityCategoryType
import com.umc.domain.model.enums.UserPart

/**게시글 상세 정보**/
data class PostSummaryResponse(
    @SerializedName("postId") val postId: Long,                // 게시글 고유 ID
    @SerializedName("title") val title: String,                // 게시글 제목
    @SerializedName("content") val content: String,            // 게시글 본문
    @SerializedName("category") val category: String,          // 게시글 카테고리
    @SerializedName("region") val region: String?,             // 작성 지역 (삭제 예정)
    @SerializedName("anonymous") val anonymous: Boolean,       // 익명 작성 여부 (삭제 예정)
    @SerializedName("lightningInfo") val lightningInfo: LightningInfoResponse? // 번개 모임 상세 정보 (일반글은 null)
) {
    companion object{
        /**
         * 개별 게시글 DTO를 도메인 모델(PostItem)로 변환
         */
        fun PostSummaryResponse.toContentItemDomain(): ContentItem = ContentItem(
            postId = this.postId,
            title = this.title,
            category = try {
                CommunityCategoryType.valueOf(this.category)
            } catch (e: Exception) {
                CommunityCategoryType.FREE
            },
            // 일부는 직접 (추후 서버 요청)
            username = "사용자",
            writeTime = "방금 전", // TODO: 서버 응답에 생성일자가 추가되면 파싱 로직 적용
            likes = 0,           // API 미제공 (X)
            comments = 0,        // API 미제공 (X)
            content = this.content,
            lightningInfo = this.lightningInfo?.toLightningInfoDomain(),
            userPart = UserPart.ANDROID, // API 미제공 (X)
            isLiked = false,             // 다른 API로 처리 예정 (X)
            isScrapped = false,          // 다른 API로 처리 예정 (X)
            scraps = 0                   // API 미제공 (X)
        )
    }
}