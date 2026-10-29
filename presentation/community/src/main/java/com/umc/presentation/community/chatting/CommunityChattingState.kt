package com.umc.presentation.community.chatting

import com.umc.component.base.UiState
import com.umc.domain.model.community.chatting.CommunityChatConnectionState
import com.umc.domain.model.community.thread.CommunityMessageType
import com.umc.domain.model.community.thread.CommunityThreadDetail
import com.umc.domain.model.community.thread.CommunityThreadMember
import com.umc.domain.model.community.thread.CommunityThreadRole
import com.umc.domain.model.community.thread.CommunityThreadMessage
import com.umc.domain.model.enums.AiFeatureStatus

data class CommunityChattingState(
    val threadId: String,
    val thread: CommunityThreadDetail? = null,
    val myMemberId: String = "",
    val members: Map<String, CommunityThreadMember> = emptyMap(),
    val memberProfiles: Map<String, CommunityParticipantProfile> = emptyMap(),
    val messages: List<CommunityThreadMessage> = emptyList(),
    // 실시간 퇴장 안내는 서버 메시지 ID가 없으므로 읽음/요약 대상과 별도로 보관한다.
    val departureNotices: List<CommunityThreadMessage> = emptyList(),
    val knownDeletedMessageIds: Set<String> = emptySet(),
    val unreadMessagesAtEntry: List<CommunityThreadMessage> = emptyList(),
    val unreadCountAtEntry: Int = 0,
    val summaryCandidateMessages: List<CommunityThreadMessage> = emptyList(),
    val localImageUrisByMessageId: Map<String, List<String>> = emptyMap(),
    val pendingMessages: Map<String, PendingCommunityMessage> = emptyMap(),
    val readWatermarks: Map<String, String> = emptyMap(),
    val connectionState: CommunityChatConnectionState = CommunityChatConnectionState.DISCONNECTED,
    val draft: String = "",
    val mentionedMembers: Map<String, String> = emptyMap(),
    val draftMentions: List<CommunityDraftMention> = emptyList(),
    val editingMessageId: String? = null,
    val editDraft: String = "",
    val editCommandId: String? = null,
    val editError: String? = null,
    val hasMore: Boolean = false,
    val nextBefore: String? = null,
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isRecovering: Boolean = false,
    val recoveryRevision: Int = 0,
    val recoveryIncomplete: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val showOwnershipTransferRequiredDialog: Boolean = false,
    val isDeleting: Boolean = false,
    val aiFeatureStatus: AiFeatureStatus? = null,
    val isSummarizingUnread: Boolean = false,
    val aiDownloadPercent: Int? = null,
    val unreadSummary: String? = null,
    val unreadSummaryError: String? = null,
    val summarizedMessageCount: Int = 0,
    val summaryMessageIds: List<String> = emptyList(),
    val summaryStart: String = "",
    val summaryEnd: String = "",
    val summaryTruncated: Boolean = false,
    val errorMessage: String? = null,
    val isThreadUnavailable: Boolean = false,
    val isAccessDenied: Boolean = false,
) : UiState {
    val isLastRemainingOwner: Boolean
        get() = thread?.myRole == CommunityThreadRole.OWNER && thread.memberCount.toIntOrNull() == 1

    val canSummarize: Boolean
        get() = aiFeatureStatus?.isUsable == true

    internal fun acknowledgeEdit(commandId: String): CommunityChattingState =
        if (editCommandId == commandId) {
            copy(editingMessageId = null, editDraft = "", editCommandId = null, editError = null)
        } else this

    internal fun rejectEdit(commandId: String?, message: String): CommunityChattingState =
        if (commandId != null && editCommandId == commandId) {
            copy(editCommandId = null, editError = message)
        } else this
}

data class PendingCommunityMessage(
    val clientMessageId: String,
    val commandId: String,
    val content: String,
    val type: CommunityMessageType = CommunityMessageType.TEXT,
    val localUris: List<String> = emptyList(),
    val fileMetadataIds: List<String> = emptyList(),
    val confirmedFilesByUri: Map<String, String> = emptyMap(),
    val error: String? = null,
    val acknowledged: Boolean = false,
    val mentionedMemberIds: List<Long> = emptyList(),
    val replyToId: String? = null,
)
