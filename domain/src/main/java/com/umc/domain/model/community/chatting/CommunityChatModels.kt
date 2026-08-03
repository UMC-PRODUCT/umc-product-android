package com.umc.domain.model.community.chatting

import com.umc.domain.model.community.thread.CommunityReaction
import com.umc.domain.model.community.thread.CommunityThreadMessage
import com.umc.domain.model.community.thread.CommunityThreadSummary

sealed interface CommunityChatEvent {
    val eventId: String
    val threadId: String
    val occurredAt: String

    data class CommandAcknowledged(
        override val eventId: String,
        override val threadId: String,
        override val occurredAt: String,
        val commandId: String,
        val command: String,
        val messageId: String? = null,
        val clientMessageId: String? = null,
        val deduplicated: Boolean = false,
    ) : CommunityChatEvent

    data class MessageChanged(
        override val eventId: String,
        override val threadId: String,
        override val occurredAt: String,
        val type: Type,
        val message: CommunityThreadMessage,
        val clientMessageId: String? = null,
    ) : CommunityChatEvent {
        enum class Type { CREATED, UPDATED, DELETED }
    }

    data class ReactionChanged(
        override val eventId: String,
        override val threadId: String,
        override val occurredAt: String,
        val messageId: String,
        val reactions: List<CommunityReaction>,
    ) : CommunityChatEvent

    data class ReadUpdated(
        override val eventId: String,
        override val threadId: String,
        override val occurredAt: String,
        val memberId: String,
        val lastReadMessageId: String,
    ) : CommunityChatEvent

    data class ThreadInvited(
        override val eventId: String,
        override val threadId: String,
        override val occurredAt: String,
        val thread: CommunityThreadSummary,
    ) : CommunityChatEvent

    data class ThreadStateChanged(
        override val eventId: String,
        override val threadId: String,
        override val occurredAt: String,
        val type: Type,
        val memberId: String? = null,
        val memberCount: String? = null,
    ) : CommunityChatEvent {
        enum class Type { UPDATED, DELETED, MEMBER_KICKED, MEMBER_LEFT }
    }
}

data class CommunityChatError(
    val commandId: String? = null,
    val clientMessageId: String? = null,
    val status: Int = 0,
    val code: String = "",
    val message: String = "",
    val retryable: Boolean = false,
)

enum class CommunityChatConnectionState { DISCONNECTED, CONNECTING, CONNECTED }

data class CreateCommunityMessageCommand(
    val clientMessageId: String,
    val type: String,
    val content: String?,
    val fileMetadataIds: List<String> = emptyList(),
    val mentionedMemberIds: List<Long> = emptyList(),
    val replyToId: Long? = null,
)
