package com.umc.domain.model.mypage


import com.umc.domain.model.enums.CategoryType
import com.umc.domain.model.enums.ContentType
import com.umc.domain.model.enums.RecruitType

/**Mypage Tap에서도 사용!**/
data class ContentItem (
    val category : CategoryType, //카테고리 타입
    val region : String,
    val contentType : ContentType, //전체, 질문, 명예의 전당
    val recruitType : RecruitType, //모집중 여부
    val title : String,
    val username : String,
    val writeTime: String,
    val likes : String,
    val comments : String,
)