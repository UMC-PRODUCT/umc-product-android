package com.umc.presentation.community.search

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
