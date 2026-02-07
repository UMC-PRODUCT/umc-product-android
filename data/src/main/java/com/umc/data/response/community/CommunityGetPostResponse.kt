package com.umc.data.response.community

import com.google.gson.annotations.SerializedName
import com.umc.domain.model.community.LightningInfo
import com.umc.domain.model.community.PostItem
import com.umc.domain.model.community.PostPageModel
import com.umc.domain.model.enums.CommunityCategoryType

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
         * 전체 페이지 응답 DTO를 도메인 모델(PostPageModel)로 변환
         */
        fun CommunityGetPostResponse.toCommunityDomain(): PostPageModel = PostPageModel(
            posts = this.content.map { it.toPostItemDomain() }, // 리스트 내부 아이템들을 각각 변환
            hasNext = this.hasNext,
            hasPrevious = this.hasPrevious,
            page = this.page,
            size = this.size,
            totalPages = this.totalPages,
            totalElements = this.totalElements
        )

        /**
         * 개별 게시글 DTO를 도메인 모델(PostItem)로 변환
         */
        private fun PostSummaryResponse.toPostItemDomain(): PostItem = PostItem(
            postId = this.postId,
            // 서버의 String 카테고리를 도메인 Enum으로 변환
            category = try {
                CommunityCategoryType.valueOf(this.category)
            } catch (e: Exception) {
                CommunityCategoryType.FREE
            },
            title = this.title,
            content = this.content,
            // 익명 여부에 따른 이름 처리
            writeTime = "방금 전", // TODO: 서버에서 작성 시간이 오면 변환
            lightningInfo = this.lightningInfo?.toLightningDomain(),
            // 나머지 기본값 (필요 시 서버 응답 추가 매핑)
            likes = 0,
            comments = 0
        )

        /**
         * 번개 정보 DTO를 도메인 모델(LightningInfo)로 변환
         */
        private fun LightningInfoResponse.toLightningDomain(): LightningInfo = LightningInfo(
            meetAt = this.meetAt,
            location = this.location,
            maxParticipants = this.maxParticipants
        )
    }

}