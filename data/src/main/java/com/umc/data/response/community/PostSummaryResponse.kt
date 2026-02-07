package com.umc.data.response.community

import com.google.gson.annotations.SerializedName

/**게시글 상세 정보**/
data class PostSummaryResponse(
    @SerializedName("postId") val postId: Long,                // 게시글 고유 ID
    @SerializedName("title") val title: String,                // 게시글 제목
    @SerializedName("content") val content: String,            // 게시글 본문
    @SerializedName("category") val category: String,          // 게시글 카테고리
    @SerializedName("region") val region: String?,             // 작성 지역 (삭제 예정)
    @SerializedName("anonymous") val anonymous: Boolean,       // 익명 작성 여부 (삭제 예정)
    @SerializedName("lightningInfo") val lightningInfo: LightningInfoResponse? // 번개 모임 상세 정보 (일반글은 null)
)