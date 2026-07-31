package com.umc.presentation.community.search

sealed interface CommunitySearchEvent {

    data object NavigateBack : CommunitySearchEvent

    data class NavigateToThreadDetail(
        val threadId: Long,
    ) : CommunitySearchEvent
}