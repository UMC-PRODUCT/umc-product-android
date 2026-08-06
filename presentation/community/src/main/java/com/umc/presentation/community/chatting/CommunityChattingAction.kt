package com.umc.presentation.community.chatting

import com.umc.domain.model.community.thread.CommunityMessageReportReason
import com.umc.domain.model.community.thread.CommunityThreadMessage

sealed interface CommunityChattingAction {
    data object OnBackClick : CommunityChattingAction
    data object OnMoreClick : CommunityChattingAction
    data object OnUnreadSummaryClick : CommunityChattingAction
    data object OnCameraClick : CommunityChattingAction
    data class OnSendImages(val uris: List<String>) : CommunityChattingAction
    data class OnDraftChanged(val value: String) : CommunityChattingAction
    data class OnSendClick(val replyToId: Long?) : CommunityChattingAction
    data object OnLoadPrevious : CommunityChattingAction
    data class OnDeleteMessage(val messageId: String) : CommunityChattingAction
    data class OnReact(
        val message: CommunityThreadMessage,
        val emoji: String,
    ) : CommunityChattingAction
    data class OnReportMessage(
        val messageId: String,
        val reason: CommunityMessageReportReason,
    ) : CommunityChattingAction
    data object OnToggleMuted : CommunityChattingAction
    data object OnTogglePinned : CommunityChattingAction
    data object OnLeave : CommunityChattingAction
    data object OnDismissOwnershipTransferRequired : CommunityChattingAction
    data class OnRetryPending(val clientMessageId: String) : CommunityChattingAction
    data class OnDismissPending(val clientMessageId: String) : CommunityChattingAction
    data object OnRetryLoad : CommunityChattingAction
    data object OnInviteParticipants : CommunityChattingAction
    data object OnEditThread : CommunityChattingAction
    data object OnDeleteThread : CommunityChattingAction
    data object OnDismissDeleteThread : CommunityChattingAction
    data object OnConfirmDeleteThread : CommunityChattingAction
    data class OnKickMember(val memberId: String) : CommunityChattingAction
    data class OnTransferOwnership(val memberId: String) : CommunityChattingAction
}
