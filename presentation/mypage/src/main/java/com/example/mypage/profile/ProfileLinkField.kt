package com.example.mypage.profile

import com.umc.component.theme.AppStrings

/**
 * 프로필 정보 편집 화면의 링크 입력 항목 (화면 위에서부터 순서대로)
 *
 * @param title 항목 제목
 * @param placeholder 입력값이 없을 때 보여줄 예시
 * @param editable false 면 값을 보여주기만 한다 (이메일은 인증을 거쳐야 바꿀 수 있어 여기서 수정하지 않는다)
 */
enum class ProfileLinkField(
    val title: String,
    val placeholder: String,
    val editable: Boolean = true,
) {
    EMAIL(AppStrings.PROFILE_LINK_EMAIL, AppStrings.PROFILE_LINK_EMAIL_PLACEHOLDER, editable = false),
    GITHUB(AppStrings.PROFILE_LINK_GITHUB, AppStrings.PROFILE_LINK_GITHUB_PLACEHOLDER),
    LINKEDIN(AppStrings.PROFILE_LINK_LINKEDIN, AppStrings.PROFILE_LINK_LINKEDIN_PLACEHOLDER),
    BLOG(AppStrings.PROFILE_LINK_BLOG, AppStrings.PROFILE_LINK_BLOG_PLACEHOLDER),
    INSTAGRAM(AppStrings.PROFILE_LINK_INSTAGRAM, AppStrings.PROFILE_LINK_INSTAGRAM_PLACEHOLDER),
    PERSONAL(AppStrings.PROFILE_LINK_PERSONAL, AppStrings.PROFILE_LINK_PERSONAL_PLACEHOLDER),
}
