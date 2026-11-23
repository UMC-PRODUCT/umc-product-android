package com.umc.presentation.community.chatting

import com.umc.component.theme.AppStrings
import com.umc.domain.model.community.thread.CommunityThreadMessage

internal const val DELETED_REPLY_SNIPPET = AppStrings.CHAT_DELETED_REPLY_SNIPPET
internal const val DELETED_MESSAGE_NOTICE = AppStrings.CHAT_DELETED_REPLY_SNIPPET

/** 서버가 삭제된 메시지에 내려주는 status 값 */
internal const val COMMUNITY_MESSAGE_STATUS_DELETED = "DELETED"

/** 삭제된 메시지인지. 서버는 deletedAt 과 status 중 하나만 채워 보낼 수 있어 둘 다 본다. */
internal val CommunityThreadMessage.isDeleted: Boolean
    get() = deletedAt != null || status == COMMUNITY_MESSAGE_STATUS_DELETED

/** 서버 미읽음 개수는 삭제·시스템 기록을 포함한 최신 메시지 범위다. */
internal fun communityEntryUnreadMessages(
    messages: List<CommunityThreadMessage>,
    deletedIds: Set<String>,
    unreadCount: Int,
): List<CommunityThreadMessage> = communityUnreadSummaryCandidates(
    messages.take(unreadCount.coerceAtLeast(0)), deletedIds, unreadCount.coerceAtLeast(0),
)

/** Exclude deleted messages before limiting the entry summary candidates. */
internal fun communityUnreadSummaryCandidates(
    messages: List<CommunityThreadMessage>,
    deletedIds: Set<String>,
    limit: Int,
): List<CommunityThreadMessage> = messages.asSequence()
    .filterNot { it.messageId in deletedIds || it.isDeleted }
    .filterNot { it.type.isSystemNotice }
    .take(limit.coerceAtLeast(0))
    .toList()

internal fun communityDeletedMessageIds(
    messages: List<CommunityThreadMessage>,
    knownDeletedIds: Set<String> = emptySet(),
): Set<String> = knownDeletedIds + messages.asSequence()
    .filter { it.isDeleted }
    .map { it.messageId }
    .toSet()

/** 삭제 원문은 안내로 표시하고, 기존 답장에 담긴 인용 원문은 유지한다. */
internal fun communityDisplayedMessages(
    messages: List<CommunityThreadMessage>,
    deletedIds: Set<String>,
): List<CommunityThreadMessage> = messages.asReversed()
    .map { message ->
        if (message.messageId in deletedIds || message.isDeleted) {
            message.copy(
                status = COMMUNITY_MESSAGE_STATUS_DELETED,
                content = DELETED_MESSAGE_NOTICE,
                fileMetadataIds = emptyList(),
                files = emptyList(),
                mentions = emptyList(),
                replyTo = null,
                reactions = emptyList(),
            )
        } else message
    }
