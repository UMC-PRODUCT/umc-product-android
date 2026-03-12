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
    val userId : Long = -1L,                //작성자 ID(challengerId)
    val userMemberId : Long = -1L,          //작성자 MEMBERID
    val username : String = "",             //작성자
    val userNickName : String = "",         //작성자 닉네임
    val userProfileImage: String = "",      //작싱자 아이콘
    val writeTime: String = "",             //작성 시간
    val likes : Int = 0,                    //좋아요 수
    val comments : Int = 0,                 //댓글 수

    //아래는 본문 내용
    val content : String,                   //내용
    val lightningInfo: LightningInfo? = null, // 번개 내용
    val userPart : UserPart,                //작성자 유저파트 (API는 X)
    val isLiked: Boolean = false,           // 좋아요 클릭 여부 (다른 API로)
    val isScrapped: Boolean = false,        // 스크랩 클릭 여부 (다른 API로)
    val scraps : Int = 0,                   // 스크랩 수
)

data class PostLike(
    val liked : Boolean,
    val likeCount : Int
)

data class PostScrap(
    val scrapped : Boolean,
    val scrapCount : Int
)