package com.umc.domain.model.enums

//커뮤니티에서의 카테고리 타입
enum class CommunityCategoryType(val label: String) {
    LIGHTNING("번개"),
    HOBBY("취미"),
    QUESTION("질문"),
    INFO("정보"),
    SUGGESTION("건의"),
    FREE("자유"),
}