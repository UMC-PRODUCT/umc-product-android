package com.umc.data.request.member

import com.google.gson.annotations.SerializedName

/**
 * 명함 수정 요청 (PATCH /api/v1/member/me/business-card)
 *
 * 빈 연락처·링크는 null 로 보내 서버에서 지우게 한다. profileImageId 가 null 이면 기존 사진을 유지한다
 */
data class EditBusinessCardRequest(
    @SerializedName("profileImageId") val profileImageId: String?,
    @SerializedName("contact") val contact: BusinessCardContactRequest,
    @SerializedName("links") val links: List<BusinessCardLinkRequest>,
)

data class BusinessCardContactRequest(
    @SerializedName("email") val email: String?,
    @SerializedName("isPublic") val isPublic: Boolean,
)

data class BusinessCardLinkRequest(
    @SerializedName("type") val type: String,
    @SerializedName("link") val link: String?,
    @SerializedName("isPublic") val isPublic: Boolean,
)
