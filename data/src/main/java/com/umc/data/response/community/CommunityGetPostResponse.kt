package com.umc.data.response.community

import com.google.gson.annotations.SerializedName
import com.umc.data.response.community.PostDetailResponse.Companion.toContentItemDomain
import com.umc.data.response.community.PostSummaryResponse.Companion.toContentItemDomain
import com.umc.domain.model.community.ContentItem
import com.umc.domain.model.community.LightningInfo
import com.umc.domain.model.community.PostPageModel
import com.umc.domain.model.enums.CommunityCategoryType
import com.umc.domain.model.enums.UserPart

data class CommunityGetPostResponse (
    @SerializedName("content") val content: List<PostSummaryResponse>, // 게시글 데이터 리스트
    @SerializedName("page") val page: Int,                              // 현재 페이지 번호
    @SerializedName("size") val size: Int,                              // 한 페이지당 포함된 게시글 개수
    @SerializedName("totalElements") val totalElements: Long,           // 서버에 저장된 전체 게시글 총 개수
    @SerializedName("totalPages") val totalPages: Int,                  // 전체 페이지 수
    @SerializedName("hasNext") val hasNext: Boolean,                    // 다음 페이지 존재 여부
    @SerializedName("hasPrevious") val hasPrevious: Boolean             // 이전 페이지 존재 여부
    ) {
    companion object {
        /**
         * 커뮤니티 페이지 들어갈 시 전체 페이지 응답 DTO를 도메인 모델(PostPageModel)로 변환
         */
        fun CommunityGetPostResponse.toPostPageModelDomain(): PostPageModel = PostPageModel(
            posts = this.content.map { it.toContentItemDomain() }, // 리스트 내부 아이템들을 각각 변환
            hasNext = this.hasNext,
            hasPrevious = this.hasPrevious,
            page = this.page,
            size = this.size,
            totalPages = this.totalPages,
            totalElements = this.totalElements
        )

    }

}