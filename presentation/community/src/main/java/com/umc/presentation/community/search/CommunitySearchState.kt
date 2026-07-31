package com.umc.presentation.community.search

import com.umc.presentation.community.model.CommunityThreadUiModel

data class CommunitySearchState(
    val query: String = "",
    val recentSearches: List<String> = emptyList(),
    val searchResults: List<CommunityThreadUiModel> = emptyList(),
    val hasSearched: Boolean = false,
    val isLoading: Boolean = false,

) {
    val isInitial: Boolean
        get() = !hasSearched

    val isResultEmpty: Boolean
        get() = hasSearched && searchResults.isEmpty()
}