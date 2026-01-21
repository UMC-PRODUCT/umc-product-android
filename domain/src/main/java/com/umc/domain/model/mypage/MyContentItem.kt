package com.umc.domain.model.mypage

import com.umc.domain.model.enums.MyContentType


data class MyContentItem (
    val category : String,
    val region : String,
    val status : MyContentType, //모집중 여부
    val title : String,
    val username : String,
    val writeTime: String,
    val likes : String,
    val comments : String,
    val isSoft : Boolean, //지식 관련이나 아니냐의 차이
)