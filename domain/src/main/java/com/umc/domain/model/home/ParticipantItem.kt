package com.umc.domain.model.home

import com.umc.domain.model.enums.UserPart
import java.util.UUID
import kotlin.math.abs

data class ParticipantItem(
    val id: Long,
    val name: String,
    val nickname: String = "",
    val school: String = "",
    val gisu: Long = 0,
    val userPart: UserPart = UserPart.ANDROID,
    val profileImage: String = ""
)

//이는 유저 검색 후 response를 DTO에 맞춰 domain으로 가져오기 위한 용도
data class ParticipantSearchPage(
    val content: List<ParticipantItem>,
    val nextCursor: Long?,
    val hasNext: Boolean
)

//recyclerivew 용도 (userPart랑 ParticipantItem을 같이 보여주기)
sealed class SearchResultItem {
    data class Header(val partName: String) : SearchResultItem()
    data class Participant(val user: ParticipantItem) : SearchResultItem()
}

