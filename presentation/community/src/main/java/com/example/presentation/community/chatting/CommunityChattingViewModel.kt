package com.example.presentation.community.chatting

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.umc.component.base.BaseViewModel
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.community.chatting.*
import com.umc.domain.model.community.thread.*
import com.umc.domain.repository.AppDataStoreRepository
import com.umc.domain.repository.community.CommunityChatRepository
import com.umc.domain.repository.community.CommunityThreadRepository
import com.umc.domain.repository.member.MemberRepository
import com.umc.domain.model.enums.UploadFileCategory
import com.umc.domain.usecase.storage.UploadFileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CommunityChattingViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val threadRepository: CommunityThreadRepository,
    private val chatRepository: CommunityChatRepository,
    private val appDataStoreRepository: AppDataStoreRepository,
    private val memberRepository: MemberRepository,
    private val uploadFileUseCase: UploadFileUseCase,
) : BaseViewModel<CommunityChattingUiState, CommunityChattingEvent>(
    CommunityChattingUiState(threadId = checkNotNull(savedStateHandle["threadId"]))
) {
    private val processedEventIds = LinkedHashSet<String>()
    private var lastReadMessageIdSent: String? = null
    private var reconnectJob: Job? = null
    private var manualReconnectInProgress = false

    init {
        observeRealtime()
        observeCurrentMember()
        bootstrap()
    }

    fun bootstrap() = viewModelScope.launch {
        updateState { copy(isLoading = true, errorMessage = null, isThreadUnavailable = false) }
        val threadId = uiState.value.threadId
        val detail = threadRepository.getThread(threadId)
        val messages = threadRepository.getMessages(threadId)
        if (detail is ApiState.Success && messages is ApiState.Success) {
            updateState {
                copy(
                    thread = detail.data,
                    messages = messages.data.messages.distinctBy(CommunityThreadMessage::messageId),
                    hasMore = messages.data.hasMore,
                    nextBefore = messages.data.nextBefore,
                    isLoading = false,
                )
            }
            loadMembers()
            chatRepository.connect()
        } else {
            val detailFailure = (detail as? ApiState.Fail)?.failState
            if (detailFailure != null && detailFailure.isThreadUnavailable()) {
                updateState {
                    copy(
                        isLoading = false,
                        errorMessage = null,
                        isThreadUnavailable = true,
                    )
                }
                return@launch
            }
            val message = (detail as? ApiState.Fail)?.failState?.message
                ?: (messages as? ApiState.Fail)?.failState?.message
                ?: "채팅을 불러오지 못했습니다."
            updateState { copy(isLoading = false, errorMessage = message) }
            emitEvent(CommunityChattingEvent.ShowError(message))
        }
    }

    fun loadPreviousMessages() = viewModelScope.launch {
        val state = uiState.value
        if (!state.hasMore || state.isLoadingMore) return@launch
        updateState { copy(isLoadingMore = true) }
        when (val response = threadRepository.getMessages(state.threadId, state.nextBefore)) {
            is ApiState.Success -> updateState {
                copy(
                    messages = (messages + response.data.messages)
                        .distinctBy(CommunityThreadMessage::messageId),
                    hasMore = response.data.hasMore,
                    nextBefore = response.data.nextBefore,
                    isLoadingMore = false,
                )
            }
            is ApiState.Fail -> {
                updateState { copy(isLoadingMore = false) }
                emitEvent(CommunityChattingEvent.ShowError(response.failState.message))
            }
        }
    }

    fun sendText(content: String, mentionedMemberIds: List<Long> = emptyList(), replyToId: Long? = null) {
        val trimmed = content.trim()
        if (trimmed.isEmpty() || trimmed.codePointCount(0, trimmed.length) > 2_000) return
        val clientMessageId = UUID.randomUUID().toString()
        val command = CreateCommunityMessageCommand(
            clientMessageId = clientMessageId,
            type = CommunityMessageType.TEXT.name,
            content = trimmed,
            mentionedMemberIds = mentionedMemberIds.distinct(),
            replyToId = replyToId,
        )
        runCatching { chatRepository.createMessage(uiState.value.threadId, command) }
            .onSuccess { commandId ->
                updateState {
                    copy(
                        draft = "",
                        pendingMessages = pendingMessages + (
                            clientMessageId to PendingCommunityMessage(
                                clientMessageId = clientMessageId,
                                commandId = commandId,
                                content = trimmed,
                                type = CommunityMessageType.TEXT,
                                mentionedMemberIds = mentionedMemberIds.distinct(),
                                replyToId = replyToId,
                            )
                        ),
                    )
                }
            }
            .onFailure { throwable ->
                updateState {
                    copy(
                        draft = "",
                        pendingMessages = pendingMessages + (
                            clientMessageId to PendingCommunityMessage(
                                clientMessageId = clientMessageId,
                                commandId = "",
                                content = trimmed,
                                type = CommunityMessageType.TEXT,
                                error = throwable.message.orEmpty().ifBlank { "메시지를 전송하지 못했습니다." },
                                mentionedMemberIds = mentionedMemberIds.distinct(),
                                replyToId = replyToId,
                            )
                        ),
                    )
                }
            }
    }

    fun editMessage(messageId: String, content: String) =
        runCommand { chatRepository.editMessage(uiState.value.threadId, messageId, content.trim()) }

    fun deleteMessage(messageId: String) =
        runCommand { chatRepository.deleteMessage(uiState.value.threadId, messageId) }

    fun sendImage(uriString: String) = viewModelScope.launch {
        if (uriString.isBlank()) return@launch
        val clientMessageId = UUID.randomUUID().toString()
        updateState {
            copy(
                pendingMessages = pendingMessages + (
                    clientMessageId to PendingCommunityMessage(
                        clientMessageId = clientMessageId,
                        commandId = "",
                        content = "",
                        type = CommunityMessageType.IMAGE,
                        localUri = uriString,
                    )
                )
            )
        }

        when (val upload = uploadFileUseCase(uriString, UploadFileCategory.ETC)) {
            is ApiState.Success -> {
                val fileIds = listOf(upload.data.fileId)
                val command = CreateCommunityMessageCommand(
                    clientMessageId = clientMessageId,
                    type = CommunityMessageType.IMAGE.name,
                    content = null,
                    fileMetadataIds = fileIds,
                )
                runCatching { chatRepository.createMessage(uiState.value.threadId, command) }
                    .onSuccess { commandId ->
                        updateState {
                            val pending = pendingMessages[clientMessageId]
                                ?: return@updateState this
                            copy(
                                pendingMessages = pendingMessages + (
                                    clientMessageId to pending.copy(
                                        commandId = commandId,
                                        fileMetadataIds = fileIds,
                                        error = null,
                                    )
                                )
                            )
                        }
                    }
                    .onFailure { throwable -> markImagePendingFailed(clientMessageId, throwable.message) }
            }
            is ApiState.Fail -> markImagePendingFailed(clientMessageId, upload.failState.message)
        }
    }

    private fun markImagePendingFailed(clientMessageId: String, message: String?) = updateState {
        val pending = pendingMessages[clientMessageId] ?: return@updateState this
        copy(
            pendingMessages = pendingMessages + (
                clientMessageId to pending.copy(
                    error = message.orEmpty().ifBlank { "이미지를 전송하지 못했습니다." },
                )
            )
        )
    }

    fun toggleReaction(message: CommunityThreadMessage, emoji: String) = runCommand {
        if (message.reactions.any { it.emoji == emoji && it.reactedByMe }) {
            chatRepository.removeReaction(uiState.value.threadId, message.messageId, emoji)
        } else {
            chatRepository.addReaction(uiState.value.threadId, message.messageId, emoji)
        }
    }

    fun markRead(messageId: String) {
        if (messageId.isBlank() || lastReadMessageIdSent == messageId) return
        lastReadMessageIdSent = messageId
        updateState { copy(thread = thread?.copy(unreadCount = "0")) }
        runCatching { chatRepository.updateRead(uiState.value.threadId, messageId) }
            .onFailure {
                lastReadMessageIdSent = null
                emitEvent(CommunityChattingEvent.ShowError(it.message.orEmpty()))
            }
    }

    fun reportMessage(messageId: String, reason: CommunityMessageReportReason) = viewModelScope.launch {
        resultResponse(
            threadRepository.reportMessage(messageId, reason),
            successCallback = { emitEvent(CommunityChattingEvent.MessageReported) },
            errorCallback = { emitEvent(CommunityChattingEvent.ShowError(it.message)) },
        )
    }

    fun retryPendingMessage(clientMessageId: String) = viewModelScope.launch {
        val pending = uiState.value.pendingMessages[clientMessageId] ?: return@launch

        if (chatRepository.connectionState.value != CommunityChatConnectionState.CONNECTED) {
            reconnectJob?.cancel()
            reconnectJob = null
            manualReconnectInProgress = true
            chatRepository.disconnect()
            runCatching { chatRepository.connect() }
            val connected = withTimeoutOrNull(7_000) {
                chatRepository.connectionState.first {
                    it == CommunityChatConnectionState.CONNECTED
                }
            } != null
            manualReconnectInProgress = false

            if (!connected) {
                updateState {
                    copy(
                        pendingMessages = pendingMessages + (
                            clientMessageId to pending.copy(
                                error = "연결되지 않아 재전송하지 못했습니다.",
                            )
                        )
                    )
                }
                ensureReconnectLoop()
                return@launch
            }
        }

        updateState { copy(pendingMessages = pendingMessages - clientMessageId) }
        if (pending.type == CommunityMessageType.IMAGE) {
            if (pending.fileMetadataIds.isEmpty()) {
                sendImage(pending.localUri.orEmpty())
            } else {
                val newClientMessageId = UUID.randomUUID().toString()
                val command = CreateCommunityMessageCommand(
                    clientMessageId = newClientMessageId,
                    type = CommunityMessageType.IMAGE.name,
                    content = null,
                    fileMetadataIds = pending.fileMetadataIds,
                )
                runCatching { chatRepository.createMessage(uiState.value.threadId, command) }
                    .onSuccess { commandId ->
                        updateState {
                            copy(
                                pendingMessages = pendingMessages + (
                                    newClientMessageId to pending.copy(
                                        clientMessageId = newClientMessageId,
                                        commandId = commandId,
                                        error = null,
                                    )
                                )
                            )
                        }
                    }
                    .onFailure {
                        updateState { copy(pendingMessages = pendingMessages + (clientMessageId to pending)) }
                    }
            }
        } else {
            sendText(
                content = pending.content,
                mentionedMemberIds = pending.mentionedMemberIds,
                replyToId = pending.replyToId,
            )
        }
    }

    fun dismissPendingMessage(clientMessageId: String) =
        updateState { copy(pendingMessages = pendingMessages - clientMessageId) }

    fun toggleMuted() = viewModelScope.launch {
        val state = uiState.value
        when (val result = threadRepository.setMuted(state.threadId, !(state.thread?.isMuted ?: false))) {
            is ApiState.Success -> updateState { copy(thread = result.data) }
            is ApiState.Fail -> emitEvent(CommunityChattingEvent.ShowError(result.failState.message))
        }
    }

    fun togglePinned() = viewModelScope.launch {
        val state = uiState.value
        when (val result = threadRepository.setPinned(state.threadId, !(state.thread?.isPinned ?: false))) {
            is ApiState.Success -> updateState { copy(thread = result.data) }
            is ApiState.Fail -> emitEvent(CommunityChattingEvent.ShowError(result.failState.message))
        }
    }

    fun leaveThread() = viewModelScope.launch {
        when (val result = threadRepository.leaveThread(uiState.value.threadId)) {
            is ApiState.Success -> emitEvent(CommunityChattingEvent.ThreadUnavailable)
            is ApiState.Fail -> emitEvent(CommunityChattingEvent.ShowError(result.failState.message))
        }
    }

    fun kickMember(memberId: String) = viewModelScope.launch {
        val state = uiState.value
        if (state.thread?.myRole != CommunityThreadRole.OWNER || memberId == state.myMemberId) return@launch

        when (val result = threadRepository.kickMember(state.threadId, memberId)) {
            is ApiState.Success -> updateState {
                copy(
                    members = members - memberId,
                    thread = thread?.copy(memberCount = result.data.memberCount),
                )
            }
            is ApiState.Fail -> emitEvent(CommunityChattingEvent.ShowError(result.failState.message))
        }
    }

    fun updateDraft(value: String) = updateState { copy(draft = value) }

    private fun runCommand(block: () -> String) {
        runCatching(block).onFailure {
            emitEvent(CommunityChattingEvent.ShowError(it.message.orEmpty()))
        }
    }

    private fun observeRealtime() {
        viewModelScope.launch {
            chatRepository.connectionState.collectLatest { connection ->
                updateState { copy(connectionState = connection) }
                if (connection == CommunityChatConnectionState.CONNECTED) {
                    reconnectJob?.cancel()
                    reconnectJob = null
                    reconcileLatestMessages()
                    markLatestMessageRead()
                } else if (
                    connection == CommunityChatConnectionState.DISCONNECTED &&
                    !manualReconnectInProgress
                ) {
                    ensureReconnectLoop()
                }
            }
        }
        viewModelScope.launch {
            chatRepository.events.collect { event ->
                if (event.threadId != uiState.value.threadId || !rememberEvent(event.eventId)) return@collect
                reduce(event)
            }
        }
        viewModelScope.launch {
            chatRepository.errors.collect { error ->
                val clientId = error.clientMessageId
                if (clientId != null) {
                    updateState {
                        copy(
                            pendingMessages = pendingMessages + (
                                clientId to (pendingMessages[clientId]?.copy(error = error.message)
                                    ?: PendingCommunityMessage(
                                        clientMessageId = clientId,
                                        commandId = error.commandId.orEmpty(),
                                        content = "",
                                        error = error.message,
                                    ))
                            )
                        )
                    }
                }
                emitEvent(CommunityChattingEvent.ShowError(error.message))
            }
        }
    }

    private fun observeCurrentMember() {
        viewModelScope.launch {
            appDataStoreRepository.getUserInfo().collectLatest { user ->
                updateState { copy(myMemberId = user.id.toString()) }
            }
        }
    }

    private fun loadMembers() = viewModelScope.launch {
        when (val result = threadRepository.getMembers(uiState.value.threadId, limit = 100)) {
            is ApiState.Success -> {
                val members = result.data.items
                updateState { copy(members = members.associateBy(CommunityThreadMember::memberId)) }
                val membersWithProfiles = coroutineScope {
                    members.map { member ->
                        async {
                            val memberId = member.memberId.toLongOrNull() ?: return@async member
                            when (val profile = memberRepository.getMemberProfile(memberId)) {
                                is ApiState.Success -> member.copy(
                                    profileImageUrl = profile.data.profileImageLink,
                                )
                                is ApiState.Fail -> member
                            }
                        }
                    }.awaitAll()
                }
                updateState {
                    copy(members = membersWithProfiles.associateBy(CommunityThreadMember::memberId))
                }
            }
            is ApiState.Fail -> Unit
        }
    }

    private fun rememberEvent(eventId: String): Boolean {
        if (!processedEventIds.add(eventId)) return false
        if (processedEventIds.size > 500) processedEventIds.remove(processedEventIds.first())
        return true
    }

    private fun ensureReconnectLoop() {
        if (reconnectJob?.isActive == true) return
        reconnectJob = viewModelScope.launch {
            var retryDelayMillis = 1_000L
            while (isActive && chatRepository.connectionState.value != CommunityChatConnectionState.CONNECTED) {
                chatRepository.disconnect()
                runCatching { chatRepository.connect() }

                val result = withTimeoutOrNull(8_000) {
                    chatRepository.connectionState.first {
                        it != CommunityChatConnectionState.CONNECTING
                    }
                }
                val connected = result == CommunityChatConnectionState.CONNECTED
                if (connected) break

                delay(retryDelayMillis)
                retryDelayMillis = (retryDelayMillis * 2).coerceAtMost(15_000L)
            }
        }
    }

    private fun reduce(event: CommunityChatEvent) {
        when (event) {
            is CommunityChatEvent.CommandAcknowledged -> {
                event.clientMessageId?.let { clientId ->
                    updateState {
                        copy(pendingMessages = pendingMessages.mapValues { (id, pending) ->
                            if (id == clientId) pending.copy(acknowledged = true) else pending
                        })
                    }
                }
            }
            is CommunityChatEvent.MessageChanged -> updateState {
                val pending = event.clientMessageId?.let(pendingMessages::get)
                val receivedMessage = if (
                    event.message.type == CommunityMessageType.IMAGE &&
                    event.message.content.isNullOrBlank() &&
                    pending?.localUri != null
                ) {
                    event.message.copy(content = pending.localUri)
                } else {
                    event.message
                }
                val withoutOld = messages.filterNot { it.messageId == receivedMessage.messageId }
                copy(
                    messages = listOf(receivedMessage) + withoutOld,
                    pendingMessages = event.clientMessageId?.let { pendingMessages - it } ?: pendingMessages,
                )
            }.also { markRead(event.message.messageId) }
            is CommunityChatEvent.ReactionChanged -> updateState {
                copy(messages = messages.map {
                    if (it.messageId == event.messageId) it.copy(reactions = event.reactions) else it
                })
            }
            is CommunityChatEvent.ReadUpdated -> updateState {
                copy(
                    readWatermarks = readWatermarks + (event.memberId to event.lastReadMessageId),
                    thread = if (event.memberId == myMemberId) thread?.copy(unreadCount = "0") else thread,
                )
            }
            is CommunityChatEvent.ThreadStateChanged -> when (event.type) {
                CommunityChatEvent.ThreadStateChanged.Type.DELETED,
                CommunityChatEvent.ThreadStateChanged.Type.MEMBER_KICKED,
                CommunityChatEvent.ThreadStateChanged.Type.MEMBER_LEFT ->
                    emitEvent(CommunityChattingEvent.ThreadUnavailable)
                CommunityChatEvent.ThreadStateChanged.Type.UPDATED -> refreshDetail()
            }
            is CommunityChatEvent.ThreadInvited -> Unit
        }
    }

    private fun refreshDetail() = viewModelScope.launch {
        when (val result = threadRepository.getThread(uiState.value.threadId)) {
            is ApiState.Success -> updateState { copy(thread = result.data) }
            is ApiState.Fail -> Unit
        }
    }

    private suspend fun reconcileLatestMessages() {
        when (val result = threadRepository.getMessages(uiState.value.threadId)) {
            is ApiState.Success -> {
                updateState {
                    copy(
                        messages = (result.data.messages + messages)
                            .distinctBy(CommunityThreadMessage::messageId),
                        hasMore = result.data.hasMore,
                        nextBefore = result.data.nextBefore,
                    )
                }
                markLatestMessageRead()
            }
            is ApiState.Fail -> Unit
        }
    }

    private fun markLatestMessageRead() {
        uiState.value.messages.firstOrNull()?.messageId?.let(::markRead)
    }

    override fun onCleared() {
        reconnectJob?.cancel()
        chatRepository.disconnect()
        super.onCleared()
    }
}

data class CommunityChattingUiState(
    val threadId: String,
    val thread: CommunityThreadDetail? = null,
    val myMemberId: String = "",
    val members: Map<String, CommunityThreadMember> = emptyMap(),
    val messages: List<CommunityThreadMessage> = emptyList(),
    val pendingMessages: Map<String, PendingCommunityMessage> = emptyMap(),
    val readWatermarks: Map<String, String> = emptyMap(),
    val connectionState: CommunityChatConnectionState = CommunityChatConnectionState.DISCONNECTED,
    val draft: String = "",
    val hasMore: Boolean = false,
    val nextBefore: String? = null,
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val errorMessage: String? = null,
    val isThreadUnavailable: Boolean = false,
) : UiState

private fun com.umc.domain.model.base.FailState.isThreadUnavailable(): Boolean {
    val normalizedMessage = message.lowercase()
    return code == "404" ||
        normalizedMessage.contains("not found") ||
        normalizedMessage.contains("존재하지") ||
        normalizedMessage.contains("찾을 수 없")
}

data class PendingCommunityMessage(
    val clientMessageId: String,
    val commandId: String,
    val content: String,
    val type: CommunityMessageType = CommunityMessageType.TEXT,
    val localUri: String? = null,
    val fileMetadataIds: List<String> = emptyList(),
    val error: String? = null,
    val acknowledged: Boolean = false,
    val mentionedMemberIds: List<Long> = emptyList(),
    val replyToId: Long? = null,
)

sealed interface CommunityChattingEvent : UiEvent {
    data class ShowError(val message: String) : CommunityChattingEvent
    data object MessageReported : CommunityChattingEvent
    data object ThreadUnavailable : CommunityChattingEvent
}
