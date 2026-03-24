package com.umc.domain.model.community

import com.umc.domain.model.enums.UserPart

sealed class TrophyItem {
    data class Header(val schoolName: String) : TrophyItem()
    data class Content(val data: TrophyBody) : TrophyItem()
}

// 서버 응답
data class TrophyBody(
    val trophyId: Long,
    val challengerId: Long,
    val week: Int,
    val challengerName: String,
    val challengerProfileImage: String,
    val school: String,
    val part: UserPart,
    val title: String,
    val content: String,
    val url: String
)

data class TrophyWrite(
    //val challengerId: Long,
    val week: Int,
    val title: String,
    val content: String,
    val url: String
)