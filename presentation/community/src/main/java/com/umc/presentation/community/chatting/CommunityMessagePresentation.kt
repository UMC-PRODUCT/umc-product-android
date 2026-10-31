package com.umc.presentation.community.chatting

import com.umc.domain.model.community.thread.CommunityThreadMessage

internal const val DELETED_MESSAGE_NOTICE = "삭제된 메시지입니다."

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

/** 삭제 원문은 안내로 표시하고, 기존 답장에 담긴 인용 원문은 유지한다. */
internal fun communityDisplayedMessages(
    messages: List<CommunityThreadMessage>,
    deletedIds: Set<String>,
): List<CommunityThreadMessage> = messages.asReversed()
    .map { message ->
        if (message.messageId in deletedIds || message.deletedAt != null || message.status == "DELETED") {
            message.copy(
                status = "DELETED",
                content = DELETED_MESSAGE_NOTICE,
                fileMetadataIds = emptyList(),
                files = emptyList(),
                mentions = emptyList(),
                replyTo = null,
                reactions = emptyList(),
            )
        } else message
    }
