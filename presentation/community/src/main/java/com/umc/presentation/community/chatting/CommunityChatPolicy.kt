package com.umc.presentation.community.chatting

internal const val COMMUNITY_CHAT_MAX_IMAGE_COUNT = 4
internal const val COMMUNITY_CHAT_MAX_MESSAGE_LENGTH = 2_000


internal fun isCommunityChatMessageTooLong(content: String): Boolean {
    val trimmed = content.trim()
    return trimmed.codePointCount(0, trimmed.length) > COMMUNITY_CHAT_MAX_MESSAGE_LENGTH
}
