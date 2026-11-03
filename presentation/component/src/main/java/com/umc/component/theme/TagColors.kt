package com.umc.component.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.umc.domain.model.enums.UserChallengerRole
import com.umc.domain.model.enums.UserPart

/**
 * 디자인 시스템 Tag 의 한 색 계열
 *
 * - 파트 칩(테두리 형태): 배경 [light] · 테두리 [base] · 글자 [strong]
 * - 역할 태그 · 공지 대상 태그: 배경 [base] · 글자 [strong]
 *
 * @param light 50 단계
 * @param base 100 단계
 * @param strong 600 단계
 */
@Immutable
data class UTagColors(
    val light: Color,
    val base: Color,
    val strong: Color,
)

/**
 * 11기부터 쓰는 네 파트의 색 계열 (Tag/Part: PM · Design · Web PE · Mobile PE)
 *
 * 지난 기수 파트(Web · Android · iOS · Node.js · Spring · Admin)와 미정은 시안에 계열이 없어 null 을 돌려준다
 */
@Composable
fun UserPart.tagColors(): UTagColors? = when (this) {
    UserPart.PLAN -> UTagColors(violet50(), violet100(), violet600())
    UserPart.DESIGN -> UTagColors(pink50(), pink100(), pink600())
    UserPart.WEB_PRODUCT_ENGINEER -> UTagColors(tagYellow50(), tagYellow100(), tagYellow600())
    UserPart.MOBILE_PRODUCT_ENGINEER -> UTagColors(cobalt50(), cobalt100(), cobalt600())
    else -> null
}

/**
 * 역할 태그 색 계열 (총괄 · 부총괄 · 중앙 운영진 = purple, 지부장 = brown, 교내 역할 = blue)
 *
 * Admin(시스템 관리자) · 챌린저는 시안에서 브랜드 계열이라 main 을 쓴다. main 에는 50 단계가 없어 [UTagColors.light] 도 main100 이다
 */
@Composable
fun UserChallengerRole.tagColors(): UTagColors = when (this) {
    UserChallengerRole.CENTRAL_PRESIDENT,
    UserChallengerRole.CENTRAL_VICE_PRESIDENT,
    UserChallengerRole.CENTRAL_OPERATING_TEAM_MEMBER,
    UserChallengerRole.CENTRAL_EDUCATION_TEAM_MEMBER -> UTagColors(purple50(), purple100(), purple600())

    UserChallengerRole.CHAPTER_PRESIDENT -> UTagColors(brown50(), brown100(), brown600())

    UserChallengerRole.SCHOOL_PRESIDENT,
    UserChallengerRole.SCHOOL_VICE_PRESIDENT,
    UserChallengerRole.SCHOOL_PART_LEADER,
    UserChallengerRole.SCHOOL_ETC_ADMIN -> UTagColors(blue50(), blue100(), blue600())

    UserChallengerRole.SUPER_ADMIN,
    UserChallengerRole.CHALLENGER,
    UserChallengerRole.MEMBER -> UTagColors(main100(), main100(), main600())
}
