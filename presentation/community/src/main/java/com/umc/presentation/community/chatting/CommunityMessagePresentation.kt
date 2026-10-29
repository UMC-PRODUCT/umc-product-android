package com.umc.presentation.community.chatting

import com.umc.domain.model.community.thread.CommunityThreadMessage
import com.umc.domain.model.community.thread.CommunityMessageType
import com.umc.domain.model.community.chatting.CommunityChatEvent
import java.time.Instant
import java.time.OffsetDateTime
import java.time.LocalDateTime
import java.time.ZoneId

internal const val DELETED_REPLY_SNIPPET = "삭제된 메시지입니다."

/** Local notices never enter the server message list used by read receipts and pagination. */
internal fun CommunityChattingState.withDepartureNotice(
    event: CommunityChatEvent.ThreadStateChanged,
    memberName: String? = null,
): CommunityChattingState {
    val action = when (event.type) {
        CommunityChatEvent.ThreadStateChanged.Type.MEMBER_LEFT -> "나갔습니다."
        CommunityChatEvent.ThreadStateChanged.Type.MEMBER_KICKED -> "내보내졌습니다."
        else -> return this
    }
    if (event.memberId == null || event.memberId == myMemberId) return this
    val id = "member-departure:${event.eventId}"
    if (departureNotices.any { it.messageId == id }) return this
    // HTTP 성공으로 먼저 표시한 안내는 뒤늦게 수신한 실시간 이벤트로 교체한다.
    val localKick = if (event.type == CommunityChatEvent.ThreadStateChanged.Type.MEMBER_KICKED &&
        !event.eventId.startsWith("local-kick:") && event.memberId !in members) {
        departureNotices.lastOrNull {
            it.senderId == event.memberId && it.messageId.startsWith("member-departure:local-kick:")
        }
    } else null
    if (localKick != null) return copy(departureNotices = departureNotices.map {
        if (it.messageId == localKick.messageId) it.copy(
            messageId = id, createdAt = event.occurredAt.ifBlank { it.createdAt },
        ) else it
    })
    val name = memberName?.takeIf { it.isNotBlank() }
        ?: members[event.memberId]?.name?.takeIf { it.isNotBlank() }
        ?: messages.firstOrNull { it.senderId == event.memberId && !it.senderName.isNullOrBlank() }?.senderName
    val notice = CommunityThreadMessage(
        messageId = id,
        threadId = event.threadId,
        senderId = event.memberId,
        content = name?.let { "$it 님이 $action" } ?: "참여자가 $action",
        type = CommunityMessageType.SYSTEM,
        createdAt = event.occurredAt.ifBlank { Instant.now().toString() },
    )
    return copy(departureNotices = departureNotices + notice)
}

/** Successful HTTP kicks must be visible even when the actor receives no member.kicked event. */
internal fun CommunityChattingState.withSuccessfulKickNotice(
    beforeRequest: CommunityChattingState,
    memberId: String,
    requestId: String,
    occurredAt: String,
): CommunityChattingState {
    val previousIds = beforeRequest.departureNotices.mapTo(hashSetOf()) { it.messageId }
    if (departureNotices.any { it.senderId == memberId && it.messageId !in previousIds }) return this
    return withDepartureNotice(
        CommunityChatEvent.ThreadStateChanged(
            eventId = "local-kick:$requestId", threadId = threadId, occurredAt = occurredAt,
            type = CommunityChatEvent.ThreadStateChanged.Type.MEMBER_KICKED, memberId = memberId,
        ),
        memberName = beforeRequest.members[memberId]?.name,
    )
}

/** Merge display-only notices by event time and prefer a persisted server notice when available. */
internal fun communityMessagesWithDepartureNotices(
    messages: List<CommunityThreadMessage>,
    notices: List<CommunityThreadMessage>,
): List<CommunityThreadMessage> {
    if (notices.isEmpty()) return messages
    val serverNotices = messages.filter { it.type == CommunityMessageType.SYSTEM }
    val localNotices = notices.filterNot { notice ->
        serverNotices.any {
            it.content == notice.content && messageTime(it.createdAt)?.epochSecond != null &&
                messageTime(it.createdAt)?.epochSecond == messageTime(notice.createdAt)?.epochSecond
        }
    }
    return (messages + localNotices).sortedByDescending { messageTime(it.createdAt) ?: Instant.MIN }
}

private fun messageTime(value: String): Instant? = runCatching { Instant.parse(value) }.getOrNull()
    ?: runCatching { OffsetDateTime.parse(value).toInstant() }.getOrNull()
    ?: runCatching { LocalDateTime.parse(value).atZone(ZoneId.of("Asia/Seoul")).toInstant() }.getOrNull()

/** Exclude deleted messages before limiting the entry summary candidates. */
internal fun communityUnreadSummaryCandidates(
    messages: List<CommunityThreadMessage>,
    deletedIds: Set<String>,
    limit: Int,
): List<CommunityThreadMessage> = messages.asSequence()
    .filterNot { it.messageId in deletedIds || it.deletedAt != null || it.status == "DELETED" }
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
