package com.umc.domain.model.request.member

import com.umc.domain.model.mypage.BusinessCardContact
import com.umc.domain.model.mypage.BusinessCardLink

/**
 * 명함(연락처·링크·공개 여부) 수정 요청
 *
 * 링크는 전체 교체라 빠진 타입은 서버에서 지워진다. 5개 타입을 모두 담아 보낸다
 *
 * @param profileImageId 새 프로필 사진 파일 ID. null 이면 기존 사진을 유지한다
 */
data class EditBusinessCardRequest(
    val contact: BusinessCardContact,
    val links: List<BusinessCardLink>,
    val profileImageId: String? = null,
)
