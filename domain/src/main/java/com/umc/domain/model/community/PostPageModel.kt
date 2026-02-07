package com.umc.domain.model.community

data class PostPageModel(
    val posts: List<PostItem>, // 게시글 리스트
    val hasNext: Boolean,         // 다음 페이지 존재 여부
    val hasPrevious: Boolean,     // 이전 페이지 존재 여부
    val totalElements: Long,       // 전체 게시글 수
    val page: Int,                // 현재 페이지 번호
    val size: Int,                // 한 페이지당 포함된 게시글 개수
    val totalPages: Int,          // 전체 페이지 수

)