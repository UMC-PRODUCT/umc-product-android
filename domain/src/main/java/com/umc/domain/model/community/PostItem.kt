package com.umc.domain.model.community

import com.umc.domain.model.enums.CommunityCategoryType
import com.umc.domain.model.enums.UserPart

data class PostItem (
    val postId: Long,                         // 게시글 고유 ID (추가 권장)
    val category: CommunityCategoryType,      // 카테고리 (Enum)
    val title: String,                        // 제목
    val content: String,                      // 본문
    val username: String = "어헛차",           // 작성자 (익명 처리 포함)
    val writeTime: String = "방금 전",         // 작성 시간 (파싱된 문자열)
    val likes: Int = 0,                       // 좋아요 수
    val comments: Int = 0,                    // 댓글 수
    val lightningInfo: LightningInfo? = null, // 번개 모임 추가 정보
    )