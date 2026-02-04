package com.umc.domain.model.home

import com.umc.domain.model.enums.UserPart
import java.util.UUID

data class ParticipantItem(
    val name : String = "",
    val userPart : UserPart = UserPart.ANDROID,
    val school : String = "학교",
    val id: String = UUID.randomUUID().toString()
)

sealed class SearchResultItem {
    data class Header(val partName: String) : SearchResultItem()
    data class Participant(val user: ParticipantItem) : SearchResultItem()
}

