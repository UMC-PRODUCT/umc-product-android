package com.umc.presentation.community.chatting

import com.umc.domain.model.community.thread.CommunityThreadMessage

internal const val DELETED_REPLY_SNIPPET = "삭제된 메시지입니다."

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
    .filterNot { it.messageId in deletedIds || it.deletedAt != null || it.status == "DELETED" }
    .filterNot { it.type.isSystemNotice }
    .take(limit.coerceAtLeast(0))
    .toList()

internal fun communityDeletedMessageIds(
    messages: List<CommunityThreadMessage>,
    knownDeletedIds: Set<String> = emptySet(),
): Set<String> = knownDeletedIds + messages.asSequence()
    .filter { it.deletedAt != null || it.status == "DELETED" }
    .map { it.messageId }
    .toSet()

/** Keep tombstones in state for reconciliation, but display only their existing replies. */
internal fun communityDisplayedMessages(
    messages: List<CommunityThreadMessage>,
    deletedIds: Set<String>,
): List<CommunityThreadMessage> = messages.asReversed()
    .filterNot { it.messageId in deletedIds }
    .map { message ->
        val reply = message.replyTo
        if (reply != null && reply.messageId in deletedIds) {
            message.copy(replyTo = reply.copy(snippet = DELETED_REPLY_SNIPPET))
        } else message
    }
