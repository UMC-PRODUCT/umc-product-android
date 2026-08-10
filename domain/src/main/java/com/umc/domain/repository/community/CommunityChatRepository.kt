package com.umc.domain.repository.community

import com.umc.domain.model.community.chatting.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface CommunityChatRepository {
    val connectionState: StateFlow<CommunityChatConnectionState>
    val events: Flow<CommunityChatEvent>
    val errors: Flow<CommunityChatError>

    suspend fun connect()
    fun disconnect()
    fun createMessage(threadId: String, command: CreateCommunityMessageCommand): String
    fun editMessage(threadId: String, messageId: String, content: String): String
    fun deleteMessage(threadId: String, messageId: String): String
    fun addReaction(threadId: String, messageId: String, emoji: String): String
    fun removeReaction(threadId: String, messageId: String, emoji: String): String
    fun updateRead(threadId: String, lastReadMessageId: String): String
}
