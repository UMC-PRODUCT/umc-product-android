package com.umc.presentation.community.chatting

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.umc.component.base.BaseViewModel
import com.umc.component.theme.AppStrings
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.community.chatting.CommunityChatConnectionState
import com.umc.domain.model.community.chatting.CommunityChatEvent
import com.umc.domain.model.community.chatting.CreateCommunityMessageCommand
import com.umc.domain.model.community.thread.CommunityMessageReportReason
import com.umc.domain.model.community.thread.CommunityMessageType
import com.umc.domain.model.community.thread.CommunityThreadMember
import com.umc.domain.model.community.thread.CommunityThreadMessage
import com.umc.domain.model.community.thread.CommunityThreadRole
import com.umc.domain.repository.AppDataStoreRepository
import com.umc.domain.repository.community.CommunityChatRepository
import com.umc.domain.repository.community.CommunityThreadRepository
import com.umc.domain.repository.member.MemberRepository
import com.umc.domain.model.enums.UploadFileCategory
import com.umc.domain.usecase.storage.UploadFileUseCase
import com.umc.domain.usecase.ai.CheckAiFeatureStatusUseCase
import com.umc.domain.usecase.ai.SummarizeUnreadChatUseCase
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
    private val checkAiFeatureStatusUseCase: CheckAiFeatureStatusUseCase,
    private val summarizeUnreadChatUseCase: SummarizeUnreadChatUseCase,
) : BaseViewModel<CommunityChattingState, CommunityChattingEvent>(
    createInitialChattingState(savedStateHandle)
) {
    val state = uiState
    val event = uiEvent

    private val processedEventIds = LinkedHashSet<String>()
    private var lastReadMessageIdSent: String? = null
    private var reconnectJob: Job? = null
    private var manualReconnectInProgress = false
    private var lastSummaryMessages: List<CommunityThreadMessage> = emptyList()

    init {
        observeCurrentMember()
        checkAiAvailability()
        if (state.value.threadId.isNotBlank()) {
            observeRealtime()
            bootstrap()
        }
    }

    fun onAction(action: CommunityChattingAction) {
        when (action) {
            CommunityChattingAction.OnBackClick -> emitEvent(CommunityChattingEvent.NavigateBack)
            CommunityChattingAction.OnMoreClick -> emitEvent(CommunityChattingEvent.OpenMore)
            is CommunityChattingAction.OnUnreadSummaryClick ->
                summarizeUnreadMessages(unreadCount = action.unreadCount)
            CommunityChattingAction.OnRetryUnreadSummary -> {
                updateState { copy(aiFeatureStatus = null) }
                summarizeUnreadMessages(requestedMessages = lastSummaryMessages)
            }
            CommunityChattingAction.OnDismissUnreadSummary ->
                updateState {
                    copy(
                        unreadSummary = null,
                        unreadSummaryError = null,
                        isSummarizingUnread = false,
                        aiDownloadPercent = null,
                    )
                }
            CommunityChattingAction.OnCameraClick -> emitEvent(CommunityChattingEvent.OpenCamera)
            is CommunityChattingAction.OnSendImages -> sendImages(action.uris)
            is CommunityChattingAction.OnDraftChanged -> updateDraft(action.value)
            is CommunityChattingAction.OnSendClick -> sendText(
                content = state.value.draft,
                replyToId = action.replyToId,
            )
            CommunityChattingAction.OnLoadPrevious -> loadPreviousMessages()
            is CommunityChattingAction.OnDeleteMessage -> deleteMessage(action.messageId)
            is CommunityChattingAction.OnReact -> toggleReaction(action.message, action.emoji)
            is CommunityChattingAction.OnReportMessage -> reportMessage(
                messageId = action.messageId,
                reason = action.reason,
            )
            CommunityChattingAction.OnToggleMuted -> toggleMuted()
            CommunityChattingAction.OnTogglePinned -> togglePinned()
            CommunityChattingAction.OnLeave -> leaveThread()
            CommunityChattingAction.OnDismissOwnershipTransferRequired ->
                updateState { copy(showOwnershipTransferRequiredDialog = false) }
            is CommunityChattingAction.OnRetryPending -> retryPendingMessage(action.clientMessageId)
            is CommunityChattingAction.OnDismissPending -> dismissPendingMessage(action.clientMessageId)
            CommunityChattingAction.OnRetryLoad -> bootstrap()
            CommunityChattingAction.OnInviteParticipants -> emitEvent(
                CommunityChattingEvent.OpenInviteParticipants
            )
            CommunityChattingAction.OnEditThread -> emitEvent(CommunityChattingEvent.OpenEditThread)
            CommunityChattingAction.OnDeleteThread -> openDeleteThreadDialog()
            CommunityChattingAction.OnDismissDeleteThread -> dismissDeleteThreadDialog()
            CommunityChattingAction.OnConfirmDeleteThread -> deleteThread()
            is CommunityChattingAction.OnKickMember -> kickMember(action.memberId)
            is CommunityChattingAction.OnTransferOwnership -> transferOwnership(action.memberId)
        }
    }

    fun bootstrap() = viewModelScope.launch {
        updateState { copy(isLoading = true, errorMessage = null, isThreadUnavailable = false) }
        val threadId = uiState.value.threadId
        if (threadId.isBlank()) {
            updateState { copy(isLoading = false, isThreadUnavailable = true) }
            return@launch
        }
        val (detail, messages) = coroutineScope {
            val detailDeferred = async { threadRepository.getThread(threadId) }
            val messagesDeferred = async { threadRepository.getMessages(threadId) }
            detailDeferred.await() to messagesDeferred.await()
        }
        if (detail is ApiState.Success && messages is ApiState.Success) {
            val loadedMessages = messages.data.messages
                .distinctBy(CommunityThreadMessage::messageId)
            val unreadCount = detail.data.unreadCount.toIntOrNull()?.coerceAtLeast(0) ?: 0
            updateState {
                copy(
                    thread = detail.data,
                    messages = loadedMessages,
                    unreadMessagesAtEntry = loadedMessages
                        .take(unreadCount.coerceAtMost(MAX_SUMMARY_MESSAGE_COUNT)),
                    unreadCountAtEntry = unreadCount,
                    hasMore = messages.data.hasMore,
                    nextBefore = messages.data.nextBefore,
                    isLoading = false,
                )
            }
            loadMembers()
            chatRepository.connect()
        } else {
            val detailFailure = (detail as? ApiState.Fail)?.failState
            if (detailFailure != null && detailFailure.hasThreadUnavailableCode()) {
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
                ?: AppStrings.CHAT_LOAD_FAILED
            updateState { copy(isLoading = false, errorMessage = message) }
            emitEvent(CommunityChattingEvent.ShowError(message))
        }
    }

    private fun checkAiAvailability() = viewModelScope.launch {
        val status = checkAiFeatureStatusUseCase()
        updateState { copy(aiFeatureStatus = status) }
    }

    private fun summarizeUnreadMessages(
        requestedMessages: List<CommunityThreadMessage>? = null,
        unreadCount: Int? = null,
    ) =
        viewModelScope.launch {
            val current = uiState.value
            if (current.isSummarizingUnread) return@launch

            val boundedMessages = when {
                requestedMessages != null -> requestedMessages
                unreadCount != null -> current.messages.take(
                    unreadCount.coerceIn(0, MAX_SUMMARY_MESSAGE_COUNT)
                )
                else -> current.unreadMessagesAtEntry
            }
            val targetMessages = boundedMessages
                .take(MAX_SUMMARY_MESSAGE_COUNT)
                .asReversed()

            if (targetMessages.isEmpty()) {
                updateState {
                    copy(
                        isSummarizingUnread = false,
                        unreadSummary = null,
                        unreadSummaryError = AppStrings.CHAT_AI_EMPTY_MESSAGES,
                        summarizedMessageCount = 0,
                    )
                }
                return@launch
            }

            lastSummaryMessages = targetMessages.asReversed()

            updateState {
                copy(
                    isSummarizingUnread = true,
                    aiDownloadPercent = null,
                    unreadSummary = null,
                    unreadSummaryError = null,
                    summarizedMessageCount = targetMessages.size,
                )
            }

            // 초기 상태 확인과 클릭이 경합해도 요청을 버리지 않고 시트 안에서 확인한다.
            val aiStatus = uiState.value.aiFeatureStatus ?: checkAiFeatureStatusUseCase().also { status ->
                updateState { copy(aiFeatureStatus = status) }
            }
            if (!aiStatus.isUsable) {
                updateState {
                    copy(
                        isSummarizingUnread = false,
                        aiDownloadPercent = null,
                        unreadSummaryError = AppStrings.CHAT_AI_UNAVAILABLE,
                    )
                }
                return@launch
            }

            when (
                val result = summarizeUnreadChatUseCase(targetMessages) { percent ->
                    updateState { copy(aiDownloadPercent = percent) }
                }
            ) {
                is ApiState.Success -> updateState {
                    copy(
                        isSummarizingUnread = false,
                        aiDownloadPercent = null,
                        unreadSummary = result.data,
                        unreadSummaryError = null,
                    )
                }

                is ApiState.Fail -> {
                    updateState {
                        copy(
                            isSummarizingUnread = false,
                            aiDownloadPercent = null,
                            unreadSummaryError = result.failState.message,
                        )
                    }
                }
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

    fun sendText(
        content: String,
        mentionedMemberIds: List<Long> = emptyList(),
        replyToId: String? = null,
    ) {
        val trimmed = content.trim()
        if (trimmed.isEmpty()) return
        if (trimmed.codePointCount(0, trimmed.length) > COMMUNITY_CHAT_MAX_MESSAGE_LENGTH) {
            emitEvent(CommunityChattingEvent.ShowError(AppStrings.CHAT_MESSAGE_TOO_LONG))
            return
        }
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
                                error = throwable.message.orEmpty().ifBlank { AppStrings.CHAT_SEND_MESSAGE_FAILED },
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

    fun sendImages(uriStrings: List<String>) = viewModelScope.launch {
        val uploadUris = uriStrings
            .filter(String::isNotBlank)
            .distinct()
            .take(COMMUNITY_CHAT_MAX_IMAGE_COUNT)
        if (uploadUris.isEmpty()) return@launch
        val clientMessageId = UUID.randomUUID().toString()
        updateState {
            copy(
                pendingMessages = pendingMessages + (
                    clientMessageId to PendingCommunityMessage(
                        clientMessageId = clientMessageId,
                        commandId = "",
                        content = "",
                        type = CommunityMessageType.IMAGE,
                        localUris = uploadUris,
                    )
                )
            )
        }

        val uploads = coroutineScope {
            uploadUris.map { uri ->
                async { uploadFileUseCase(uri, UploadFileCategory.ETC) }
            }.awaitAll()
        }
        val uploadFailure = uploads.filterIsInstance<ApiState.Fail>().firstOrNull()
        if (uploadFailure != null) {
            markImagePendingFailed(clientMessageId, uploadFailure.failState.message)
            return@launch
        }

        val fileIds = uploads.map { (it as ApiState.Success).data.fileId }
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

    private fun markImagePendingFailed(clientMessageId: String, message: String?) = updateState {
        val pending = pendingMessages[clientMessageId] ?: return@updateState this
        copy(
            pendingMessages = pendingMessages + (
                clientMessageId to pending.copy(
                    error = message.orEmpty().ifBlank { AppStrings.CHAT_SEND_IMAGE_FAILED },
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
                                error = AppStrings.CHAT_RETRY_DISCONNECTED,
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
                sendImages(pending.localUris)
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
            is ApiState.Success -> emitEvent(CommunityChattingEvent.ThreadDeleted)
            is ApiState.Fail -> {
                if (result.failState.code == OWNERSHIP_TRANSFER_REQUIRED_CODE) {
                    emitEvent(CommunityChattingEvent.ShowError(result.failState.message))
                    updateState { copy(showOwnershipTransferRequiredDialog = true) }
                } else {
                    emitEvent(CommunityChattingEvent.ShowError(result.failState.message))
                }
            }
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

    fun transferOwnership(memberId: String) = viewModelScope.launch {
        val state = uiState.value
        if (state.thread?.myRole != CommunityThreadRole.OWNER || memberId == state.myMemberId) return@launch

        when (
            val result = threadRepository.changeMemberRole(
                threadId = state.threadId,
                memberId = memberId,
                role = CommunityThreadRole.OWNER,
            )
        ) {
            is ApiState.Success -> updateState {
                copy(
                    members = members.mapValues { (id, member) ->
                        when (id) {
                            memberId -> member.copy(role = CommunityThreadRole.OWNER)
                            myMemberId -> member.copy(role = CommunityThreadRole.MEMBER)
                            else -> member
                        }
                    },
                    thread = thread?.copy(myRole = CommunityThreadRole.MEMBER),
                )
            }
            is ApiState.Fail -> emitEvent(CommunityChattingEvent.ShowError(result.failState.message))
        }
    }

    private fun openDeleteThreadDialog() {
        if (uiState.value.thread?.myRole != CommunityThreadRole.OWNER) return
        updateState { copy(showDeleteDialog = true) }
    }

    private fun dismissDeleteThreadDialog() {
        if (uiState.value.isDeleting) return
        updateState { copy(showDeleteDialog = false) }
    }

    private fun deleteThread() = viewModelScope.launch {
        val state = uiState.value
        if (state.thread?.myRole != CommunityThreadRole.OWNER || state.isDeleting) return@launch

        updateState { copy(showDeleteDialog = false, isDeleting = true) }
        when (val result = threadRepository.deleteThread(state.threadId)) {
            is ApiState.Success -> emitEvent(CommunityChattingEvent.ThreadUnavailable)
            is ApiState.Fail -> {
                updateState { copy(isDeleting = false) }
                emitEvent(CommunityChattingEvent.ShowError(result.failState.message))
            }
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
            repeat(MAX_RECONNECT_ATTEMPTS) { attempt ->
                if (!isActive || chatRepository.connectionState.value == CommunityChatConnectionState.CONNECTED) {
                    return@launch
                }
                chatRepository.disconnect()
                runCatching { chatRepository.connect() }

                val result = withTimeoutOrNull(8_000) {
                    chatRepository.connectionState.first {
                        it != CommunityChatConnectionState.CONNECTING
                    }
                }
                val connected = result == CommunityChatConnectionState.CONNECTED
                if (connected) return@launch

                if (attempt < MAX_RECONNECT_ATTEMPTS - 1) delay(retryDelayMillis)
                retryDelayMillis = (retryDelayMillis * 2).coerceAtMost(15_000L)
            }
            emitEvent(CommunityChattingEvent.ShowError(AppStrings.CHAT_RETRY_DISCONNECTED))
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
                    pending?.localUris?.isNotEmpty() == true
                ) {
                    event.message.copy(content = pending.localUris.first())
                } else {
                    event.message
                }
                val withoutOld = messages.filterNot { it.messageId == receivedMessage.messageId }
                copy(
                    messages = listOf(receivedMessage) + withoutOld,
                    localImageUrisByMessageId = if (pending?.localUris?.isNotEmpty() == true) {
                        localImageUrisByMessageId + (receivedMessage.messageId to pending.localUris)
                    } else {
                        localImageUrisByMessageId
                    },
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
                CommunityChatEvent.ThreadStateChanged.Type.DELETED ->
                    emitEvent(CommunityChattingEvent.ThreadUnavailable)
                CommunityChatEvent.ThreadStateChanged.Type.MEMBER_KICKED,
                CommunityChatEvent.ThreadStateChanged.Type.MEMBER_LEFT -> {
                    if (event.memberId == uiState.value.myMemberId) {
                        emitEvent(CommunityChattingEvent.ThreadUnavailable)
                    } else {
                        event.memberId?.let { memberId ->
                            updateState {
                                copy(
                                    members = members - memberId,
                                    thread = event.memberCount?.let { count ->
                                        thread?.copy(memberCount = count)
                                    } ?: thread,
                                )
                            }
                        }
                        refreshDetail()
                        loadMembers()
                    }
                }
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

    fun refreshThread() {
        refreshDetail()
        loadMembers()
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

    private companion object {
        const val MAX_SUMMARY_MESSAGE_COUNT = 50
        const val MAX_RECONNECT_ATTEMPTS = 5
        const val OWNERSHIP_TRANSFER_REQUIRED_CODE = "COMMUNITY-0041"
    }
}

private fun com.umc.domain.model.base.FailState.hasThreadUnavailableCode(): Boolean =
    code == "404"

private fun createInitialChattingState(savedStateHandle: SavedStateHandle): CommunityChattingState {
    val threadId = savedStateHandle.get<String>("threadId").orEmpty()
    return CommunityChattingState(
        threadId = threadId,
        isThreadUnavailable = threadId.isBlank(),
    )
}
