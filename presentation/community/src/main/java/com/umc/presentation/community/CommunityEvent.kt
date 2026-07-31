package com.umc.presentation.community

sealed interface CommunityEvent {

    data class NavigateToThreadDetail(
        val threadId: Long,
    ) : CommunityEvent

    data object NavigateToSearch : CommunityEvent

    data object NavigateToCreateThread : CommunityEvent

    data class ShowToast(
        val message: String,
    ) : CommunityEvent
}