package com.example.mypage.profile

import com.umc.component.theme.AppStrings
import com.umc.domain.model.enums.LinkType

/**
 * 프로필 정보 편집 화면의 연락처·링크 입력 항목 (화면 위에서부터 순서대로)
 *
 * 항목마다 공개·비공개를 고를 수 있고, 명함 수정 API로 함께 저장한다
 *
 * @param title 항목 제목
 * @param placeholder 입력값이 없을 때 보여줄 예시
 * @param linkType 서버 링크 타입. 이메일은 링크가 아니라 연락처라 null (로그인 이메일과 별개인 선택 입력값)
 */
enum class ProfileLinkField(
    val title: String,
    val placeholder: String,
    val linkType: LinkType?,
) {
    EMAIL(AppStrings.PROFILE_LINK_EMAIL, AppStrings.PROFILE_LINK_EMAIL_PLACEHOLDER, linkType = null),
    GITHUB(AppStrings.PROFILE_LINK_GITHUB, AppStrings.PROFILE_LINK_GITHUB_PLACEHOLDER, LinkType.GITHUB),
    LINKEDIN(AppStrings.PROFILE_LINK_LINKEDIN, AppStrings.PROFILE_LINK_LINKEDIN_PLACEHOLDER, LinkType.LINKEDIN),
    BLOG(AppStrings.PROFILE_LINK_BLOG, AppStrings.PROFILE_LINK_BLOG_PLACEHOLDER, LinkType.BLOG),
    INSTAGRAM(AppStrings.PROFILE_LINK_INSTAGRAM, AppStrings.PROFILE_LINK_INSTAGRAM_PLACEHOLDER, LinkType.INSTAGRAM),
    PERSONAL(AppStrings.PROFILE_LINK_PERSONAL, AppStrings.PROFILE_LINK_PERSONAL_PLACEHOLDER, LinkType.PERSONAL),
}
