package com.umc.domain.model.mypage

import com.umc.domain.model.enums.MyContentType

data class MyContentItem (
    val category : String,
    val region : String,
    val status : MyContentType,
    val title : String,
    val username : String,
    val writeTime: String,
    var likes : String,
    var comments : String
)