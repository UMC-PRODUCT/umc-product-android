package com.umc.presentation.community

sealed interface CommunityEvent {

    data class NavigateToThreadDetail(
        val threadId: String,
    ) : CommunityEvent

    data object NavigateToSearch : CommunityEvent

    data object NavigateToCreateThread : CommunityEvent

    data class NavigateToEditThread(
        val threadId: String,
    ) : CommunityEvent

    data class ShowToast(
        val message: String,
    ) : CommunityEvent
}