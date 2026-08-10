package com.umc.presentation.community.chatting

import com.umc.component.base.UiEvent

sealed interface CommunityChattingEvent : UiEvent {
    data object NavigateBack : CommunityChattingEvent
    data object OpenMore : CommunityChattingEvent
    data object OpenUnreadSummary : CommunityChattingEvent
    data object OpenCamera : CommunityChattingEvent
    data object OpenInviteParticipants : CommunityChattingEvent
    data object OpenEditThread : CommunityChattingEvent
    data class ShowError(val message: String) : CommunityChattingEvent
    data object MessageReported : CommunityChattingEvent
    data object ThreadDeleted : CommunityChattingEvent
    data object ThreadUnavailable : CommunityChattingEvent
}
