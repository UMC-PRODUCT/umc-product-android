package com.umc.presentation.community.chatting

import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.view.WindowManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.emoji2.emojipicker.EmojiPickerView
import coil.compose.AsyncImage
import com.umc.component.R
import com.umc.component.component.DialogType
import com.umc.component.component.UBasicDialog
import com.umc.component.component.UButton
import com.umc.component.component.UDialog
import com.umc.component.component.UToastData
import com.umc.component.component.UToastHost
import com.umc.component.theme.AppStrings
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.grey100
import com.umc.component.theme.grey200
import com.umc.component.theme.grey300
import com.umc.component.theme.grey400
import com.umc.component.theme.grey800
import com.umc.component.theme.grey950
import com.umc.component.theme.indigo100
import com.umc.component.theme.indigo200
import com.umc.component.theme.indigo300
import com.umc.component.theme.indigo500
import com.umc.component.theme.red400
import com.umc.component.theme.red100
import com.umc.component.theme.red500
import com.umc.component.theme.white
import com.umc.component.theme.grey50
import com.umc.component.theme.grey500
import com.umc.component.theme.grey600
import com.umc.component.theme.grey700
import com.umc.component.theme.indigo600
import com.umc.presentation.community.bottomsheet.ConversationSummaryBottomSheet
import com.umc.presentation.community.component.chatting.CommunityChatMemberTag
import com.umc.presentation.community.component.chatting.CommunityChatProfileImage
import com.umc.presentation.community.component.chatting.CommunityChatInputBar
import com.umc.presentation.community.component.chatting.communityChatPartTag
import com.umc.domain.model.community.thread.CommunityMessageReportReason
import com.umc.domain.model.community.thread.CommunityMessageType
import com.umc.domain.model.community.thread.CommunityReaction
import com.umc.domain.model.community.thread.CommunityThreadDetail
import com.umc.domain.model.community.thread.CommunityThreadMember
import com.umc.domain.model.community.thread.CommunityThreadMessage
import com.umc.domain.model.community.thread.CommunityThreadRole
import com.umc.domain.model.enums.UserPart
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.net.URI
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(
    androidx.compose.foundation.ExperimentalFoundationApi::class,
    ExperimentalLayoutApi::class,
    ExperimentalMaterial3Api::class,
)
@Composable
fun CommunityChattingScreen(
    state: CommunityChattingState,
    toastData: UToastData? = null,
    onToastDismiss: () -> Unit = {},
    onViewParticipantProfile: (String) -> Unit = {},
    onAction: (CommunityChattingAction) -> Unit,
) {
    val onBack = { onAction(CommunityChattingAction.OnBackClick) }
    val onMore = { onAction(CommunityChattingAction.OnMoreClick) }
    val onUnreadSummary = { onAction(CommunityChattingAction.OnUnreadSummaryClick) }
    val onCamera = { onAction(CommunityChattingAction.OnCameraClick) }
    val onSendImages: (List<String>) -> Unit = { onAction(CommunityChattingAction.OnSendImages(it)) }
    val onDraftChange: (String) -> Unit = { onAction(CommunityChattingAction.OnDraftChanged(it)) }
    val onSend: (String?) -> Unit = { onAction(CommunityChattingAction.OnSendClick(it)) }
    val onLoadPrevious = { onAction(CommunityChattingAction.OnLoadPrevious) }
    val onDeleteMessage: (String) -> Unit = { onAction(CommunityChattingAction.OnDeleteMessage(it)) }
    val onReact: (CommunityThreadMessage, String) -> Unit = { message, emoji ->
        onAction(CommunityChattingAction.OnReact(message, emoji))
    }
    val onReportMessage: (String, CommunityMessageReportReason) -> Unit = { messageId, reason ->
        onAction(CommunityChattingAction.OnReportMessage(messageId, reason))
    }
    val onToggleMuted = { onAction(CommunityChattingAction.OnToggleMuted) }
    val onTogglePinned = { onAction(CommunityChattingAction.OnTogglePinned) }
    val onLeave = { onAction(CommunityChattingAction.OnLeave) }
    val onRetryPending: (String) -> Unit = { onAction(CommunityChattingAction.OnRetryPending(it)) }
    val onDismissPending: (String) -> Unit = { onAction(CommunityChattingAction.OnDismissPending(it)) }
    val onRetryLoad = { onAction(CommunityChattingAction.OnRetryLoad) }
    val onInviteParticipants = { onAction(CommunityChattingAction.OnInviteParticipants) }
    val onEditThread = { onAction(CommunityChattingAction.OnEditThread) }
    val onDeleteThread = { onAction(CommunityChattingAction.OnDeleteThread) }
    val onKickMember: (String) -> Unit = { onAction(CommunityChattingAction.OnKickMember(it)) }
    val onTransferOwnership: (String) -> Unit = {
        onAction(CommunityChattingAction.OnTransferOwnership(it))
    }

    if (state.isThreadUnavailable) {
        ThreadUnavailableScreen(onBack = onBack)
        return
    }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val imeInsets = WindowInsets.ime
    val initialUnreadScrollOffset = with(density) {
        INITIAL_UNREAD_SCROLL_OFFSET.roundToPx()
    }
    val displayedMessages = remember(state.messages) { state.messages.asReversed() }
    var knownMessageIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var knownPendingMessageIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var newMessageCount by remember { mutableIntStateOf(0) }
    var unreadDividerMessageId by remember { mutableStateOf<String?>(null) }
    var followsLatestMessage by remember { mutableStateOf(true) }
    var wasUserScrolling by remember { mutableStateOf(false) }
    var selectedMessage by remember { mutableStateOf<CommunityThreadMessage?>(null) }
    var replyingMessage by remember { mutableStateOf<CommunityThreadMessage?>(null) }
    var emojiPickerMessage by remember { mutableStateOf<CommunityThreadMessage?>(null) }
    var pendingDeleteConfirmationId by remember { mutableStateOf<String?>(null) }
    var showLeaveDialog by remember { mutableStateOf(false) }
    var reportingMessageId by remember { mutableStateOf<String?>(null) }
    var showParticipants by remember { mutableStateOf(false) }
    var pendingReportConfirmation by remember {
        mutableStateOf<Pair<String, CommunityMessageReportReason>?>(null)
    }
    val clipboard = LocalClipboard.current
    val context = LocalContext.current
    DisposableEffect(context) {
        val window = context.findActivity()?.window
        val originalSoftInputMode = window?.attributes?.softInputMode
        window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

        onDispose {
            if (window != null && originalSoftInputMode != null) {
                window.setSoftInputMode(originalSoftInputMode)
            }
        }
    }
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(
            maxItems = COMMUNITY_CHAT_MAX_IMAGE_COUNT,
        ),
    ) { uris ->
        onSendImages(uris.take(COMMUNITY_CHAT_MAX_IMAGE_COUNT).map { it.toString() })
    }

    if (state.showOwnershipTransferRequiredDialog) {
        UDialog(
            title = AppStrings.CHAT_OWNERSHIP_REQUIRED_TITLE,
            subtitle = AppStrings.CHAT_OWNERSHIP_REQUIRED_DESCRIPTION,
            confirmText = AppStrings.CHAT_CONFIRM,
            confirmBackgroundColor = grey100(),
            confirmTextColor = grey700(),
            onConfirm = {
                onAction(CommunityChattingAction.OnDismissOwnershipTransferRequired)
            },
            onDismissRequest = {
                onAction(CommunityChattingAction.OnDismissOwnershipTransferRequired)
            },
        )
    }

    if (state.showDeleteDialog) {
        UBasicDialog(
            title = AppStrings.CHAT_DELETE_THREAD_TITLE,
            content = AppStrings.CHAT_DELETE_THREAD_DESCRIPTION,
            negativeText = AppStrings.CHAT_CANCEL,
            positiveText = AppStrings.CHAT_DELETE_ACTION,
            type = DialogType.ERROR,
            showCloseButton = false,
            negativeBackgroundColor = grey100(),
            negativeBorderColor = grey100(),
            negativeTextColor = grey600(),
            positiveBackgroundColor = red100(),
            positiveBorderColor = red100(),
            positiveTextColor = red500(),
            onNegative = { onAction(CommunityChattingAction.OnDismissDeleteThread) },
            onPositive = { onAction(CommunityChattingAction.OnConfirmDeleteThread) },
            onDismissRequest = { onAction(CommunityChattingAction.OnDismissDeleteThread) },
        )
    }

    if (
        state.isSummarizingUnread ||
        state.unreadSummary != null ||
        state.unreadSummaryError != null
    ) {
        ConversationSummaryBottomSheet(
            state = state,
            onRetry = { onAction(CommunityChattingAction.OnRetryUnreadSummary) },
            onDismiss = { onAction(CommunityChattingAction.OnDismissUnreadSummary) },
        )
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress to listState.canScrollForward }
            .collect { (isScrolling, canScrollForward) ->
                if (isScrolling) {
                    wasUserScrolling = true
                    followsLatestMessage = !canScrollForward
                } else if (wasUserScrolling) {
                    wasUserScrolling = false
                    followsLatestMessage = !canScrollForward
                    if (followsLatestMessage) {
                        newMessageCount = 0
                        unreadDividerMessageId = null
                    }
                }
            }
    }

    LaunchedEffect(state.messages) {
        val currentIds = state.messages.mapTo(linkedSetOf()) { it.messageId }
        if (knownMessageIds.isEmpty()) {
            knownMessageIds = currentIds
            if (displayedMessages.isNotEmpty()) {
                val entryUnreadCount = state.unreadCountAtEntry
                val loadedUnreadCount = entryUnreadCount.coerceAtMost(displayedMessages.size)
                val messageIndex = if (loadedUnreadCount > 0) {
                    val firstUnreadMessageIndex = displayedMessages.size - loadedUnreadCount
                    newMessageCount = entryUnreadCount
                    unreadDividerMessageId = displayedMessages[firstUnreadMessageIndex].messageId
                    followsLatestMessage = false
                    (firstUnreadMessageIndex - 1).coerceAtLeast(0)
                } else {
                    displayedMessages.lastIndex
                }
                val loadPreviousItemOffset = if (state.hasMore) 1 else 0
                listState.scrollToItem(
                    index = messageIndex + loadPreviousItemOffset,
                    scrollOffset = if (loadedUnreadCount > 0) -initialUnreadScrollOffset else 0,
                )
            }
            return@LaunchedEffect
        }

        val newMessages = state.messages.filter { it.messageId !in knownMessageIds }
        knownMessageIds = currentIds
        if (newMessages.isEmpty()) return@LaunchedEffect

        val containsMyMessage = newMessages.any { it.senderId == state.myMemberId }
        if (followsLatestMessage || containsMyMessage) {
            newMessageCount = 0
            unreadDividerMessageId = null
            listState.animateScrollToItem(
                latestListItemIndex(
                    messageCount = displayedMessages.size,
                    pendingMessageCount = state.pendingMessages.size,
                    hasMore = state.hasMore,
                ),
            )
        } else {
            if (newMessageCount == 0) {
                unreadDividerMessageId = newMessages
                    .lastOrNull { it.senderId != state.myMemberId }
                    ?.messageId
            }
            newMessageCount += newMessages.count { it.senderId != state.myMemberId }
        }
    }

    LaunchedEffect(state.pendingMessages.keys) {
        val currentIds = state.pendingMessages.keys.toSet()
        val hasNewPendingMessage = currentIds.any { it !in knownPendingMessageIds }
        knownPendingMessageIds = currentIds
        if (!hasNewPendingMessage) return@LaunchedEffect

        newMessageCount = 0
        unreadDividerMessageId = null
        followsLatestMessage = true
        val lastItemIndex = latestListItemIndex(
            messageCount = displayedMessages.size,
            pendingMessageCount = currentIds.size,
            hasMore = state.hasMore,
        )
        listState.animateScrollToItem(lastItemIndex)
    }

    LaunchedEffect(
        listState,
        displayedMessages.size,
        state.pendingMessages.size,
        state.hasMore,
    ) {
        snapshotFlow { imeInsets.getBottom(density) }
            .collectLatest { imeBottom ->
                if (imeBottom <= 0 || displayedMessages.isEmpty()) return@collectLatest

                // Wait until the IME animation and Scaffold bottom-bar remeasurement settle.
                delay(120)
                followsLatestMessage = true
                listState.animateScrollToItem(
                    latestListItemIndex(
                        messageCount = displayedMessages.size,
                        pendingMessageCount = state.pendingMessages.size,
                        hasMore = state.hasMore,
                    ),
                )
            }
    }

    Scaffold(
        containerColor = grey100(),
        snackbarHost = {
            UToastHost(
                data = toastData,
                onDismiss = onToastDismiss,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        },
        topBar = {
            ChatTopBar(
                title = state.thread?.title.orEmpty().ifBlank { AppStrings.CHAT_ROOM_DEFAULT },
                onBack = onBack,
                isMuted = state.thread?.isMuted == true,
                isPinned = state.thread?.isPinned == true,
                isOwner = state.thread?.myRole == CommunityThreadRole.OWNER,
                onSummary = onUnreadSummary,
                onToggleMuted = onToggleMuted,
                onTogglePinned = onTogglePinned,
                onParticipants = {
                    showParticipants = true
                    onMore()
                },
                onInviteParticipants = onInviteParticipants,
                onShareLink = {
                    state.thread?.shareUrl?.takeIf(String::isNotBlank)?.let { shareUrl ->
                        val deepLink = buildCommunityThreadDeepLink(shareUrl)
                        context.startActivity(
                            Intent.createChooser(
                                Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, deepLink)
                                },
                                AppStrings.CHAT_SHARE_CHOOSER_TITLE,
                            )
                        )
                    }
                },
                onLeave = { showLeaveDialog = true },
                onEditThread = onEditThread,
                onDeleteThread = onDeleteThread,
            )
        },
        bottomBar = {
            if (
                !state.isLoading &&
                state.errorMessage == null &&
                state.thread?.myRole != CommunityThreadRole.UNKNOWN
            ) {
                CommunityChatInputBar(
                    value = state.draft,
                    enabled = true,
                    replyingMessage = replyingMessage,
                    onValueChange = onDraftChange,
                    onCamera = {
                        onCamera()
                        imagePicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onSend = {
                        onSend(replyingMessage?.messageId)
                        replyingMessage = null
                    },
                )
            }
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (state.isLoading) {
                ChatLoadingSkeleton(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                )
            } else if (state.errorMessage != null) {
                ChatLoadError(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    onRetry = onRetryLoad,
                )
            } else if (state.messages.isEmpty() && state.pendingMessages.isEmpty()) {
                EmptyChatMessages(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(
                        start = 18.dp,
                        end = 18.dp,
                        top = 20.dp,
                        bottom = 18.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
            if (state.hasMore) {
                item {
                    TextButton(
                        onClick = onLoadPrevious,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            if (state.isLoadingMore) {
                                AppStrings.CHAT_LOADING_MORE
                            } else {
                                AppStrings.CHAT_LOAD_MORE
                            },
                        )
                    }
                }
            }

            itemsIndexed(
                items = displayedMessages,
                key = { _, message -> message.messageId },
            ) { index, message ->
                val isMine = message.senderId == state.myMemberId
                val messageDate = communityCreatedAtDate(message.createdAt)
                val previousDate = displayedMessages
                    .getOrNull(index - 1)
                    ?.createdAt
                    ?.let(::communityCreatedAtDate)

                Column {
                    if (index == 0 || messageDate != previousDate) {
                        DateDivider(formatCommunityDate(message.createdAt))
                        Spacer(Modifier.height(14.dp))
                    }
                    if (message.messageId == unreadDividerMessageId) {
                        NewMessagesDivider()
                        Spacer(Modifier.height(14.dp))
                    }
                    ChatMessageRow(
                        message = message,
                        imageUris = state.localImageUrisByMessageId[message.messageId].orEmpty(),
                        member = message.senderId?.let(state.members::get),
                        isMine = isMine,
                        menuExpanded = selectedMessage?.messageId == message.messageId,
                        onLongClick = { selectedMessage = message },
                        onDismissMenu = { selectedMessage = null },
                        onReply = {
                            replyingMessage = message
                            selectedMessage = null
                        },
                        onCopy = {
                            scope.launch {
                                clipboard.setClipEntry(
                                    ClipEntry(
                                        ClipData.newPlainText(
                                            AppStrings.CHAT_ACTION_COPY,
                                            message.content.orEmpty(),
                                        )
                                    )
                                )
                            }
                            selectedMessage = null
                        },
                        onReport = {
                            reportingMessageId = message.messageId
                            selectedMessage = null
                        },
                        onDelete = {
                            onDeleteMessage(message.messageId)
                            selectedMessage = null
                        },
                        onReact = { emoji ->
                            onReact(message, emoji)
                            selectedMessage = null
                        },
                        onShowMoreEmojis = {
                            emojiPickerMessage = message
                            selectedMessage = null
                        },
                    )
                }
            }

            items(
                state.pendingMessages.values.toList(),
                key = { it.clientMessageId },
            ) { pending ->
                PendingMessageRow(
                    message = pending,
                    onRetry = { onRetryPending(pending.clientMessageId) },
                    onDismiss = { pendingDeleteConfirmationId = pending.clientMessageId },
                )
                }
            }
            }

            if (newMessageCount > 0 && !state.isLoading && state.errorMessage == null) {
                UnreadSummaryCard(
                    unreadCount = newMessageCount,
                    onClick = onUnreadSummary,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(
                            start = 18.dp,
                            top = padding.calculateTopPadding() + 20.dp,
                            end = 18.dp,
                        ),
                )
                NewMessageButton(
                    count = newMessageCount,
                    onClick = {
                        newMessageCount = 0
                        unreadDividerMessageId = null
                        followsLatestMessage = true
                        scope.launch {
                            val lastItemIndex = latestListItemIndex(
                                messageCount = displayedMessages.size,
                                pendingMessageCount = state.pendingMessages.size,
                                hasMore = state.hasMore,
                            )
                            listState.animateScrollToItem(lastItemIndex)
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = padding.calculateBottomPadding() + 12.dp),
                )
            }
        }
    }

    emojiPickerMessage?.let { message ->
        AdditionalEmojiSheet(
            onDismiss = { emojiPickerMessage = null },
            onEmojiSelected = { emoji ->
                onReact(message, emoji)
                emojiPickerMessage = null
            },
        )
    }

    pendingDeleteConfirmationId?.let { clientMessageId ->
        UBasicDialog(
            title = AppStrings.CHAT_DELETE_MESSAGE_TITLE,
            negativeText = AppStrings.CHAT_CANCEL,
            positiveText = AppStrings.CHAT_DELETE_ACTION,
            type = DialogType.ERROR,
            showCloseButton = false,
            negativeBackgroundColor = grey100(),
            negativeBorderColor = grey100(),
            positiveBackgroundColor = red100(),
            positiveBorderColor = red100(),
            positiveTextColor = red500(),
            onPositive = {
                onDismissPending(clientMessageId)
                pendingDeleteConfirmationId = null
            },
            onNegative = { pendingDeleteConfirmationId = null },
            onDismissRequest = { pendingDeleteConfirmationId = null },
        )
    }

    if (showLeaveDialog) {
        UBasicDialog(
            title = AppStrings.CHAT_LEAVE_THREAD_TITLE,
            content = AppStrings.CHAT_LEAVE_THREAD_DESCRIPTION,
            negativeText = AppStrings.CHAT_CANCEL,
            positiveText = AppStrings.CHAT_LEAVE,
            type = DialogType.WARNING,
            showCloseButton = false,
            negativeBackgroundColor = grey100(),
            negativeBorderColor = grey100(),
            positiveBackgroundColor = red100(),
            positiveBorderColor = red100(),
            positiveTextColor = red500(),
            onPositive = {
                showLeaveDialog = false
                onLeave()
            },
            onNegative = { showLeaveDialog = false },
            onDismissRequest = { showLeaveDialog = false },
        )
    }

    reportingMessageId?.let { messageId ->
        MessageReportSheet(
            onDismiss = { reportingMessageId = null },
            onReport = { reason ->
                pendingReportConfirmation = messageId to reason
                reportingMessageId = null
            },
        )
    }

    pendingReportConfirmation?.let { (messageId, reason) ->
        UDialog(
            title = AppStrings.CHAT_REPORT_CONFIRM_TITLE,
            subtitle = AppStrings.CHAT_REPORT_CONFIRM_DESCRIPTION,
            isTwoButton = true,
            negativeText = AppStrings.CHAT_CANCEL,
            positiveText = AppStrings.CHAT_REPORT_ACTION,
            negativeBackgroundColor = grey100(),
            negativeBorderColor = grey100(),
            positiveBackgroundColor = red100(),
            positiveBorderColor = red100(),
            positiveTextColor = red500(),
            onNegative = { pendingReportConfirmation = null },
            onPositive = {
                onReportMessage(messageId, reason)
                pendingReportConfirmation = null
            },
            onDismissRequest = { pendingReportConfirmation = null },
        )
    }

    if (showParticipants) {
        CommunityParticipantScreen(
            members = state.members.values.toList(),
            memberCount = state.thread?.memberCount.orEmpty(),
            myMemberId = state.myMemberId,
            isOwner = state.thread?.myRole == CommunityThreadRole.OWNER,
            onBack = { showParticipants = false },
            onViewProfile = onViewParticipantProfile,
            onKickMember = onKickMember,
            onTransferOwnership = onTransferOwnership,
        )
    }
}

@Composable
private fun EmptyChatMessages(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_comment_filled),
            contentDescription = null,
            modifier = Modifier.size(28.dp),
            tint = grey400(),
        )
        Spacer(Modifier.height(14.dp))
        Text(
            text = AppStrings.CHAT_NO_MESSAGES_TITLE,
            color = grey800(),
            style = UmcTypographyTokens.HeadlineBold
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = AppStrings.CHAT_NO_MESSAGES_DESCRIPTION,
            color = grey400(),
            style = UmcTypographyTokens.Subheadline
        )
    }
}

@Composable
private fun ThreadUnavailableScreen(onBack: () -> Unit) {
    Scaffold(
        containerColor = grey100(),
        topBar = {
            Surface(color = white()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(72.dp)
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_back),
                            contentDescription = AppStrings.CHAT_CD_BACK,
                            tint = grey950(),
                        )
                    }
                    Text(
                        text = AppStrings.CHAT_UNAVAILABLE_TOP_BAR,
                        modifier = Modifier.padding(start = 4.dp),
                        color = grey950(),
                        style = UmcTypographyTokens.Title2Bold
                    )
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_block),
                contentDescription = null,
                modifier = Modifier.size(30.dp),
                tint = grey400(),
            )
            Spacer(Modifier.height(14.dp))
            Text(
                text = AppStrings.CHAT_UNAVAILABLE_TITLE,
                color = grey800(),
                style = UmcTypographyTokens.HeadlineBold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = AppStrings.CHAT_UNAVAILABLE_DESCRIPTION,
                color = grey400(),
                style = UmcTypographyTokens.Subheadline
            )
            Spacer(Modifier.height(34.dp))
            UButton(
                text = AppStrings.CHAT_BACK_TO_COMMUNITY,
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                backgroundColor = indigo500(),
                pressedColor = indigo600(),
                textColor = white(),
                textStyle = UmcTypographyTokens.HeadlineBold,
                cornerRadius = 8.dp,
            )
            Spacer(Modifier.height(100.dp))
        }
    }
}

@Composable
private fun ChatLoadingSkeleton(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "chat-loading")
    val shimmerX by transition.animateFloat(
        initialValue = -500f,
        targetValue = 1_200f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1_250, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "chat-loading-shimmer",
    )
    val brush = Brush.linearGradient(
        colors = listOf(
            grey200(),
            grey50(),
            grey200(),
        ),
        start = Offset(shimmerX - 260f, 0f),
        end = Offset(shimmerX, 260f),
    )

    Column(
        modifier = modifier.padding(horizontal = 24.dp, vertical = 34.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp),
    ) {
        ChatSkeletonIncoming(brush)
        ChatSkeletonMine(brush)
        ChatSkeletonIncoming(brush)
        ChatSkeletonMine(brush)
    }
}

@Composable
private fun ChatSkeletonIncoming(brush: Brush) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        SkeletonBlock(
            modifier = Modifier.size(54.dp),
            brush = brush,
            shape = CircleShape,
        )
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SkeletonBlock(
                modifier = Modifier
                    .width(148.dp)
                    .height(34.dp),
                brush = brush,
                shape = RoundedCornerShape(18.dp),
            )
            SkeletonBlock(
                modifier = Modifier
                    .fillMaxWidth(0.78f)
                    .height(74.dp),
                brush = brush,
                shape = RoundedCornerShape(18.dp),
            )
        }
    }
}

@Composable
private fun ChatSkeletonMine(brush: Brush) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
    ) {
        SkeletonBlock(
            modifier = Modifier
                .fillMaxWidth(0.68f)
                .height(74.dp),
            brush = brush,
            shape = RoundedCornerShape(18.dp),
        )
    }
}

@Composable
private fun SkeletonBlock(
    modifier: Modifier,
    brush: Brush,
    shape: androidx.compose.ui.graphics.Shape,
) {
    Box(modifier = modifier.background(brush = brush, shape = shape))
}

@Composable
private fun ChatLoadError(
    modifier: Modifier = Modifier,
    onRetry: () -> Unit,
) {
    Column(
        modifier = modifier.padding(horizontal = 32.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_community_error),
            contentDescription = null,
            modifier = Modifier.size(46.dp),
            tint = grey400(),
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = AppStrings.CHAT_LOAD_ERROR_TITLE,
            color = grey800(),
            style = UmcTypographyTokens.HeadlineBold
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = AppStrings.CHAT_LOAD_ERROR_DESCRIPTION,
            color = grey400(),
            style = UmcTypographyTokens.Subheadline
        )
        Spacer(Modifier.height(72.dp))
        UButton(
            text = AppStrings.CHAT_RETRY,
            onClick = onRetry,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            backgroundColor = indigo500(),
            pressedColor = indigo600(),
            textColor = white(),
            textStyle = UmcTypographyTokens.HeadlineBold,
            cornerRadius = 12.dp,
        )
    }
}

@Composable
private fun ChatTopBar(
    title: String,
    onBack: () -> Unit,
    isMuted: Boolean,
    isPinned: Boolean,
    isOwner: Boolean,
    onSummary: () -> Unit,
    onToggleMuted: () -> Unit,
    onTogglePinned: () -> Unit,
    onParticipants: () -> Unit,
    onInviteParticipants: () -> Unit,
    onShareLink: () -> Unit,
    onLeave: () -> Unit,
    onEditThread: () -> Unit,
    onDeleteThread: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Surface(color = white()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(72.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    painterResource(R.drawable.ic_back),
                    contentDescription = AppStrings.CHAT_CD_BACK,
                    tint = grey950(),
                )
            }
            Text(
                text = title,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
                color = grey950(),
                style = UmcTypographyTokens.Title2Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        painterResource(R.drawable.ic_menu_kebab),
                        contentDescription = AppStrings.CHAT_CD_MORE,
                        tint = grey950(),
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier.width(206.dp),
                    offset = DpOffset(x = (-8).dp, y = 0.dp),
                    shape = RoundedCornerShape(18.dp),
                    containerColor = white(),
                    shadowElevation = 8.dp,
                ) {
                    ThreadMenuSectionTitle(AppStrings.CHAT_MENU_SUMMARY_SETTINGS)
                    ThreadMenuItem(AppStrings.CHAT_MENU_SUMMARY) {
                        menuExpanded = false
                        onSummary()
                    }
                    ThreadMenuItem(
                        if (isMuted) AppStrings.CHAT_MENU_NOTIFICATION_ON
                        else AppStrings.CHAT_MENU_NOTIFICATION_OFF,
                    ) {
                        menuExpanded = false
                        onToggleMuted()
                    }
                    ThreadMenuItem(
                        if (isPinned) AppStrings.CHAT_MENU_UNPIN else AppStrings.CHAT_MENU_PIN,
                    ) {
                        menuExpanded = false
                        onTogglePinned()
                    }
                    ThreadMenuDivider()
                    ThreadMenuSectionTitle(AppStrings.CHAT_MENU_PARTICIPANTS)
                    ThreadMenuItem(AppStrings.CHAT_MENU_VIEW_PARTICIPANTS) {
                        menuExpanded = false
                        onParticipants()
                    }
                    if (isOwner) {
                        ThreadMenuItem(AppStrings.CHAT_MENU_INVITE_PARTICIPANTS) {
                            menuExpanded = false
                            onInviteParticipants()
                        }
                    }
                    ThreadMenuItem(AppStrings.CHAT_MENU_SHARE_LINK) {
                        menuExpanded = false
                        onShareLink()
                    }
                    ThreadMenuDivider()
                    if (isOwner) {
                        ThreadMenuSectionTitle(AppStrings.CHAT_MENU_THREAD)
                        ThreadMenuItem(AppStrings.CHAT_MENU_EDIT_THREAD) {
                            menuExpanded = false
                            onEditThread()
                        }
                        ThreadMenuItem(AppStrings.CHAT_MENU_DELETE_THREAD, color = red400()) {
                            menuExpanded = false
                            onDeleteThread()
                        }
                    }
                    ThreadMenuItem(AppStrings.CHAT_LEAVE, color = red400()) {
                        menuExpanded = false
                        onLeave()
                    }
                }
            }
        }
    }
}

@Composable
private fun ThreadMenuSectionTitle(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(start = 22.dp, end = 22.dp, top = 10.dp, bottom = 5.dp),
        color = grey500(),
        style = UmcTypographyTokens.FootnoteBold
    )
}

@Composable
private fun ThreadMenuItem(
    text: String,
    color: Color = grey950(),
    onClick: () -> Unit,
) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 22.dp, vertical = 14.dp),
        color = color,
        style = UmcTypographyTokens.Subheadline
    )
}

@Composable
private fun ThreadMenuDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp),
        color = grey300(),
    )
}

@Composable
private fun UnreadSummaryCard(
    unreadCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(indigo100())
            .border(1.dp, indigo200(), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_ai),
            contentDescription = null,
            tint = indigo500(),
            modifier = Modifier.size(32.dp),
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                AppStrings.CHAT_UNREAD_COUNT_FORMAT.format(unreadCount),
                color = grey950(),
                style = UmcTypographyTokens.SubheadlineBold
            )
            Text(
                AppStrings.CHAT_UNREAD_SUMMARY_DESCRIPTION,
                color = grey800(),
                style = UmcTypographyTokens.Footnote
            )
        }
        Icon(painterResource(R.drawable.ic_next_small), contentDescription = null, tint = grey400())
    }
}

private const val COMMUNITY_DEEP_LINK_HOST = "https://api.university.neordinary.com"
private const val COMMUNITY_DEEP_LINK_PATH = "/community/threads"
private val INITIAL_UNREAD_SCROLL_OFFSET = 96.dp
private val MESSAGE_TIME_MIN_WIDTH = 32.dp

internal fun buildCommunityThreadDeepLink(shareUrl: String): String {
    val rawUrl = shareUrl.trim()
    val parsed = runCatching { URI(rawUrl) }.getOrNull()
    val path = parsed?.rawPath
        ?.takeIf(String::isNotBlank)
        ?: rawUrl.substringBefore('?').substringBefore('#')
    val queryThreadId = parsed?.rawQuery
        ?.split('&')
        ?.firstOrNull { it.substringBefore('=') == "threadId" }
        ?.substringAfter('=', missingDelimiterValue = "")
    val threadId = queryThreadId
        ?.takeIf(String::isNotBlank)
        ?: path.trimEnd('/').substringAfterLast('/')

    if (threadId.isBlank()) return rawUrl

    return "$COMMUNITY_DEEP_LINK_HOST$COMMUNITY_DEEP_LINK_PATH?threadId=$threadId"
}

@Composable
private fun NewMessageButton(
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        color = indigo500(),
        contentColor = white(),
        shadowElevation = 4.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.ic_dropdown),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = AppStrings.CHAT_NEW_MESSAGE_COUNT_FORMAT.format(count),
                style = UmcTypographyTokens.HeadlineBold
            )
        }
    }
}

@Composable
private fun DateDivider(label: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        HorizontalDivider(Modifier.weight(1f), color = grey300())
        Text(label, color = grey300(), style = UmcTypographyTokens.Footnote)
        HorizontalDivider(Modifier.weight(1f), color = grey300())
    }
}

@Composable
private fun NewMessagesDivider() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        HorizontalDivider(Modifier.weight(1f), color = indigo300())
        Text(
            text = AppStrings.CHAT_NEW_MESSAGE_DIVIDER,
            color = indigo500(),
            style = UmcTypographyTokens.FootnoteBold,
        )
        HorizontalDivider(Modifier.weight(1f), color = indigo300())
    }
}

@Composable
private fun ChatMessageRow(
    message: CommunityThreadMessage,
    imageUris: List<String>,
    member: CommunityThreadMember?,
    isMine: Boolean,
    menuExpanded: Boolean,
    onLongClick: () -> Unit,
    onDismissMenu: () -> Unit,
    onReply: () -> Unit,
    onCopy: () -> Unit,
    onReport: () -> Unit,
    onDelete: () -> Unit,
    onReact: (String) -> Unit,
    onShowMoreEmojis: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (isMine) {
                MineMessage(message, imageUris, onLongClick)
            } else {
                OtherMessage(message, imageUris, member, onLongClick)
            }
            if (message.reactions.isNotEmpty()) {
                MessageReactions(
                    reactions = message.reactions,
                    isMine = isMine,
                    onReactionClick = onReact,
                )
            }
        }
        if (menuExpanded) {
            MessageActionPopup(
                isMine = isMine,
                onDismiss = onDismissMenu,
                onReply = onReply,
                onCopy = onCopy,
                onReport = onReport,
                onDelete = onDelete,
                onReact = onReact,
                onShowMoreEmojis = onShowMoreEmojis,
            )
        }
    }
}

@Composable
private fun MessageReactions(
    reactions: List<CommunityReaction>,
    isMine: Boolean,
    onReactionClick: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = if (isMine) 0.dp else 46.dp,
                end = 0.dp,
                top = 5.dp,
            ),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start,
    ) {
        reactions.forEach { reaction ->
            val selectedColor = if (reaction.reactedByMe) indigo100() else white()
            Surface(
                modifier = Modifier
                    .padding(end = 5.dp)
                    .clickable { onReactionClick(reaction.emoji) },
                shape = RoundedCornerShape(14.dp),
                color = selectedColor,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (reaction.reactedByMe) indigo200() else grey200(),
                ),
            ) {
                Text(
                    text = "${reaction.emoji} ${reaction.count}",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    color = grey950(),
                    style = UmcTypographyTokens.Footnote
                )
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun OtherMessage(
    message: CommunityThreadMessage,
    imageUris: List<String>,
    member: CommunityThreadMember?,
    onLongClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        CommunityChatProfileImage(member?.profileImageUrl)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                Text(
                    message.senderName.orEmpty().ifBlank { AppStrings.CHAT_UNKNOWN_USER },
                    color = grey950(),
                    style = UmcTypographyTokens.SubheadlineBold
                )
                if (
                    member?.role == CommunityThreadRole.ADMIN ||
                    member?.part == UserPart.ADMIN
                ) {
                    CommunityChatMemberTag(AppStrings.CHAT_ADMIN_BADGE, grey950(), white())
                }
                member?.part?.takeUnless { it == UserPart.ADMIN }?.let { part ->
                    val tag = communityChatPartTag(part)
                    CommunityChatMemberTag(tag.first, tag.second, tag.third)
                }
            }
            Spacer(Modifier.height(7.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Surface(
                    modifier = Modifier.weight(1f, fill = false),
                    color = if (message.type == CommunityMessageType.IMAGE) Color.Transparent else white(),
                    shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 5.dp),
                ) {
                    MessageBubbleContent(
                        message = message,
                        imageUris = imageUris,
                        isMine = false,
                        modifier = Modifier
                            .widthIn(max = 270.dp)
                            .combinedClickable(onClick = {}, onLongClick = onLongClick)
                            .then(
                                if (message.type == CommunityMessageType.IMAGE) Modifier
                                else Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                            ),
                    )
                }
                MessageTime(message.createdAt, Modifier.padding(start = 8.dp, bottom = 2.dp))
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun MineMessage(
    message: CommunityThreadMessage,
    imageUris: List<String>,
    onLongClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.Bottom,
    ) {
        MessageTime(message.createdAt, Modifier.padding(end = 8.dp, bottom = 2.dp))
        Surface(
            color = if (message.type == CommunityMessageType.IMAGE) Color.Transparent else indigo500(),
            shape = RoundedCornerShape(16.dp),
        ) {
            MessageBubbleContent(
                message = message,
                imageUris = imageUris,
                isMine = true,
                modifier = Modifier
                    .widthIn(max = 285.dp)
                    .combinedClickable(onClick = {}, onLongClick = onLongClick)
                    .then(
                        if (message.type == CommunityMessageType.IMAGE) Modifier
                        else Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                    ),
            )
        }
    }
}

@Composable
private fun MessageBubbleContent(
    message: CommunityThreadMessage,
    imageUris: List<String> = emptyList(),
    isMine: Boolean,
    modifier: Modifier = Modifier,
) {
    var imageViewerIndex by remember(message.messageId) { mutableStateOf<Int?>(null) }
    val foreground = if (isMine) white() else grey950()
    val secondary = if (isMine) indigo100() else grey400()
    val divider = if (isMine) white().copy(alpha = 0.22f) else grey200()
    val reply = message.replyTo

    if (message.type == CommunityMessageType.IMAGE) {
        val models = imageUris.ifEmpty {
            message.files
                .map { it.fileUrl }
                .filter(String::isNotBlank)
                .ifEmpty {
                    listOfNotNull(message.content?.takeIf(String::isNotBlank))
                }
        }.take(COMMUNITY_CHAT_MAX_IMAGE_COUNT)
        imageViewerIndex?.let { initialIndex ->
            ChatImageViewer(
                models = models,
                initialIndex = initialIndex,
                onDismiss = { imageViewerIndex = null },
            )
        }
        if (models.size == 1) {
            AsyncImage(
                model = models.first(),
                contentDescription = AppStrings.CHAT_CD_IMAGE,
                modifier = modifier
                    .size(width = 220.dp, height = 180.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { imageViewerIndex = 0 },
                contentScale = ContentScale.Crop,
            )
        } else if (models.isNotEmpty()) {
            ChatImageGrid(
                models = models,
                modifier = modifier,
                onImageClick = { imageViewerIndex = it },
            )
        } else {
            Box(
                modifier = modifier.size(width = 220.dp, height = 160.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = painterResource(R.drawable.ic_photo),
                        contentDescription = null,
                        tint = secondary,
                        modifier = Modifier.size(32.dp),
                    )
                }
            }
        }
        return
    }

    if (reply == null) {
        Text(
            text = message.content.orEmpty(),
            modifier = modifier,
            color = foreground,
            style = UmcTypographyTokens.Subheadline
        )
        return
    }

    Column(modifier = modifier) {
        Text(
            text = AppStrings.CHAT_REPLY_TO_FORMAT.format(
                reply.senderName.ifBlank { AppStrings.CHAT_UNKNOWN_USER },
            ),
            color = foreground,
            style = UmcTypographyTokens.SubheadlineBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = reply.snippet.ifBlank { AppStrings.CHAT_MESSAGE_CONTENT },
            modifier = Modifier.padding(top = 3.dp),
            color = secondary,
            style = UmcTypographyTokens.Footnote,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 10.dp),
            color = divider,
        )
        Text(
            text = message.content.orEmpty(),
            color = foreground,
            style = UmcTypographyTokens.Subheadline
        )
    }
}

@Composable
private fun ChatImageGrid(
    models: List<String>,
    modifier: Modifier = Modifier,
    onImageClick: (Int) -> Unit = {},
) {
    val images = models.take(COMMUNITY_CHAT_MAX_IMAGE_COUNT)
    val containerModifier = modifier.width(220.dp).clip(RoundedCornerShape(16.dp))

    when (images.size) {
        0 -> Unit
        1 -> ChatGridImage(images.first(), containerModifier.height(180.dp)) { onImageClick(0) }
        2 -> Row(
            modifier = containerModifier.height(160.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            images.forEachIndexed { index, model ->
                ChatGridImage(model, Modifier.weight(1f).fillMaxHeight()) { onImageClick(index) }
            }
        }
        3 -> Row(
            modifier = containerModifier.height(220.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            ChatGridImage(images.first(), Modifier.weight(1f).fillMaxHeight()) { onImageClick(0) }
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                images.drop(1).forEachIndexed { index, model ->
                    ChatGridImage(model, Modifier.weight(1f).fillMaxWidth()) { onImageClick(index + 1) }
                }
            }
        }
        else -> Column(
            modifier = containerModifier,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            images.chunked(2).forEachIndexed { rowIndex, rowModels ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    rowModels.forEachIndexed { columnIndex, model ->
                        val imageIndex = rowIndex * 2 + columnIndex
                        ChatGridImage(model, Modifier.weight(1f).aspectRatio(1f)) {
                            onImageClick(imageIndex)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatGridImage(model: String, modifier: Modifier, onClick: () -> Unit = {}) {
    AsyncImage(
        model = model,
        contentDescription = AppStrings.CHAT_CD_IMAGE,
        modifier = modifier.clickable(onClick = onClick),
        contentScale = ContentScale.Crop,
    )
}

@Composable
private fun ChatImageViewer(
    models: List<String>,
    initialIndex: Int,
    onDismiss: () -> Unit,
) {
    if (models.isEmpty()) return
    val pagerState = rememberPagerState(
        initialPage = initialIndex.coerceIn(models.indices),
        pageCount = models::size,
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
            ) { page ->
                AsyncImage(
                    model = models[page],
                    contentDescription = AppStrings.CHAT_CD_IMAGE,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                )
            }
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(12.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = AppStrings.CHAT_CD_BACK,
                    tint = white(),
                )
            }
            if (models.size > 1) {
                Text(
                    text = "${pagerState.currentPage + 1} / ${models.size}",
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp),
                    color = white(),
                    style = UmcTypographyTokens.SubheadlineBold,
                )
            }
        }
    }
}

@Composable
private fun MessageActionPopup(
    isMine: Boolean,
    onDismiss: () -> Unit,
    onReply: () -> Unit,
    onCopy: () -> Unit,
    onReport: () -> Unit,
    onDelete: () -> Unit,
    onReact: (String) -> Unit,
    onShowMoreEmojis: () -> Unit,
) {
    val density = LocalDensity.current
    val popupPositionProvider = remember(isMine, density) {
        val gap = with(density) { 8.dp.roundToPx() }
        val screenMargin = with(density) { 12.dp.roundToPx() }

        object : PopupPositionProvider {
            override fun calculatePosition(
                anchorBounds: IntRect,
                windowSize: IntSize,
                layoutDirection: LayoutDirection,
                popupContentSize: IntSize,
            ): IntOffset {
                val desiredX = if (isMine) {
                    anchorBounds.right - popupContentSize.width
                } else {
                    anchorBounds.left
                }
                val maxX = (windowSize.width - popupContentSize.width - screenMargin)
                    .coerceAtLeast(screenMargin)
                val x = desiredX.coerceIn(screenMargin, maxX)

                val maxY = (windowSize.height - popupContentSize.height - screenMargin)
                    .coerceAtLeast(screenMargin)
                val belowY = anchorBounds.bottom + gap
                val aboveY = anchorBounds.top - gap - popupContentSize.height
                val y = if (belowY <= maxY) {
                    belowY
                } else {
                    aboveY.coerceIn(screenMargin, maxY)
                }

                return IntOffset(x, y)
            }
        }
    }

    Popup(
        popupPositionProvider = popupPositionProvider,
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true),
    ) {
        Column(
            modifier = Modifier.width(190.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = if (isMine) Alignment.End else Alignment.Start,
        ) {
            Surface(
                modifier = Modifier.width(112.dp),
                shape = RoundedCornerShape(10.dp),
                color = white(),
                shadowElevation = 7.dp,
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    MessageActionItem(AppStrings.CHAT_ACTION_REPLY, R.drawable.ic_forward_circle, onClick = onReply)
                    MessageActionItem(AppStrings.CHAT_ACTION_COPY, R.drawable.ic_document, onClick = onCopy)
                    MessageActionItem(AppStrings.CHAT_ACTION_REPORT, R.drawable.ic_warning, onClick = onReport)
                    MessageActionItem(
                        text = AppStrings.CHAT_DELETE,
                        iconRes = R.drawable.ic_trash_can,
                        color = red400(),
                        onClick = onDelete,
                    )
                }
            }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = white(),
                shadowElevation = 7.dp,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    listOf("❤️", "👍", "✅", "🙂", "😆").forEach { emoji ->
                        Text(
                            text = emoji,
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable { onReact(emoji) }
                                .padding(4.dp),
                            fontSize = 18.sp,
                        )
                    }
                    IconButton(
                        onClick = onShowMoreEmojis,
                        modifier = Modifier.size(30.dp),
                    ) {
                        Icon(
                            painterResource(R.drawable.ic_add),
                            contentDescription = AppStrings.CHAT_CD_OTHER_EMOJI,
                            modifier = Modifier.size(17.dp),
                        )
                    }
                }
            }
        }
    }
}

private val MessageReportReasons = listOf(
    CommunityMessageReportReason.SPAM to AppStrings.CHAT_REPORT_REASON_SPAM,
    CommunityMessageReportReason.ABUSE to AppStrings.CHAT_REPORT_REASON_ABUSE,
    CommunityMessageReportReason.INAPPROPRIATE to AppStrings.CHAT_REPORT_REASON_INAPPROPRIATE,
    CommunityMessageReportReason.PRIVACY to AppStrings.CHAT_REPORT_REASON_PRIVACY,
    CommunityMessageReportReason.ETC to AppStrings.CHAT_REPORT_REASON_ETC,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MessageReportSheet(
    onDismiss: () -> Unit,
    onReport: (CommunityMessageReportReason) -> Unit,
) {
    var selectedReason by remember { mutableStateOf<CommunityMessageReportReason?>(null) }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = white(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(537.dp)
                .padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
        ) {
            Text(
                text = AppStrings.CHAT_REPORT_REASON_TITLE,
                color = grey950(),
                style = UmcTypographyTokens.Title3Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = AppStrings.CHAT_REPORT_GUIDE,
                color = grey600(),
                style = UmcTypographyTokens.Subheadline
            )
            Spacer(Modifier.height(12.dp))
            MessageReportReasons.forEach { (reason, label) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedReason = reason }
                        .padding(vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = label,
                        modifier = Modifier.weight(1f),
                        color = grey950(),
                        style = UmcTypographyTokens.Body
                    )
                    Checkbox(
                        checked = selectedReason == reason,
                        onCheckedChange = { selectedReason = reason },
                        modifier = Modifier.size(24.dp),
                        colors = CheckboxDefaults.colors(
                            checkedColor = indigo500(),
                            uncheckedColor = grey400(),
                            checkmarkColor = white(),
                        ),
                    )
                }
                HorizontalDivider(color = grey200())
            }
            Spacer(Modifier.weight(1f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = grey100(),
                        contentColor = grey800(),
                    ),
                ) {
                    Text(AppStrings.CHAT_REPORT_CANCEL, style = UmcTypographyTokens.HeadlineBold)
                }
                Button(
                    onClick = { selectedReason?.let(onReport) },
                    enabled = selectedReason != null,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = red100(),
                        contentColor = red500(),
                        disabledContainerColor = red100().copy(alpha = 0.55f),
                        disabledContentColor = red500().copy(alpha = 0.45f),
                    ),
                ) {
                    Text(AppStrings.CHAT_REPORT_ACTION, style = UmcTypographyTokens.HeadlineBold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdditionalEmojiSheet(
    onDismiss: () -> Unit,
    onEmojiSelected: (String) -> Unit,
) {
    val currentOnEmojiSelected by rememberUpdatedState(onEmojiSelected)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = white(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(360.dp),
            factory = { context ->
                EmojiPickerView(context).apply {
                    emojiGridColumns = 8
                    setOnEmojiPickedListener { pickedEmoji ->
                        currentOnEmojiSelected(pickedEmoji.emoji)
                    }
                }
            },
        )
        Spacer(Modifier.navigationBarsPadding())
    }
}

@Composable
private fun MessageActionItem(
    text: String,
    @androidx.annotation.DrawableRes iconRes: Int,
    color: Color = grey950(),
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text, color = color, style = UmcTypographyTokens.Subheadline)
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun PendingMessageRow(
    message: PendingCommunityMessage,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
) {
    val failed = message.error != null
    val isImage = message.type == CommunityMessageType.IMAGE && message.localUris.isNotEmpty()

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.End,
    ) {
        Row(
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                if (failed) AppStrings.CHAT_SEND_FAILED else AppStrings.CHAT_SENDING,
                modifier = Modifier.padding(end = 8.dp, bottom = 2.dp),
                color = if (failed) red400() else grey400(),
                fontSize = if (failed) 13.sp else 11.sp,
            )
            Surface(
                color = when {
                    isImage -> Color.Transparent
                    failed -> red100()
                    else -> indigo500()
                },
                shape = RoundedCornerShape(16.dp),
                border = if (failed) {
                    androidx.compose.foundation.BorderStroke(1.dp, red400())
                } else {
                    null
                },
            ) {
                if (message.type == CommunityMessageType.IMAGE && message.localUris.size == 1) {
                    AsyncImage(
                        model = message.localUris.first(),
                        contentDescription = AppStrings.CHAT_CD_SEND_IMAGE,
                        modifier = Modifier
                            .size(width = 220.dp, height = 180.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop,
                    )
                } else if (message.type == CommunityMessageType.IMAGE) {
                    ChatImageGrid(models = message.localUris)
                } else {
                    Text(
                        message.content,
                        modifier = Modifier
                            .widthIn(max = 285.dp)
                            .padding(horizontal = 16.dp, vertical = 13.dp),
                        color = if (failed) red400() else white(),
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                    )
                }
            }
        }
        if (failed) {
            Row(
                modifier = Modifier.padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                IconButton(
                    onClick = onRetry,
                    modifier = Modifier.size(38.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_refresh),
                        contentDescription = AppStrings.CHAT_CD_RETRY_MESSAGE,
                        tint = grey950(),
                        modifier = Modifier.size(26.dp),
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(38.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_close_big),
                        contentDescription = AppStrings.CHAT_CD_DELETE_FAILED_MESSAGE,
                        tint = red400(),
                        modifier = Modifier.size(28.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageTime(createdAt: String, modifier: Modifier = Modifier) {
    Text(
        formatCommunityCreatedAt(createdAt),
        modifier = modifier.widthIn(min = MESSAGE_TIME_MIN_WIDTH),
        color = grey400(),
        fontSize = 11.sp,
        maxLines = 1,
        softWrap = false,
    )
}

internal fun latestListItemIndex(
    messageCount: Int,
    pendingMessageCount: Int,
    hasMore: Boolean,
): Int = (
    messageCount + pendingMessageCount + (if (hasMore) 1 else 0) - 1
).coerceAtLeast(0)

private val KoreaZoneId = ZoneId.of("Asia/Seoul")
private val CommunityTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

internal fun formatCommunityCreatedAt(createdAt: String): String {
    val instant = parseCommunityCreatedAt(createdAt) ?: return "--:--"

    return CommunityTimeFormatter.format(instant.atZone(KoreaZoneId))
}

private fun communityCreatedAtDate(createdAt: String): LocalDate? =
    parseCommunityCreatedAt(createdAt)?.atZone(KoreaZoneId)?.toLocalDate()

internal fun formatCommunityDate(
    createdAt: String,
    today: LocalDate = LocalDate.now(KoreaZoneId),
): String {
    val date = communityCreatedAtDate(createdAt) ?: return AppStrings.CHAT_DATE_UNKNOWN
    return when (date) {
        today -> AppStrings.CHAT_DATE_TODAY
        today.minusDays(1) -> AppStrings.CHAT_DATE_YESTERDAY
        else -> if (date.year == today.year) {
            AppStrings.CHAT_DATE_MONTH_DAY_FORMAT.format(date.monthValue, date.dayOfMonth)
        } else {
            AppStrings.CHAT_DATE_YEAR_MONTH_DAY_FORMAT.format(
                date.year,
                date.monthValue,
                date.dayOfMonth,
            )
        }
    }
}

private fun parseCommunityCreatedAt(createdAt: String): Instant? {
    if (createdAt.isBlank()) return null
    return runCatching { Instant.parse(createdAt) }.getOrNull()
        ?: runCatching { OffsetDateTime.parse(createdAt).toInstant() }.getOrNull()
        ?: runCatching {
            LocalDateTime.parse(createdAt).atZone(KoreaZoneId).toInstant()
        }.getOrNull()
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun CommunityChattingScreenPreview() {
    CommunityChattingScreen(
        state = CommunityChattingState(
            threadId = "1",
            thread = CommunityThreadDetail(
                threadId = "1",
                title = "이건 채팅방이름임 이건채이름...",
                unreadCount = "32",
            ),
            myMemberId = "10",
            members = mapOf(
                "20" to CommunityThreadMember(
                    memberId = "20",
                    name = "홍길동(닉네임)",
                    part = UserPart.IOS,
                    role = CommunityThreadRole.ADMIN,
                )
            ),
            messages = listOf(
                CommunityThreadMessage(
                    messageId = "4",
                    senderId = "10",
                    content = "네 저녁까지요! 늦으면 다음 주에 몰아서 인증해도 됩니다",
                    createdAt = "2026-07-30T14:33:00",
                ),
                CommunityThreadMessage(
                    messageId = "3",
                    senderId = "20",
                    senderName = "홍길동(닉네임)",
                    content = "인증은 자정까지죠? 늦으면 어떻게 되나요?",
                    createdAt = "2026-07-30T14:31:00",
                ),
                CommunityThreadMessage(
                    messageId = "2",
                    senderId = "10",
                    content = "저는 절반 정도요 ㅠㅠ 주말에 마무리하려구요",
                    createdAt = "2026-07-30T14:20:00",
                ),
                CommunityThreadMessage(
                    messageId = "1",
                    senderId = "20",
                    senderName = "홍길동(닉네임)",
                    content = "다들 이번 주 과제 어디까지 하셨어요? 저는 로그인 화면까지 했어요",
                    createdAt = "2026-07-30T14:14:00",
                ),
            ),
        ),
        onAction = {},
    )
}
