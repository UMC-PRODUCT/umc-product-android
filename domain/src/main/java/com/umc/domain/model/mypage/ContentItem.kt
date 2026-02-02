package com.umc.domain.model.mypage


import com.umc.domain.model.enums.CategoryType
import com.umc.domain.model.enums.CommunityCategoryType
import com.umc.domain.model.enums.ContentType
import com.umc.domain.model.enums.RecruitType
import com.umc.domain.model.enums.UserPart

/**Mypage Tap에서도 사용!**/
data class ContentItem (
    val category : CommunityCategoryType, //카테고리 타입
    val region : String = "",            /**해당 파트 역시 제거하기로 결정 -- 추후 API 연결 시 삭제 예정**/
    val contentType : ContentType, //전체, 질문, 명예의 전당
    val recruitType : RecruitType, /**일단 해당 파트는 제거하기로 결정 -- 추후 API 연결 시 삭제 예정**/
    val title : String,
    val username : String,
    val writeTime: String,
    val likes : Int,
    val comments : Int,

    //아래는 본문 내용
    val content : String,
    val userPart : UserPart,
    val isLiked: Boolean = false,   // 좋아요 클릭 여부
    val isScrapped: Boolean = false, // 스크랩 클릭 여부
    val scraps : Int = 0,
    //이미지는?
)