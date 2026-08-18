package com.umc.presentation.community.search

/**
 * 커뮤니티 검색 화면에서 발생하는 사용자 액션
 *
 * 검색어 입력, 검색 실행, 최근 검색어 관리,
 * 스레드 선택 등의 사용자 동작을 관리합니다.
 */
sealed interface CommunitySearchAction {

    data class OnQueryChanged(
        val query: String,
    ) : CommunitySearchAction

    data object OnSearchClick : CommunitySearchAction

    data object OnCancelClick : CommunitySearchAction

    data object OnBackClick : CommunitySearchAction

    data object OnClearQueryClick : CommunitySearchAction

    data object OnClearAllRecentSearchesClick : CommunitySearchAction

    data class OnRecentSearchClick(
        val query: String,
    ) : CommunitySearchAction

    data class OnDeleteRecentSearchClick(
        val query: String,
    ) : CommunitySearchAction

    data class OnThreadClick(
        val threadId: String,
    ) : CommunitySearchAction
}