package com.umc.presentation.community.model

import com.umc.component.theme.AppStrings

/**
 * 커뮤니티 스레드에서 사용하는 카테고리
 *
 * 스레드 분류 및 목록 필터링에 사용되며,
 * 각 카테고리가 화면에 표시할 한글 라벨을 관리합니다.
 */
enum class CommunityCategory(
    val label: String,
) {
    ALL(AppStrings.COMMON_ALL),
    UNREAD(AppStrings.COMMUNITY_CATEGORY_UNREAD),

    STUDY(AppStrings.COMMUNITY_CATEGORY_STUDY),
    QNA(AppStrings.COMMON_QNA),
    PROJECT(AppStrings.COMMUNITY_CATEGORY_PROJECT),
    FREE(AppStrings.COMMUNITY_CATEGORY_FREE),
}