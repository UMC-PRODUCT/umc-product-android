package com.umc.presentation.community.chatting

import com.umc.component.base.UiState
import com.umc.domain.model.community.chatting.CommunityChatConnectionState
import com.umc.domain.model.community.thread.CommunityMessageType
import com.umc.domain.model.community.thread.CommunityThreadDetail
import com.umc.domain.model.community.thread.CommunityThreadMember
import com.umc.domain.model.community.thread.CommunityThreadMessage
import com.umc.domain.model.enums.AiFeatureStatus

data class CommunityChattingState(
    val threadId: String,
    val thread: CommunityThreadDetail? = null,
    val myMemberId: String = "",
    val members: Map<String, CommunityThreadMember> = emptyMap(),
    val messages: List<CommunityThreadMessage> = emptyList(),
    val unreadMessagesAtEntry: List<CommunityThreadMessage> = emptyList(),
    val unreadCountAtEntry: Int = 0,
    val summaryCandidateMessages: List<CommunityThreadMessage> = emptyList(),
    val localImageUrisByMessageId: Map<String, List<String>> = emptyMap(),
    val pendingMessages: Map<String, PendingCommunityMessage> = emptyMap(),
    val readWatermarks: Map<String, String> = emptyMap(),
    val connectionState: CommunityChatConnectionState = CommunityChatConnectionState.DISCONNECTED,
    val draft: String = "",
    val hasMore: Boolean = false,
    val nextBefore: String? = null,
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val showOwnershipTransferRequiredDialog: Boolean = false,
    val isDeleting: Boolean = false,
    val aiFeatureStatus: AiFeatureStatus? = null,
    val isSummarizingUnread: Boolean = false,
    val aiDownloadPercent: Int? = null,
    val unreadSummary: String? = null,
    val unreadSummaryError: String? = null,
    val summarizedMessageCount: Int = 0,
    val errorMessage: String? = null,
    val isThreadUnavailable: Boolean = false,
) : UiState

data class PendingCommunityMessage(
    val clientMessageId: String,
    val commandId: String,
    val content: String,
    val type: CommunityMessageType = CommunityMessageType.TEXT,
    val localUris: List<String> = emptyList(),
    val fileMetadataIds: List<String> = emptyList(),
    val error: String? = null,
    val acknowledged: Boolean = false,
    val mentionedMemberIds: List<Long> = emptyList(),
    val replyToId: String? = null,
)
