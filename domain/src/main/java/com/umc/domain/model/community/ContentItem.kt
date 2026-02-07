package com.umc.domain.model.community

import com.umc.domain.model.enums.CommunityCategoryType
import com.umc.domain.model.enums.ContentType
import com.umc.domain.model.enums.RecruitType
import com.umc.domain.model.enums.UserPart

/**Mypage Tap에서도 사용!**/
data class ContentItem (
    val postId: Long = -1L,
    val title : String,                     //제목
    val category : CommunityCategoryType,   //카테고리 타입
    val username : String = "",             //작성자 (API는 X)
    val writeTime: String = "",             //작성 시간 (API는 X)
    val likes : Int = 0,                    //좋아요 수 (API는 X)
    val comments : Int = 0,                 //댓글 수 (API는 X)

    //아래는 본문 내용
    val content : String,                   //내용
    val lightningInfo: LightningInfo? = null, // 번개 내용
    val userPart : UserPart,                //작성자 유저파트 (API는 X)
    val isLiked: Boolean = false,           // 좋아요 클릭 여부 (다른 API로)
    val isScrapped: Boolean = false,        // 스크랩 클릭 여부 (다른 API로)
    val scraps : Int = 0,                   // 스크랩 수
)

/**
 *
 * data class PostItemModel (
 *     val postId: Long,                         // 게시글 고유 ID (추가 권장)
 *     val category: CommunityCategoryType,      // 카테고리 (Enum)
 *     val title: String,                        // 제목
 *     val content: String,                      // 본문
 *     val username: String = "어헛차",           // 작성자 (익명 처리 포함)
 *     val writeTime: String = "방금 전",         // 작성 시간 (파싱된 문자열)
 *     val likes: Int = 0,                       // 좋아요 수
 *     val comments: Int = 0,                    // 댓글 수
 *     val lightningInfo: LightningInfo? = null, // 번개 모임 추가 정보
 *     )
 *
 * **/