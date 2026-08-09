package com.umc.presentation.community.chatting

import android.content.Intent
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import coil.compose.AsyncImage
import com.umc.component.theme.black
import com.umc.component.R
import com.umc.component.component.DialogType
import com.umc.component.component.UBasicDialog
import com.umc.component.component.UDialog
import com.umc.component.theme.AppStrings
import com.umc.component.theme.grey100
import com.umc.component.theme.grey200
import com.umc.component.theme.grey300
import com.umc.component.theme.grey400
import com.umc.component.theme.grey800
import com.umc.component.theme.grey950
import com.umc.component.theme.indigo100
import com.umc.component.theme.indigo200
import com.umc.component.theme.indigo500
import com.umc.component.theme.red400
import com.umc.component.theme.red100
import com.umc.component.theme.red500
import com.umc.component.theme.white
import com.umc.component.theme.green100
import com.umc.component.theme.green300
import com.umc.component.theme.green700
import com.umc.component.theme.grey50
import com.umc.component.theme.grey500
import com.umc.component.theme.grey600
import com.umc.component.theme.grey700
import com.umc.component.theme.grey900
import com.umc.component.theme.indigo600
import com.umc.component.theme.red600
import com.umc.component.theme.yellow100
import com.umc.component.theme.yellow500
import com.umc.component.theme.yellow600
import com.umc.domain.model.community.thread.CommunityMessageReportReason
import com.umc.domain.model.community.thread.CommunityMessageType
import com.umc.domain.model.community.thread.CommunityReaction
import com.umc.domain.model.community.thread.CommunityThreadDetail
import com.umc.domain.model.community.thread.CommunityThreadMember
import com.umc.domain.model.community.thread.CommunityThreadMessage
import com.umc.domain.model.community.thread.CommunityThreadRole
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.net.URI
import kotlinx.coroutines.launch

@OptIn(
    androidx.compose.foundation.ExperimentalFoundationApi::class,
    ExperimentalMaterial3Api::class,
)
@Composable
fun CommunityChattingScreen(
    state: CommunityChattingState,
    snackbarHostState: SnackbarHostState? = null,
    onAction: (CommunityChattingAction) -> Unit,
) {
    val onBack = { onAction(CommunityChattingAction.OnBackClick) }
    val onMore = { onAction(CommunityChattingAction.OnMoreClick) }
    val onUnreadSummary: (List<CommunityThreadMessage>) -> Unit = {
        onAction(CommunityChattingAction.OnUnreadSummaryClick(it))
    }
    val onCamera = { onAction(CommunityChattingAction.OnCameraClick) }
    val onSendImages: (List<String>) -> Unit = { onAction(CommunityChattingAction.OnSendImages(it)) }
    val onDraftChange: (String) -> Unit = { onAction(CommunityChattingAction.OnDraftChanged(it)) }
    val onSend: (Long?) -> Unit = { onAction(CommunityChattingAction.OnSendClick(it)) }
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
    val initialUnreadScrollOffset = with(LocalDensity.current) {
        INITIAL_UNREAD_SCROLL_OFFSET.roundToPx()
    }
    val displayedMessages = remember(state.messages) { state.messages.asReversed() }
    var knownMessageIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var knownPendingMessageIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var newMessageCount by remember { mutableIntStateOf(0) }
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
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 4),
    ) { uris ->
        onSendImages(uris.take(4).map { it.toString() })
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
        ConversationSummarySheet(
            state = state,
            onRetry = { onAction(CommunityChattingAction.OnRetryUnreadSummary) },
            onDismiss = { onAction(CommunityChattingAction.OnDismissUnreadSummary) },
        )
    }

    if (showParticipants) {
        CommunityParticipantScreen(
            members = state.members.values.toList(),
            memberCount = state.thread?.memberCount.orEmpty(),
            myMemberId = state.myMemberId,
            isOwner = state.thread?.myRole == CommunityThreadRole.OWNER,
            onBack = { showParticipants = false },
            onKickMember = onKickMember,
            onTransferOwnership = onTransferOwnership,
        )
        return
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
                    if (followsLatestMessage) newMessageCount = 0
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
                    newMessageCount = entryUnreadCount
                    followsLatestMessage = false
                    (displayedMessages.size - loadedUnreadCount - 1).coerceAtLeast(0)
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
            listState.animateScrollToItem(
                latestListItemIndex(
                    messageCount = displayedMessages.size,
                    pendingMessageCount = state.pendingMessages.size,
                    hasMore = state.hasMore,
                ),
            )
        } else {
            newMessageCount += newMessages.count { it.senderId != state.myMemberId }
        }
    }

    LaunchedEffect(state.pendingMessages.keys) {
        val currentIds = state.pendingMessages.keys.toSet()
        val hasNewPendingMessage = currentIds.any { it !in knownPendingMessageIds }
        knownPendingMessageIds = currentIds
        if (!hasNewPendingMessage) return@LaunchedEffect

        newMessageCount = 0
        followsLatestMessage = true
        val lastItemIndex = latestListItemIndex(
            messageCount = displayedMessages.size,
            pendingMessageCount = currentIds.size,
            hasMore = state.hasMore,
        )
        listState.animateScrollToItem(lastItemIndex)
    }

    Scaffold(
        containerColor = grey100(),
        snackbarHost = {
            snackbarHostState?.let { hostState ->
                SnackbarHost(hostState = hostState) { data ->
                    ChatSuccessSnackbar(message = data.visuals.message)
                }
            }
        },
        topBar = {
            ChatTopBar(
                title = state.thread?.title.orEmpty().ifBlank { AppStrings.CHAT_ROOM_DEFAULT },
                onBack = onBack,
                isMuted = state.thread?.isMuted == true,
                isPinned = state.thread?.isPinned == true,
                isOwner = state.thread?.myRole == CommunityThreadRole.OWNER,
                onSummary = { onUnreadSummary(emptyList()) },
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
            if (!state.isLoading && state.errorMessage == null) {
                ChatInputBar(
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
                        onSend(replyingMessage?.messageId?.toLongOrNull())
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
                            clipboardManager.setText(AnnotatedString(message.content.orEmpty()))
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
                    onClick = {
                        onUnreadSummary(state.messages.take(newMessageCount))
                    },
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
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = AppStrings.CHAT_NO_MESSAGES_DESCRIPTION,
            color = grey400(),
            fontSize = 12.sp,
            lineHeight = 17.sp,
        )
    }
}

@Composable
private fun ChatSuccessSnackbar(message: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(7.dp),
        color = grey900(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_check_success),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = green300(),
            )
            Text(
                text = message,
                color = white(),
                fontSize = 13.sp,
                lineHeight = 18.sp,
            )
        }
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
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
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
                fontSize = 15.sp,
                lineHeight = 21.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = AppStrings.CHAT_UNAVAILABLE_DESCRIPTION,
                color = grey400(),
                fontSize = 13.sp,
                lineHeight = 18.sp,
            )
            Spacer(Modifier.height(34.dp))
            Button(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = indigo500(),
                    contentColor = white(),
                ),
            ) {
                Text(
                    text = AppStrings.CHAT_BACK_TO_COMMUNITY,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
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
            painter = painterResource(R.drawable.ic_error_large),
            contentDescription = null,
            modifier = Modifier.size(46.dp),
            tint = grey400(),
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = AppStrings.CHAT_LOAD_ERROR_TITLE,
            color = grey800(),
            fontSize = 22.sp,
            lineHeight = 28.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = AppStrings.CHAT_LOAD_ERROR_DESCRIPTION,
            color = grey400(),
            fontSize = 16.sp,
            lineHeight = 22.sp,
        )
        Spacer(Modifier.height(72.dp))
        Button(
            onClick = onRetry,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = indigo500(),
                contentColor = white(),
            ),
        ) {
            Text(
                text = AppStrings.CHAT_RETRY,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
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
                fontSize = 21.sp,
                lineHeight = 26.sp,
                fontWeight = FontWeight.Bold,
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
        color = grey400(),
        fontSize = 13.sp,
        lineHeight = 18.sp,
        fontWeight = FontWeight.SemiBold,
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
        fontSize = 16.sp,
        lineHeight = 22.sp,
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
        Text("✨", fontSize = 24.sp)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                AppStrings.CHAT_UNREAD_COUNT_FORMAT.format(unreadCount),
                color = grey950(),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                AppStrings.CHAT_UNREAD_SUMMARY_DESCRIPTION,
                color = grey800(),
                fontSize = 12.sp,
            )
        }
        Icon(painterResource(R.drawable.ic_next_small), contentDescription = null, tint = grey400())
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConversationSummarySheet(
    state: CommunityChattingState,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = white(),
        dragHandle = {
            Box(
                Modifier
                    .padding(top = 10.dp, bottom = 18.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(grey400()),
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 300.dp)
                .padding(horizontal = 20.dp)
                .padding(bottom = 40.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_ai),
                    contentDescription = null,
                    tint = indigo500(),
                    modifier = Modifier.size(22.dp),
                )
                Text(
                    text = AppStrings.CHAT_AI_SHEET_TITLE,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp),
                    color = grey950(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
                IconButton(onClick = onRetry, enabled = !state.isSummarizingUnread) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = AppStrings.CHAT_RETRY,
                        tint = grey500(),
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            when {
                state.isSummarizingUnread -> SummaryLoadingContent(state.aiDownloadPercent)
                state.unreadSummaryError != null -> SummaryErrorContent(
                    message = state.unreadSummaryError,
                )
                else -> SummarySuccessContent(
                    summary = state.unreadSummary.orEmpty(),
                    messageCount = state.summarizedMessageCount,
                )
            }
        }
    }
}

@Composable
private fun SummaryLoadingContent(downloadPercent: Int?) {
    Text(
        text = downloadPercent
            ?.let { AppStrings.AI_MODEL_DOWNLOADING.format(it) }
            ?: AppStrings.CHAT_AI_SUMMARIZING_DESCRIPTION,
        color = grey500(),
        fontSize = 12.sp,
    )
    Spacer(Modifier.height(18.dp))
    listOf(0.68f, 1f, 0.9f, 0.9f).forEach { fraction ->
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF5B9CF5), Color(0xFF28C7A5)),
                    ),
                ),
        )
        Spacer(Modifier.height(10.dp))
    }
}

@Composable
private fun SummaryErrorContent(message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = grey400(),
            modifier = Modifier.size(32.dp),
        )
        Spacer(Modifier.height(14.dp))
        Text(
            text = AppStrings.CHAT_AI_SUMMARY_FAILED,
            color = grey600(),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = message.ifBlank { AppStrings.CHAT_AI_SUMMARY_RETRY_DESCRIPTION },
            color = grey400(),
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun SummarySuccessContent(summary: String, messageCount: Int) {
    Text(
        text = AppStrings.CHAT_AI_SUMMARY_COUNT_FORMAT.format(messageCount),
        color = grey500(),
        fontSize = 12.sp,
    )
    Spacer(Modifier.height(14.dp))
    summary
        .lineSequence()
        .map { it.trim().removePrefix("-").removePrefix("•").trim() }
        .filter(String::isNotBlank)
        .forEach { line ->
            Row(
                modifier = Modifier.padding(vertical = 5.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Text("✓", color = indigo500(), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = line,
                    modifier = Modifier.padding(start = 10.dp),
                    color = grey800(),
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                )
            }
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
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = AppStrings.CHAT_NEW_MESSAGE_COUNT_FORMAT.format(count),
                fontSize = 13.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.SemiBold,
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
        Text(label, color = grey300(), fontSize = 12.sp, fontWeight = FontWeight.Medium)
        HorizontalDivider(Modifier.weight(1f), color = grey300())
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
                end = if (isMine) 0.dp else 0.dp,
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
                    fontSize = 12.sp,
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
        ProfileImage(member?.profileImageUrl)
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
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                member?.part?.let { part ->
                    val tag = partTag(part)
                    MemberTag(tag.first, tag.second, tag.third)
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
    val foreground = if (isMine) white() else grey950()
    val secondary = if (isMine) indigo100() else grey400()
    val divider = if (isMine) white().copy(alpha = 0.22f) else grey200()
    val reply = message.replyTo

    if (message.type == CommunityMessageType.IMAGE) {
        val models = imageUris.ifEmpty {
            listOfNotNull(message.content?.takeIf(String::isNotBlank))
        }.take(4)
        if (models.size == 1) {
            AsyncImage(
                model = models.first(),
                contentDescription = AppStrings.CHAT_CD_IMAGE,
                modifier = modifier
                    .size(width = 220.dp, height = 180.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop,
            )
        } else if (models.isNotEmpty()) {
            ChatImageGrid(models = models, modifier = modifier)
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
                    Text(
                        text = AppStrings.CHAT_IMAGE,
                        modifier = Modifier.padding(top = 6.dp),
                        color = secondary,
                        fontSize = 13.sp,
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
            fontSize = 14.sp,
            lineHeight = 20.sp,
        )
        return
    }

    Column(modifier = modifier) {
        Text(
            text = AppStrings.CHAT_REPLY_TO_FORMAT.format(
                reply.senderName.ifBlank { AppStrings.CHAT_UNKNOWN_USER },
            ),
            color = foreground,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = reply.snippet.ifBlank { AppStrings.CHAT_MESSAGE_CONTENT },
            modifier = Modifier.padding(top = 3.dp),
            color = secondary,
            fontSize = 13.sp,
            lineHeight = 19.sp,
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
            fontSize = 14.sp,
            lineHeight = 20.sp,
        )
    }
}

@Composable
private fun ChatImageGrid(
    models: List<String>,
    modifier: Modifier = Modifier,
) {
    val images = models.take(4)
    val containerModifier = modifier.width(220.dp).clip(RoundedCornerShape(16.dp))

    when (images.size) {
        0 -> Unit
        1 -> ChatGridImage(images.first(), containerModifier.height(180.dp))
        2 -> Row(
            modifier = containerModifier.height(160.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            images.forEach { model ->
                ChatGridImage(model, Modifier.weight(1f).fillMaxHeight())
            }
        }
        3 -> Row(
            modifier = containerModifier.height(220.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            ChatGridImage(images.first(), Modifier.weight(1f).fillMaxHeight())
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                images.drop(1).forEach { model ->
                    ChatGridImage(model, Modifier.weight(1f).fillMaxWidth())
                }
            }
        }
        else -> Column(
            modifier = containerModifier,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            images.chunked(2).forEach { rowModels ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    rowModels.forEach { model ->
                        ChatGridImage(model, Modifier.weight(1f).aspectRatio(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatGridImage(model: String, modifier: Modifier) {
    AsyncImage(
        model = model,
        contentDescription = AppStrings.CHAT_CD_IMAGE,
        modifier = modifier,
        contentScale = ContentScale.Crop,
    )
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
    Popup(
        alignment = if (isMine) Alignment.TopEnd else Alignment.TopStart,
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
                        iconRes = R.drawable.ic_delete,
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

private val AdditionalReactionEmojis = listOf(
    "😀", "😃", "😄", "😁", "😂", "🤣", "😊", "😍",
    "🥰", "😘", "😎", "🤩", "🥳", "🙂", "🥹", "😢",
    "😭", "😤", "😡", "🤔", "🫡", "🤗", "🤭", "😴",
    "👍", "👎", "👏", "🙌", "🙏", "💪", "👌", "✌️",
    "❤️", "🧡", "💛", "💚", "💙", "💜", "🖤", "🤍",
    "🔥", "✨", "🎉", "💯", "✅", "❌", "👀", "🚀",
)

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
                fontSize = 20.sp,
                lineHeight = 26.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = AppStrings.CHAT_REPORT_GUIDE,
                color = grey400(),
                fontSize = 12.sp,
                lineHeight = 17.sp,
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
                        fontSize = 14.sp,
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
                    Text(AppStrings.CHAT_REPORT_CANCEL, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
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
                    Text(AppStrings.CHAT_REPORT_ACTION, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
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
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = white(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        Text(
            text = AppStrings.CHAT_EMOJI_SELECT,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            color = grey950(),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(8),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 320.dp)
                .padding(horizontal = 12.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
        ) {
            gridItems(AdditionalReactionEmojis) { emoji ->
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(CircleShape)
                        .clickable { onEmojiSelected(emoji) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = emoji, fontSize = 24.sp)
                }
            }
        }
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
        Text(text, color = color, fontSize = 12.sp)
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(17.dp),
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
internal fun ProfileImage(imageUrl: String?) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(white(), CircleShape)
            .border(1.dp, grey200(), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            modifier = Modifier.fillMaxWidth(),
            painter = painterResource(R.drawable.ic_person),
            contentDescription = null,
            contentScale = ContentScale.Fit,
        )
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = AppStrings.CHAT_CD_PROFILE_IMAGE,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
    }
}

@Composable
private fun MemberTag(text: String, background: Color, foreground: Color) {
    Text(
        text = text,
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(background)
            .padding(horizontal = 7.dp, vertical = 4.dp),
        color = foreground,
        fontSize = 10.sp,
        lineHeight = 12.sp,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
internal fun partTag(part: String): Triple<String, Color, Color> = when (part) {
    "IOS" -> Triple("iOS", yellow100(), yellow500())
    "ANDROID" -> Triple("Android", green100(), green700())
    "PLAN" -> Triple("PM", indigo100(), indigo600())
    "DESIGN" -> Triple("Design", red100(), red600())
    "WEB" -> Triple("Web", indigo100(), indigo600())
    "NODEJS" -> Triple("Node.js", green100(), green700())
    "SPRINGBOOT" -> Triple("Spring", green100(), green700())
    else -> Triple(part, grey100(), grey600())
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

private fun latestListItemIndex(
    messageCount: Int,
    pendingMessageCount: Int,
    hasMore: Boolean,
): Int = (
    messageCount + pendingMessageCount + if (hasMore) 1 else 0 - 1
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
            LocalDateTime.parse(createdAt).toInstant(ZoneOffset.UTC)
        }.getOrNull()
}

@Composable
private fun ChatInputBar(
    value: String,
    enabled: Boolean,
    replyingMessage: CommunityThreadMessage?,
    onValueChange: (String) -> Unit,
    onCamera: () -> Unit,
    onSend: () -> Unit,
) {
    Surface(color = grey100()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(10.dp),
        ) {
            if (replyingMessage != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = white(),
                    border = androidx.compose.foundation.BorderStroke(1.dp, grey200()),
                    shadowElevation = 7.dp,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 132.dp)
                            .padding(start = 20.dp, end = 12.dp, top = 16.dp, bottom = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            Text(
                                text = AppStrings.CHAT_REPLY_TO_FORMAT.format(
                                    replyingMessage.senderName.orEmpty().ifBlank { AppStrings.CHAT_ME },
                                ),
                                color = grey950(),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = replyingMessage.content.orEmpty().ifBlank {
                                    AppStrings.CHAT_MESSAGE_CONTENT
                                },
                                    color = grey400(),
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Spacer(Modifier.height(7.dp))
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.CenterStart,
                            ) {
                                if (value.isEmpty()) {
                                    Text(
                                        AppStrings.CHAT_REPLY_PLACEHOLDER,
                                        color = grey400(),
                                        fontSize = 17.sp,
                                    )
                                }
                                BasicTextField(
                                    value = value,
                                    onValueChange = onValueChange,
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = enabled,
                                    textStyle = LocalTextStyle.current.copy(
                                        color = black(),
                                        fontSize = 17.sp,
                                        lineHeight = 23.sp,
                                    ),
                                    cursorBrush = SolidColor(indigo500()),
                                    maxLines = 3,
                                )
                            }
                        }
                        IconButton(
                            onClick = onSend,
                            enabled = enabled && value.isNotBlank(),
                            modifier = Modifier.align(Alignment.Top),
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_send),
                                contentDescription = AppStrings.CHAT_CD_REPLY_SEND,
                                modifier = Modifier.size(34.dp),
                            tint = if (enabled && value.isNotBlank()) indigo500() else grey400(),
                            )
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 54.dp)
                        .clip(RoundedCornerShape(1000.dp))
                        .background(white())
                        .border(1.dp, grey200(), RoundedCornerShape(1000.dp)),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onCamera) {
                        Icon(
                            painterResource(R.drawable.ic_photo),
                            contentDescription = AppStrings.CHAT_CD_ATTACH_PHOTO,
                            tint = black(),
                        )
                    }
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        if (value.isEmpty()) {
                            Text(AppStrings.CHAT_MESSAGE_PLACEHOLDER, color = grey400(), fontSize = 14.sp)
                        }
                        BasicTextField(
                            value = value,
                            onValueChange = onValueChange,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = enabled,
                            textStyle = LocalTextStyle.current.copy(
                                color = black(),
                                fontSize = 14.sp,
                            ),
                            cursorBrush = SolidColor(black()),
                            maxLines = 4,
                        )
                    }
                    IconButton(
                        onClick = onSend,
                        enabled = enabled && value.isNotBlank(),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_send),
                            contentDescription = AppStrings.CHAT_CD_SEND,
                            tint = if (enabled && value.isNotBlank()) indigo500() else grey300(),
                        )
                    }
                }
            }
        }
    }
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
                    part = "IOS",
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
