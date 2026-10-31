package com.umc.data.response.member

import com.google.gson.annotations.SerializedName
import com.umc.domain.model.enums.LinkType
import com.umc.domain.model.mypage.BusinessCard
import com.umc.domain.model.mypage.BusinessCardContact
import com.umc.domain.model.mypage.BusinessCardLink

/**
 * 명함 응답 (내 명함 조회, 명함 수정 화면 조회, 명함 수정 결과)
 *
 * 세 응답 모두 이름·학교·활동 등 다른 필드도 내려오지만, 앱은 연락처·링크·공개 여부만 이 응답에서 읽는다
 * (나머지는 기존 내 프로필 조회에서 가져온다)
 */
data class BusinessCardResponse(
    @SerializedName("contact") val contact: BusinessCardContactResponse?,
    @SerializedName("links") val links: List<BusinessCardLinkResponse>?,
) {
    fun toDomain(): BusinessCard = BusinessCard(
        contact = contact?.let { BusinessCardContact(email = it.email.orEmpty(), isPublic = it.isPublic ?: true) },
        // 앱이 모르는 링크 타입은 건너뛴다
        links = links.orEmpty().mapNotNull { link ->
            val type = LinkType.entries.firstOrNull { it.label == link.type } ?: return@mapNotNull null
            BusinessCardLink(type = type, link = link.link.orEmpty(), isPublic = link.isPublic ?: true)
        }
    )
}

data class BusinessCardContactResponse(
    @SerializedName("email") val email: String?,
    @SerializedName("isPublic") val isPublic: Boolean?,
)

data class BusinessCardLinkResponse(
    @SerializedName("type") val type: String?,
    @SerializedName("link") val link: String?,
    @SerializedName("isPublic") val isPublic: Boolean?,
)
