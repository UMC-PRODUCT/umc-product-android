package com.umc.data.request.member

data class UpdateMyLinkRequest(
    val links: List<LinkItemRequest>
)

data class LinkItemRequest(
    val type: String,
    val link: String
)
