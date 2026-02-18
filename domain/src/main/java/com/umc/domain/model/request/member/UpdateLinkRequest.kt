package com.umc.domain.model.request.member

data class UpdateLinkRequest(
    val links: List<LinkItem>
)

data class LinkItem(
    val type: String,
    val link: String
)
