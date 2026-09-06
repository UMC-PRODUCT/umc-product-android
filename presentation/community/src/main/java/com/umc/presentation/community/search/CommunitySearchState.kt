package com.umc.presentation.community.search

import com.umc.presentation.community.model.CommunityThreadUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * 커뮤니티 검색 화면에서 사용하는 UI 상태
 *
 * 검색어, 최근 검색어, 검색 결과 및
 * 검색 진행 상태와 오류 정보를 관리합니다.
 */
data class CommunitySearchState(
    val query: String = "",
    val recentSearches: ImmutableList<String> = persistentListOf(),
    val searchResults: ImmutableList<CommunityThreadUiModel> = persistentListOf(),
    val hasSearched: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
) {
    val isInitial: Boolean
        get() = !hasSearched

    val isResultEmpty: Boolean
        get() = hasSearched &&
                !isLoading &&
                errorMessage == null &&
                searchResults.isEmpty()
}