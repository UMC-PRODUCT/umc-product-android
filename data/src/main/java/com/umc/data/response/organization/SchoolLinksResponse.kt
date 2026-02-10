package com.umc.data.response.organization

import kotlinx.serialization.Serializable

@Serializable
data class SchoolLinksResponse(
    val kakaoLink: String,
    val instagramLink: String,
    val youtubeLink: String
)