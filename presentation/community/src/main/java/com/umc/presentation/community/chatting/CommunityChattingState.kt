package com.umc.presentation.community.chatting

import com.umc.component.base.UiState
import com.umc.domain.model.community.chatting.CommunityChatConnectionState
import com.umc.domain.model.community.thread.CommunityMessageType
import com.umc.domain.model.community.thread.CommunityThreadDetail
import com.umc.domain.model.community.thread.CommunityThreadMember
import com.umc.domain.model.community.thread.CommunityThreadRole
import com.umc.domain.model.community.thread.CommunityThreadMessage
import com.umc.domain.model.enums.AiFeatureStatus

enum class BlockedMembersStatus { LOADING, LOADED, FAILED }

data class CommunityChattingState(
    val threadId: String,
    val thread: CommunityThreadDetail? = null,
    val myMemberId: String = "",
    val members: Map<String, CommunityThreadMember> = emptyMap(),
    val memberProfiles: Map<String, CommunityParticipantProfile> = emptyMap(),
    val messages: List<CommunityThreadMessage> = emptyList(),
    val blockedMembersStatus: BlockedMembersStatus = BlockedMembersStatus.LOADING,
    val verifiedUnblockedMemberIds: Set<String> = emptySet(),
    val blockedMemberIds: Set<String> = emptySet(),
    val revealedBlockedMessageIds: Set<String> = emptySet(),
    val isManagingParticipants: Boolean = false,
    val participantManagementRevision: Int = 0,
    val managedParticipantIds: Set<String> = emptySet(),
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
    /** 목록의 강퇴 카드(membershipState = KICKED)에서 진입했는지 여부 */
    val isKicked: Boolean = false,
    /** 강퇴 확인 API 요청 중 여부 (중복 클릭 방지) */
    val isAcknowledgingKick: Boolean = false,
) : UiState {
    internal fun toggleBlockedMessage(messageId: String): CommunityChattingState {
        val message = messages.firstOrNull { it.messageId == messageId } ?: return this
        if (message.senderId == myMemberId || message.senderId !in blockedMemberIds ||
            message.type.isSystemNotice || message.deletedAt != null || message.status == "DELETED") return this
        return copy(revealedBlockedMessageIds = if (messageId in revealedBlockedMessageIds)
            revealedBlockedMessageIds - messageId else revealedBlockedMessageIds + messageId)
    }

    internal fun canShowReplySnippet(source: CommunityThreadMessage?): Boolean =
        source?.senderId?.let { sender -> sender == myMemberId ||
            (!isSenderVisibilityPending(sender) && sender !in blockedMemberIds) } == true

    internal fun isSenderVisibilityPending(senderId: String?): Boolean =
        senderId != myMemberId && blockedMembersStatus != BlockedMembersStatus.LOADED &&
            senderId !in blockedMemberIds && senderId !in verifiedUnblockedMemberIds

    internal fun confirmedUnblockedSenders(): Set<String> =
        (verifiedUnblockedMemberIds + messages.mapNotNull { it.senderId } + members.keys) - blockedMemberIds

    internal fun withMemberBlockChange(memberId: String, blocked: Boolean): CommunityChattingState =
        withoutUnreadSummary().copy(
            blockedMemberIds = if (blocked) blockedMemberIds + memberId else blockedMemberIds - memberId,
            verifiedUnblockedMemberIds = if (blocked) verifiedUnblockedMemberIds - memberId else verifiedUnblockedMemberIds + memberId,
            revealedBlockedMessageIds = revealedBlockedMessageIds - messages.filter { it.senderId == memberId }.map { it.messageId }.toSet(),
        )

    internal fun summaryInputMessages(candidates: List<CommunityThreadMessage>): List<CommunityThreadMessage> =
        if (blockedMembersStatus != BlockedMembersStatus.LOADED) emptyList()
        else candidates.filterNot { it.senderId in blockedMemberIds }

    internal fun withoutUnreadSummary(): CommunityChattingState = copy(
        unreadSummary = null, unreadSummaryError = null, isSummarizingUnread = false,
        aiDownloadPercent = null, summarizedMessageCount = 0, summaryMessageIds = emptyList(),
        summaryStart = "", summaryEnd = "", summaryTruncated = false,
    )

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
