package com.umc.presentation.community.chatting

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.umc.component.base.BaseViewModel
import com.umc.component.theme.AppStrings
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.base.isNetworkError
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
import com.umc.domain.model.community.thread.CommunityThreadMemberState
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
    private var realtimeObserverJob: Job? = null
    private var manualReconnectInProgress = false
    private var hasCapturedEntryUnreadMessages = false
    private var lastSummaryMessages: List<CommunityThreadMessage> = emptyList()
    private var accessGeneration = 0
    private var memberLoadGeneration = 0
    private var detailLoadGeneration = 0
    private var membershipMutationInProgress = false
    private var isLeaving = false
    private var hasEmittedThreadLeft = false

    init {
        observeCurrentMember()
        checkAiAvailability()
        if (state.value.threadId.isNotBlank()) {
            bootstrap()
        } else {
            stopRealtime()
        }
    }

    fun onAction(action: CommunityChattingAction) {
        when (action) {
            CommunityChattingAction.OnBackClick -> emitEvent(CommunityChattingEvent.NavigateBack)
            CommunityChattingAction.OnMoreClick -> emitEvent(CommunityChattingEvent.OpenMore)
            CommunityChattingAction.OnUnreadSummaryClick ->
                summarizeUnreadMessages()
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
        if (uiState.value.isLoading) return@launch
        val generation = accessGeneration
        updateState {
            copy(
                isLoading = true,
                errorMessage = null,
                isThreadUnavailable = false,
                isAccessDenied = false,
            )
        }
        val threadId = uiState.value.threadId
        if (threadId.isBlank()) {
            stopRealtime()
            updateState { copy(isLoading = false, isThreadUnavailable = true) }
            return@launch
        }
        val (detail, messages) = coroutineScope {
            val detailDeferred = async { threadRepository.getThread(threadId) }
            val messagesDeferred = async { threadRepository.getMessages(threadId) }
            detailDeferred.await() to messagesDeferred.await()
        }
        if (generation != accessGeneration) return@launch
        if (detail is ApiState.Success && detail.data.deletedAt != null) {
            showAccessDenied()
            return@launch
        }
        if (detail is ApiState.Success && messages is ApiState.Success) {
            val loadedMessages = messages.data.messages
                .distinctBy(CommunityThreadMessage::messageId)
            // 보기 전용(미참여·나간 스레드)은 읽음 처리를 보낼 수 없어 서버 unreadCount 가 나갈 때 값으로 남는다. 진입 미읽음으로 쓰지 않는다.
            val isViewOnly = detail.data.myRole == CommunityThreadRole.UNKNOWN
            val unreadCount = if (isViewOnly) {
                0
            } else {
                detail.data.unreadCount.toIntOrNull()?.coerceAtLeast(0) ?: 0
            }
            val entryUnreadMessages = loadedMessages
                .take(unreadCount.coerceAtMost(MAX_SUMMARY_MESSAGE_COUNT))
            val isInitialSummaryCandidateCapture = !hasCapturedEntryUnreadMessages
            updateState {
                copy(
                    thread = detail.data,
                    messages = loadedMessages,
                    unreadMessagesAtEntry = if (hasCapturedEntryUnreadMessages) {
                        unreadMessagesAtEntry
                    } else {
                        entryUnreadMessages
                    },
                    unreadCountAtEntry = if (hasCapturedEntryUnreadMessages) {
                        unreadCountAtEntry
                    } else {
                        unreadCount
                    },
                    summaryCandidateMessages = if (hasCapturedEntryUnreadMessages) {
                        summaryCandidateMessages
                    } else {
                        entryUnreadMessages
                    },
                    hasMore = messages.data.hasMore,
                    nextBefore = messages.data.nextBefore,
                    isLoading = false,
                )
            }
            hasCapturedEntryUnreadMessages = true
            if (isInitialSummaryCandidateCapture) {
                logSummaryCandidates(
                    reason = "CREATED",
                    messages = uiState.value.summaryCandidateMessages,
                )
            }
            if (detail.data.myRole != CommunityThreadRole.UNKNOWN) {
                loadMembers()
                observeRealtime()
                chatRepository.connect()
            } else {
                stopRealtime()
            }
        } else {
            val detailFailure = (detail as? ApiState.Fail)?.failState
            if (detailFailure != null && detailFailure.hasThreadUnavailableCode()) {
                showAccessDenied()
                return@launch
            }
            val messagesFailure = (messages as? ApiState.Fail)?.failState
            if (detailFailure?.hasThreadAccessDeniedCode() == true ||
                messagesFailure?.hasThreadAccessDeniedCode() == true
            ) {
                showAccessDenied()
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
    ) =
        viewModelScope.launch {
            val current = uiState.value
            if (current.isSummarizingUnread) return@launch

            val boundedMessages = when {
                requestedMessages != null -> requestedMessages
                else -> current.summaryCandidateMessages
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
                if (response.failState.hasThreadAccessDeniedCode()) {
                    showAccessDenied()
                    return@launch
                }
                updateState { copy(isLoadingMore = false) }
                emitEvent(CommunityChattingEvent.ShowError(response.failState.message))
            }
        }
    }

    fun sendText(
        content: String,
        mentionedMemberIds: List<Long> = emptyList(),
        replyToId: String? = null,
        clientMessageId: String = UUID.randomUUID().toString(),
        clearDraft: Boolean = true,
    ) {
        val trimmed = content.trim()
        if (uiState.value.thread == null || uiState.value.thread?.myRole == CommunityThreadRole.UNKNOWN || uiState.value.isAccessDenied) return
        if (trimmed.isEmpty()) return
        if (isCommunityChatMessageTooLong(trimmed)) {
            emitEvent(CommunityChattingEvent.ShowError(AppStrings.CHAT_MESSAGE_TOO_LONG))
            return
        }
        if (chatRepository.connectionState.value != CommunityChatConnectionState.CONNECTED) {
            // 연결이 끊긴 상태에서는 바로 실패시키지 않고 재연결을 시도한 뒤 전송한다(재전송 경로 재사용).
            updateState {
                copy(
                    draft = if (clearDraft) "" else draft,
                    pendingMessages = pendingMessages + (
                        clientMessageId to PendingCommunityMessage(
                            clientMessageId = clientMessageId,
                            commandId = "",
                            content = trimmed,
                            type = CommunityMessageType.TEXT,
                            mentionedMemberIds = mentionedMemberIds.distinct(),
                            replyToId = replyToId,
                        )
                    ),
                )
            }
            retryPendingMessage(clientMessageId)
            return
        }
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
                        draft = if (clearDraft) "" else draft,
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
                scheduleAckTimeout(clientMessageId)
            }
            .onFailure {
                updateState {
                    copy(
                        draft = if (clearDraft) "" else draft,
                        pendingMessages = pendingMessages + (
                            clientMessageId to PendingCommunityMessage(
                                clientMessageId = clientMessageId,
                                commandId = "",
                                content = trimmed,
                                type = CommunityMessageType.TEXT,
                                error = AppStrings.CHAT_SEND_MESSAGE_FAILED,
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
        if (uiState.value.thread == null || uiState.value.thread?.myRole == CommunityThreadRole.UNKNOWN || uiState.value.isAccessDenied) return@launch
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
        if (chatRepository.connectionState.value != CommunityChatConnectionState.CONNECTED && !ensureConnected()) {
            markImagePendingFailed(clientMessageId, AppStrings.CHAT_RETRY_DISCONNECTED)
            emitEvent(CommunityChattingEvent.ShowError(AppStrings.CHAT_RETRY_DISCONNECTED))
            return@launch
        }
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
                scheduleAckTimeout(clientMessageId)
            }
            .onFailure { markImagePendingFailed(clientMessageId, null) }
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
            .onFailure { lastReadMessageIdSent = null }
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

        if (!ensureConnected()) {
            updateState {
                copy(
                    pendingMessages = pendingMessages + (
                        clientMessageId to pending.copy(
                            error = AppStrings.CHAT_RETRY_DISCONNECTED,
                        )
                    )
                )
            }
            emitEvent(CommunityChattingEvent.ShowError(AppStrings.CHAT_RETRY_DISCONNECTED))
            return@launch
        }

        updateState { copy(pendingMessages = pendingMessages - clientMessageId) }
        if (pending.type == CommunityMessageType.IMAGE) {
            if (pending.fileMetadataIds.isEmpty()) {
                sendImages(pending.localUris)
            } else {
                // 소켓에 한 번 실린 메시지는 같은 clientMessageId 로 다시 보내 서버 중복 제거가 동작하게 한다.
                val newClientMessageId = if (pending.commandId.isNotBlank()) {
                    clientMessageId
                } else {
                    UUID.randomUUID().toString()
                }
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
                        scheduleAckTimeout(newClientMessageId)
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
                clientMessageId = if (pending.commandId.isNotBlank()) {
                    clientMessageId
                } else {
                    UUID.randomUUID().toString()
                },
                clearDraft = false,
            )
        }
    }

    /** 연결돼 있으면 true, 아니면 재연결을 시도해 최대 7초 기다린다. 끝내 실패하면 자동 재연결 루프를 시작하고 false. */
    private suspend fun ensureConnected(): Boolean {
        if (chatRepository.connectionState.value == CommunityChatConnectionState.CONNECTED) return true
        val startedHere = !manualReconnectInProgress
        if (startedHere) {
            reconnectJob?.cancel()
            reconnectJob = null
            manualReconnectInProgress = true
            chatRepository.disconnect()
            runCatching { chatRepository.connect() }
        }
        val connected = withTimeoutOrNull(MANUAL_RECONNECT_TIMEOUT_MILLIS) {
            chatRepository.connectionState.first {
                it == CommunityChatConnectionState.CONNECTED
            }
        } != null
        if (startedHere) manualReconnectInProgress = false
        if (!connected) ensureReconnectLoop()
        return connected
    }

    /** 서버 확인(ack)이 오지 않은 전송 중 메시지를 실패 처리한다. clientMessageId 가 null 이면 대상 전체. */
    private fun failUnacknowledgedPending(clientMessageId: String? = null) = updateState {
        copy(
            pendingMessages = pendingMessages.mapValues { (id, pending) ->
                if (
                    (clientMessageId == null || id == clientMessageId) &&
                    pending.commandId.isNotBlank() &&
                    !pending.acknowledged &&
                    pending.error == null
                ) {
                    pending.copy(
                        error = if (pending.type == CommunityMessageType.IMAGE) {
                            AppStrings.CHAT_SEND_IMAGE_FAILED
                        } else {
                            AppStrings.CHAT_SEND_MESSAGE_FAILED
                        },
                    )
                } else {
                    pending
                }
            },
        )
    }

    private fun scheduleAckTimeout(clientMessageId: String) {
        viewModelScope.launch {
            delay(PENDING_ACK_TIMEOUT_MILLIS)
            failUnacknowledgedPending(clientMessageId)
        }
    }

    fun dismissPendingMessage(clientMessageId: String) =
        updateState { copy(pendingMessages = pendingMessages - clientMessageId) }

    fun toggleMuted() = viewModelScope.launch {
        val state = uiState.value
        when (val result = threadRepository.setMuted(state.threadId, !(state.thread?.isMuted ?: false))) {
            is ApiState.Success -> {
                if (result.data.deletedAt != null) showAccessDenied()
                else updateState { copy(thread = result.data) }
            }
            is ApiState.Fail -> emitEvent(CommunityChattingEvent.ShowError(result.failState.message))
        }
    }

    fun togglePinned() = viewModelScope.launch {
        val state = uiState.value
        when (val result = threadRepository.setPinned(state.threadId, !(state.thread?.isPinned ?: false))) {
            is ApiState.Success -> {
                if (result.data.deletedAt != null) showAccessDenied()
                else updateState { copy(thread = result.data) }
            }
            is ApiState.Fail -> emitEvent(CommunityChattingEvent.ShowError(result.failState.message))
        }
    }

    // 나가기 완료는 HTTP 응답과 소켓 member.left 중 먼저 온 쪽에서 한 번만 알린다.
    private fun emitThreadLeftOnce() {
        if (hasEmittedThreadLeft) return
        hasEmittedThreadLeft = true
        emitEvent(CommunityChattingEvent.ThreadDeleted)
    }

    fun leaveThread() = viewModelScope.launch {
        if (isLeaving) return@launch
        isLeaving = true
        val threadId = uiState.value.threadId
        val thread = uiState.value.thread
        // 나간 스레드는 더보기 메뉴가 없어 고정을 풀 수 없으므로 나가기 전에 먼저 푼다. 방장은 나가기가 거절되므로 건드리지 않는다.
        var unpinnedFirst = false
        if (thread?.isPinned == true && thread.myRole != CommunityThreadRole.OWNER) {
            when (val unpinned = threadRepository.setPinned(threadId, false)) {
                is ApiState.Success -> {
                    unpinnedFirst = true
                    updateState { copy(thread = unpinned.data) }
                }
                is ApiState.Fail -> if (unpinned.failState.isNetworkError) {
                    // 네트워크가 불안정하면 고정이 서버에 남은 채 나가질 수 있어 나가기를 보내지 않는다.
                    emitEvent(CommunityChattingEvent.ShowError(unpinned.failState.message))
                    isLeaving = false
                    return@launch
                }
            }
        }
        when (val result = threadRepository.leaveThread(threadId)) {
            is ApiState.Success -> emitThreadLeftOnce()
            is ApiState.Fail -> {
                // 소켓 member.left 로 나가기가 이미 확정됐으면(요청 재전송 등으로 늦게 온 실패) 재고정·오류 토스트를 건너뛴다.
                if (hasEmittedThreadLeft) return@launch
                // 네트워크 오류는 나가기 성공 여부를 알 수 없으므로 고정을 되돌리지 않는다.
                if (unpinnedFirst && !result.failState.isNetworkError) {
                    when (val restored = threadRepository.setPinned(threadId, true)) {
                        is ApiState.Success -> updateState { copy(thread = restored.data) }
                        is ApiState.Fail -> Unit
                    }
                }
                if (result.failState.code == OWNERSHIP_TRANSFER_REQUIRED_CODE) {
                    emitEvent(CommunityChattingEvent.ShowError(result.failState.message))
                    updateState { copy(showOwnershipTransferRequiredDialog = true) }
                } else {
                    emitEvent(CommunityChattingEvent.ShowError(result.failState.message))
                }
                isLeaving = false
            }
        }
    }

    fun kickMember(memberId: String) = viewModelScope.launch {
        val state = uiState.value
        if (membershipMutationInProgress || state.thread?.myRole !in setOf(CommunityThreadRole.OWNER, CommunityThreadRole.ADMIN) || memberId == state.myMemberId || state.members[memberId]?.role == CommunityThreadRole.OWNER) return@launch
        membershipMutationInProgress = true
        try {
            when (val result = threadRepository.kickMember(state.threadId, memberId)) {
                is ApiState.Success -> updateState {
                    copy(
                        members = members - memberId,
                        thread = thread?.copy(memberCount = result.data.memberCount),
                    )
                }
                is ApiState.Fail -> emitEvent(CommunityChattingEvent.ShowError(result.failState.message))
            }
        } finally {
            membershipMutationInProgress = false
            refreshThread()
        }
    }

    fun transferOwnership(memberId: String) = viewModelScope.launch {
        val state = uiState.value
        if (membershipMutationInProgress || state.thread?.myRole != CommunityThreadRole.OWNER || memberId == state.myMemberId || state.members[memberId]?.state != CommunityThreadMemberState.ACTIVE) return@launch
        membershipMutationInProgress = true
        try {
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
                                myMemberId -> member.copy(role = CommunityThreadRole.ADMIN)
                                else -> member
                            }
                        },
                        thread = thread?.copy(myRole = CommunityThreadRole.ADMIN),
                    )
                }
                is ApiState.Fail -> emitEvent(CommunityChattingEvent.ShowError(result.failState.message))
            }
        } finally {
            membershipMutationInProgress = false
            refreshThread()
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
            is ApiState.Success -> showAccessDenied()
            is ApiState.Fail -> {
                updateState { copy(isDeleting = false) }
                emitEvent(CommunityChattingEvent.ShowError(result.failState.message))
            }
        }
    }

    fun updateDraft(value: String) = updateState { copy(draft = value) }

    private fun runCommand(block: () -> String) {
        if (uiState.value.thread == null || uiState.value.thread?.myRole == CommunityThreadRole.UNKNOWN || uiState.value.isAccessDenied) return
        runCatching(block).onFailure {
            // 예외 원문(영문)을 노출하지 않고 고정 문구를 쓴다.
            val message = if (chatRepository.connectionState.value != CommunityChatConnectionState.CONNECTED) {
                AppStrings.CHAT_ACTION_DISCONNECTED
            } else {
                AppStrings.CHAT_ACTION_FAILED
            }
            emitEvent(CommunityChattingEvent.ShowError(message))
        }
    }

    private fun observeRealtime() {
        if (realtimeObserverJob?.isActive == true) return
        realtimeObserverJob = viewModelScope.launch {
            coroutineScope {
                launch {
                    chatRepository.connectionState.collectLatest { connection ->
                        updateState { copy(connectionState = connection) }
                        if (connection == CommunityChatConnectionState.CONNECTED) {
                            reconnectJob?.cancel()
                            reconnectJob = null
                            refreshThread()
                            reconcileLatestMessages()
                            markLatestMessageRead()
                        } else if (connection == CommunityChatConnectionState.DISCONNECTED) {
                            // 끊기기 전에 보낸 미확인 메시지는 더 이상 확인이 오지 않으므로 실패 처리한다.
                            failUnacknowledgedPending()
                            if (!manualReconnectInProgress) ensureReconnectLoop()
                        }
                    }
                }
                launch {
                    chatRepository.events.collect { event ->
                        if (event.threadId != uiState.value.threadId || !rememberEvent(event.eventId)) return@collect
                        reduce(event)
                    }
                }
                launch {
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
                        // 자동/수동 재연결 중 시도마다 반복되는 소켓 연결 오류 토스트는 숨긴다(종료 시 별도 안내).
                        val isReconnectNoise = error.code == WS_CONNECTION_ERROR_CODE &&
                            (manualReconnectInProgress || reconnectJob?.isActive == true)
                        if (!isReconnectNoise) {
                            emitEvent(CommunityChattingEvent.ShowError(error.message))
                        }
                    }
                }
            }
        }
    }

    private fun stopRealtime() {
        reconnectJob?.cancel()
        reconnectJob = null
        realtimeObserverJob?.cancel()
        realtimeObserverJob = null
        manualReconnectInProgress = false
        chatRepository.disconnect()
    }

    // 내보내진 계정 등 접근 권한이 없으면 남은 내용을 지우고 전용 화면만 보여준다.
    private fun showAccessDenied() {
        accessGeneration++
        stopRealtime()
        updateState {
            copy(
                isLoading = false,
                isLoadingMore = false,
                errorMessage = null,
                isAccessDenied = true,
                thread = null,
                messages = emptyList(),
                members = emptyMap(),
                localImageUrisByMessageId = emptyMap(),
                readWatermarks = emptyMap(),
                unreadSummary = null,
                showDeleteDialog = false,
                showOwnershipTransferRequiredDialog = false,
                pendingMessages = emptyMap(),
                unreadMessagesAtEntry = emptyList(),
                unreadCountAtEntry = 0,
                summaryCandidateMessages = emptyList(),
                draft = "",
            )
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
        val loadGeneration = ++memberLoadGeneration
        val generation = accessGeneration
        val result = threadRepository.getMembers(uiState.value.threadId, limit = 100)
        if (generation != accessGeneration || loadGeneration != memberLoadGeneration) return@launch
        when (result) {
            is ApiState.Success -> {
                val members = result.data.items
                val me = members.firstOrNull { it.memberId == uiState.value.myMemberId }
                if (me?.state == CommunityThreadMemberState.KICKED) {
                    showAccessDenied()
                    return@launch
                }
                if (me != null) updateState {
                    copy(thread = thread?.copy(myRole = if (me.state == CommunityThreadMemberState.ACTIVE) me.role else CommunityThreadRole.UNKNOWN))
                }
                updateState { copy(members = members.filter { it.state == CommunityThreadMemberState.ACTIVE }.associateBy(CommunityThreadMember::memberId)) }
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
                if (generation != accessGeneration || loadGeneration != memberLoadGeneration) return@launch
                updateState {
                    copy(members = membersWithProfiles.filter { it.state == CommunityThreadMemberState.ACTIVE }.associateBy(CommunityThreadMember::memberId))
                }
            }
            is ApiState.Fail -> if (result.failState.hasThreadAccessDeniedCode() || result.failState.hasThreadUnavailableCode()) showAccessDenied()
        }
    }

    private fun rememberEvent(eventId: String): Boolean {
        if (!processedEventIds.add(eventId)) return false
        if (processedEventIds.size > 500) processedEventIds.remove(processedEventIds.first())
        return true
    }

    private fun ensureReconnectLoop() {
        if (uiState.value.isAccessDenied || uiState.value.isThreadUnavailable || uiState.value.thread == null || uiState.value.thread?.myRole == CommunityThreadRole.UNKNOWN) return
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
                // 수정·삭제 이벤트는 제자리에서 갱신하고, 새 메시지(CREATED)만 맨 앞(최신)에 넣는다.
                val updatedMessages = when {
                    messages.any { it.messageId == receivedMessage.messageId } ->
                        messages.map { if (it.messageId == receivedMessage.messageId) receivedMessage else it }
                    event.type == CommunityChatEvent.MessageChanged.Type.CREATED ->
                        listOf(receivedMessage) + messages
                    else -> messages
                }
                val isOwnMessage = receivedMessage.senderId == myMemberId || pending != null
                val updatedSummaryCandidates = when (event.type) {
                    CommunityChatEvent.MessageChanged.Type.CREATED -> {
                        if (isOwnMessage) {
                            summaryCandidateMessages
                        } else {
                            (listOf(receivedMessage) + summaryCandidateMessages)
                                .distinctBy(CommunityThreadMessage::messageId)
                                .take(MAX_SUMMARY_MESSAGE_COUNT)
                        }
                    }
                    CommunityChatEvent.MessageChanged.Type.UPDATED -> {
                        summaryCandidateMessages.map { message ->
                            if (message.messageId == receivedMessage.messageId) receivedMessage else message
                        }
                    }
                    CommunityChatEvent.MessageChanged.Type.DELETED -> {
                        summaryCandidateMessages.filterNot { message ->
                            message.messageId == receivedMessage.messageId
                        }
                    }
                }
                copy(
                    messages = updatedMessages,
                    summaryCandidateMessages = updatedSummaryCandidates,
                    localImageUrisByMessageId = if (pending?.localUris?.isNotEmpty() == true) {
                        localImageUrisByMessageId + (receivedMessage.messageId to pending.localUris)
                    } else {
                        localImageUrisByMessageId
                    },
                    pendingMessages = event.clientMessageId?.let { pendingMessages - it } ?: pendingMessages,
                )
            }.also {
                logSummaryCandidates(
                    reason = "MESSAGE_${event.type.name}",
                    messages = uiState.value.summaryCandidateMessages,
                )
                markRead(event.message.messageId)
            }
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
                    showAccessDenied()
                CommunityChatEvent.ThreadStateChanged.Type.MEMBER_KICKED,
                CommunityChatEvent.ThreadStateChanged.Type.MEMBER_LEFT -> {
                    if (event.memberId == uiState.value.myMemberId) {
                        // 내가 요청한 나가기의 member.left 가 HTTP 응답보다 먼저 와도 나가기 완료로 처리한다(토스트 누락 방지).
                        if (isLeaving && event.type == CommunityChatEvent.ThreadStateChanged.Type.MEMBER_LEFT) {
                            emitThreadLeftOnce()
                        } else {
                            showAccessDenied()
                        }
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
        val loadGeneration = ++detailLoadGeneration
        val generation = accessGeneration
        val result = threadRepository.getThread(uiState.value.threadId)
        if (generation != accessGeneration || loadGeneration != detailLoadGeneration) return@launch
        when (result) {
            is ApiState.Success -> {
                if (result.data.deletedAt != null) showAccessDenied()
                else updateState { copy(thread = result.data) }
            }
            is ApiState.Fail -> if (result.failState.hasThreadAccessDeniedCode() || result.failState.hasThreadUnavailableCode()) showAccessDenied()
        }
    }

    fun refreshThread() {
        if (uiState.value.isAccessDenied) return
        refreshDetail()
        loadMembers()
    }

    private suspend fun reconcileLatestMessages() {
        when (val result = threadRepository.getMessages(uiState.value.threadId)) {
            is ApiState.Success -> {
                updateState {
                    val existingMessageIds = messages.mapTo(hashSetOf()) { it.messageId }
                    val latestMessageIds = result.data.messages.mapTo(hashSetOf()) { it.messageId }
                    // 현재 목록의 가장 오래된 메시지가 최신 페이지 밖이면 이미 이전 페이지를 불러온 상태라 커서를 유지한다.
                    val hasLoadedOlderPages = messages.lastOrNull()
                        ?.let { it.messageId !in latestMessageIds } == true
                    val newlyReceivedMessages = result.data.messages.filter { message ->
                        message.messageId !in existingMessageIds && message.senderId != myMemberId
                    }
                    // 서버에 이미 도착한 내 메시지는 '전송 실패/전송 중' 말풍선이 중복으로 남지 않게 pending 에서 뺀다.
                    val deliveredClientIds = result.data.messages.mapNotNullTo(hashSetOf()) { it.clientMessageId }
                    copy(
                        pendingMessages = pendingMessages.filterKeys { it !in deliveredClientIds },
                        messages = (result.data.messages + messages)
                            .distinctBy(CommunityThreadMessage::messageId),
                        summaryCandidateMessages = (
                            newlyReceivedMessages + summaryCandidateMessages
                        )
                            .distinctBy(CommunityThreadMessage::messageId)
                            .take(MAX_SUMMARY_MESSAGE_COUNT),
                        hasMore = if (hasLoadedOlderPages) hasMore else result.data.hasMore,
                        nextBefore = if (hasLoadedOlderPages) nextBefore else result.data.nextBefore,
                    )
                }
                logSummaryCandidates(
                    reason = "RECONNECT_RECONCILED",
                    messages = uiState.value.summaryCandidateMessages,
                )
                markLatestMessageRead()
            }
            is ApiState.Fail -> if (result.failState.hasThreadAccessDeniedCode()) showAccessDenied()
        }
    }

    private fun markLatestMessageRead() {
        uiState.value.messages.firstOrNull()?.messageId?.let(::markRead)
    }

    private fun logSummaryCandidates(
        reason: String,
        messages: List<CommunityThreadMessage>,
    ) {
        Log.d(
            SUMMARY_LOG_TAG,
            "reason=$reason, candidateCount=${messages.size}",
        )
    }

    override fun onCleared() {
        stopRealtime()
        super.onCleared()
    }

    private companion object {
        const val MAX_SUMMARY_MESSAGE_COUNT = 50
        const val MAX_RECONNECT_ATTEMPTS = 5
        const val MANUAL_RECONNECT_TIMEOUT_MILLIS = 7_000L
        const val PENDING_ACK_TIMEOUT_MILLIS = 10_000L
        const val WS_CONNECTION_ERROR_CODE = "WS-CONNECTION"
        const val OWNERSHIP_TRANSFER_REQUIRED_CODE = "COMMUNITY-0041"
        const val SUMMARY_LOG_TAG = "CHAT_SUMMARY_CANDIDATES"
    }
}

private const val THREAD_NOT_FOUND_CODE = "COMMUNITY-0033"
private const val THREAD_DELETED_CODE = "COMMUNITY-0037"
private const val THREAD_ACCESS_DENIED_CODE = "COMMUNITY-0034"

private fun com.umc.domain.model.base.FailState.hasThreadUnavailableCode(): Boolean =
    code == "404" || code.endsWith("-404") ||
        code == THREAD_NOT_FOUND_CODE || code == THREAD_DELETED_CODE

private fun com.umc.domain.model.base.FailState.hasThreadAccessDeniedCode(): Boolean =
    code == "403" || code.endsWith("-403") || code == THREAD_ACCESS_DENIED_CODE

private fun createInitialChattingState(savedStateHandle: SavedStateHandle): CommunityChattingState {
    val threadId = savedStateHandle.get<String>("threadId").orEmpty()
    return CommunityChattingState(
        threadId = threadId,
        isThreadUnavailable = threadId.isBlank(),
    )
}
