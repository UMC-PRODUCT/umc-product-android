package com.umc.domain.model.mypage

import com.umc.domain.model.enums.LinkType

/**
 * 명함에 싣는 연락처·링크와 항목별 공개 여부
 *
 * - 내 명함 조회: 다른 회원에게 보이는 그대로라 공개 항목만 담긴다
 * - 명함 수정 화면 조회·수정 결과: 비공개 항목도 공개 여부와 함께 담긴다
 *
 * @param contact 연락처 이메일. 로그인 이메일과 별개이고, 내 명함 조회에서 비공개이거나 비어 있으면 null
 * @param links 링크 타입별 항목
 */
data class BusinessCard(
    val contact: BusinessCardContact? = null,
    val links: List<BusinessCardLink> = emptyList(),
) {
    /** 해당 타입의 링크 (없으면 빈 값·공개) */
    fun linkOf(type: LinkType): BusinessCardLink =
        links.firstOrNull { it.type == type } ?: BusinessCardLink(type)
}

data class BusinessCardContact(
    val email: String = "",
    val isPublic: Boolean = true,
)

data class BusinessCardLink(
    val type: LinkType,
    val link: String = "",
    val isPublic: Boolean = true,
)
