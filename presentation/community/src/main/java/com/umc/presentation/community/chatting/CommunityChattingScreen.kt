package com.umc.presentation.community.chatting

import kotlinx.collections.immutable.toImmutableMap
import kotlinx.collections.immutable.toImmutableSet

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
import androidx.compose.foundation.selection.toggleable
import androidx.compose.ui.semantics.Role
import com.umc.presentation.community.component.chatting.CommunityChatMessageImage
import com.umc.presentation.community.component.chatting.CommunityChatImageLoadingIndicator
import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.umc.component.component.UText
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
import com.umc.component.theme.black
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.grey100
import com.umc.component.theme.grey200
import com.umc.component.theme.grey300
import com.umc.component.theme.grey400
import com.umc.component.theme.grey800
import com.umc.component.theme.grey950
import com.umc.component.theme.main100
import com.umc.component.theme.main200
import com.umc.component.theme.main300
import com.umc.component.theme.main500
import com.umc.component.theme.red400
import com.umc.component.theme.red100
import com.umc.component.theme.red500
import com.umc.component.theme.white
import com.umc.component.theme.grey50
import com.umc.component.theme.grey500
import com.umc.component.theme.grey600
import com.umc.component.theme.grey700
import com.umc.component.theme.main600
import com.umc.presentation.community.bottomsheet.ConversationSummaryBottomSheet
import com.umc.presentation.community.component.chatting.CommunityChatMemberTag
import com.umc.presentation.community.component.chatting.CommunityChatProfileImage
import com.umc.presentation.community.component.chatting.CommunityChatInputBar
import com.umc.presentation.community.component.chatting.CommunityChatEditInputBar
import com.umc.presentation.community.component.chatting.communityChatPartTag
import com.umc.presentation.community.component.chatting.communityMentionText
import com.umc.presentation.community.component.chatting.communityMentionNameRanges
import com.umc.domain.model.community.thread.CommunityMessageReportReason
import com.umc.domain.model.community.thread.CommunityMessageType
import com.umc.domain.model.community.thread.CommunityReaction
import com.umc.domain.model.community.thread.CommunityThreadDetail
import com.umc.domain.model.community.thread.CommunityThreadCategory
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.collections.immutable.toImmutableList

@OptIn(
    androidx.compose.foundation.ExperimentalFoundationApi::class,
    ExperimentalLayoutApi::class,
    ExperimentalMaterial3Api::class,
)
@Composable
fun CommunityChattingScreen(
    state: CommunityChattingState,
    toastData: UToastData? = null,
    toastGeneration: Int = 0,
    onToastDismiss: () -> Unit = {},
    onAction: (CommunityChattingAction) -> Unit,
) {
    val onBack = { onAction(CommunityChattingAction.OnBackClick) }
    val onMore = { onAction(CommunityChattingAction.OnMoreClick) }
    val onUnreadSummary = { onAction(CommunityChattingAction.OnUnreadSummaryClick) }
    val onCamera = { onAction(CommunityChattingAction.OnCameraClick) }
    val onSendImages: (List<String>) -> Unit = { onAction(CommunityChattingAction.OnSendImages(it)) }
    val onDraftChange: (String, Int, Int) -> Unit = { text, start, end ->
        onAction(CommunityChattingAction.OnDraftChanged(text, start, end))
    }
    val onLoadPrevious = { onAction(CommunityChattingAction.OnLoadPrevious) }
    val onDeleteMessage: (String) -> Unit = { onAction(CommunityChattingAction.OnDeleteMessage(it)) }
    val onReact: (CommunityThreadMessage, String) -> Unit = { message, emoji ->
        onAction(CommunityChattingAction.OnReact(message, emoji))
    }
    val onReportMessage: (String, Set<CommunityMessageReportReason>) -> Unit = { messageId, reason ->
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

    // 강퇴된 스레드(목록의 강퇴 카드에서 진입)는 참여 종료 안내 화면으로 이동
    if (state.isKicked) {
        KickedThreadScreen(
            onBack = onBack,
            onAcknowledge = { onAction(CommunityChattingAction.OnAcknowledgeKick) },
            isAcknowledging = state.isAcknowledgingKick,
            toastData = toastData,
            onToastDismiss = onToastDismiss,
        )
        return
    }

    if (state.isThreadUnavailable) {
        ThreadUnavailableScreen(onBack = onBack)
        return
    }

    if (state.isAccessDenied) {
        ThreadUnavailableScreen(
            onBack = onBack,
            topBarTitle = AppStrings.CHAT_ROOM_DEFAULT,
            title = AppStrings.CHAT_NO_ACCESS_TITLE,
            description = AppStrings.CHAT_NO_ACCESS_DESCRIPTION,
            buttonText = AppStrings.CHAT_NO_ACCESS_BACK,
        )
        return
    }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val navigationBarInsets = WindowInsets.navigationBars
    val imeBottom = WindowInsets.ime.getBottom(density)
    var isInputFocused by remember { mutableStateOf(false) }
    var inputClickGeneration by remember { mutableIntStateOf(0) }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var inputBounds by remember { mutableStateOf<Rect?>(null) }
    var inputOverlayHeightPx by remember { mutableIntStateOf(0) }
    val mentionMembers = remember(state.members, state.myMemberId) {
        state.members.values.filter { it.memberId != state.myMemberId }.toImmutableList()
    }
    val draftMentions = remember(state.draftMentions) { state.draftMentions.toImmutableList() }
    var screenPosition by remember { mutableStateOf(Offset.Zero) }
    val initialUnreadScrollOffset = with(density) {
        INITIAL_UNREAD_SCROLL_OFFSET.roundToPx()
    }
    val deletedMessageIds = remember(state.messages, state.knownDeletedMessageIds) {
        communityDeletedMessageIds(state.messages, state.knownDeletedMessageIds)
    }
    val displayedMessages = remember(state.messages, deletedMessageIds) {
        communityDisplayedMessages(
            state.messages, deletedMessageIds,
        )
    }
    // The load-more button may be the first visible item. Anchor to a message instead,
    // otherwise prepending history keeps that button (and the oldest page) at the top.
    var historyAnchor by remember { mutableStateOf<Pair<String, Int>?>(null) }
    var knownMessageIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var knownPendingMessageIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var newMessageCount by remember { mutableIntStateOf(0) }
    var unreadDividerMessageId by remember { mutableStateOf<String?>(null) }
    var followsLatestMessage by remember { mutableStateOf(true) }
    var wasUserScrolling by remember { mutableStateOf(false) }
    var selectedMessage by remember { mutableStateOf<CommunityThreadMessage?>(null) }
    var replyingMessage by remember { mutableStateOf<CommunityThreadMessage?>(null) }
    var navigationTarget by remember { mutableStateOf<String?>(null) }
    var navigationMessage by remember { mutableStateOf<String?>(null) }

    var emojiPickerMessage by remember { mutableStateOf<CommunityThreadMessage?>(null) }
    var pendingDeleteConfirmationId by remember { mutableStateOf<String?>(null) }
    var messageDeleteConfirmationId by remember(state.threadId) { mutableStateOf<String?>(null) }
    var showLeaveDialog by remember { mutableStateOf(false) }
    var reportingMessageId by remember { mutableStateOf<String?>(null) }
    var showThreadInfo by remember { mutableStateOf(false) }
    var pendingReportConfirmation by remember {
        mutableStateOf<Pair<String, Set<CommunityMessageReportReason>>?>(null)
    }
    // 참여하지 않았거나 나간 스레드는 읽기만 가능하다(서버 역할 없음 = UNKNOWN).
    val isViewOnly = state.thread?.myRole == CommunityThreadRole.UNKNOWN
    val navigationCursors = remember(navigationTarget) { hashSetOf<String?>() }
    LaunchedEffect(navigationTarget, state.messages, state.isLoadingMore, deletedMessageIds) {
        val target = navigationTarget ?: return@LaunchedEffect
        if (target in deletedMessageIds) {
            navigationTarget = null
            navigationMessage = null
            return@LaunchedEffect
        }
        val index = displayedMessages.indexOfFirst { it.messageId == target }
        when {
            index >= 0 -> {
                navigationMessage = null
                listState.scrollToItem(index + if (state.hasMore) 1 else 0)
                followsLatestMessage = false
                navigationTarget = null
            }
            state.isLoadingMore -> Unit
            state.hasMore -> {
                if (navigationCursors.add(state.nextBefore)) onLoadPrevious()
                else {
                    navigationMessage = "원문을 불러오지 못했어요. 다시 시도해주세요."
                    navigationTarget = null
                }
            }
            else -> {
                navigationMessage = "원문을 찾을 수 없어요."
                navigationTarget = null
            }
        }
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentMessages by rememberUpdatedState(state.messages)
    val currentOnAction by rememberUpdatedState(onAction)
    LaunchedEffect(listState, lifecycleOwner, state.messages, state.recoveryIncomplete) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            snapshotFlow {
                val visibleIds = listState.layoutInfo.visibleItemsInfo.map { it.key }.toSet()
                currentMessages.firstOrNull { it.messageId in visibleIds }?.messageId
            }.collect { id -> id?.let { currentOnAction(CommunityChattingAction.OnVisibleMessage(it)) } }
        }
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
            useCompactStyle = true,
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
        val isLastOwner = state.isLastRemainingOwner
        UBasicDialog(
            title = AppStrings.CHAT_DELETE_THREAD_TITLE,
            useCompactStyle = true,
            content = if (isLastOwner) AppStrings.CHAT_LAST_OWNER_LEAVE_DESCRIPTION else AppStrings.CHAT_DELETE_THREAD_DESCRIPTION,
            negativeText = AppStrings.CHAT_CANCEL,
            positiveText = if (isLastOwner) AppStrings.CHAT_DELETE_AND_LEAVE else AppStrings.CHAT_DELETE_ACTION,
            type = DialogType.ERROR,
            showCloseButton = false,
            negativeBackgroundColor = grey100(),
            negativeBorderColor = grey100(),
            negativeTextColor = grey700(),
            positiveBackgroundColor = red100(),
            positiveBorderColor = red100(),
            positiveTextColor = red500(),
            onNegative = { onAction(CommunityChattingAction.OnDismissDeleteThread) },
            onPositive = { onAction(CommunityChattingAction.OnConfirmDeleteThread) },
            onDismissRequest = { onAction(CommunityChattingAction.OnDismissDeleteThread) },
        )
    }

    if (
        state.canSummarize && (
            state.isSummarizingUnread ||
                state.unreadSummary != null ||
                state.unreadSummaryError != null
            )
    ) {
        ConversationSummaryBottomSheet(
            state = state,
            onShowSource = {
                onAction(CommunityChattingAction.OnDismissUnreadSummary)
                navigationTarget = state.summaryMessageIds.firstOrNull()
            },
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

    var lastRecoveryRevision by remember { mutableIntStateOf(state.recoveryRevision) }
    LaunchedEffect(state.messages, state.isLoadingMore) {
        val anchor = historyAnchor ?: return@LaunchedEffect
        if (state.isLoadingMore) return@LaunchedEffect
        val messageIndex = displayedMessages.indexOfFirst { it.messageId == anchor.first }
        if (messageIndex >= 0) {
            listState.scrollToItem(
                index = messageIndex + if (state.hasMore) 1 else 0,
                scrollOffset = anchor.second,
            )
        }
        if (historyAnchor == anchor) historyAnchor = null
    }

    LaunchedEffect(state.messages) {
        val currentIds = state.messages.mapTo(linkedSetOf()) { it.messageId }
        if (lastRecoveryRevision != state.recoveryRevision) {
            lastRecoveryRevision = state.recoveryRevision
            knownMessageIds = currentIds
            // Stable message keys retain the viewport; HTTP reconciliation must not jump to an own echo.
            return@LaunchedEffect
        }
        if (knownMessageIds.isEmpty()) {
            knownMessageIds = currentIds
            if (displayedMessages.isNotEmpty()) {
                val entryUnreadMessages = if (isViewOnly) emptyList() else communityEntryUnreadMessages(
                    state.messages, deletedMessageIds, state.unreadCountAtEntry,
                )
                val loadedUnreadCount = entryUnreadMessages.size
                val messageIndex = if (loadedUnreadCount > 0) {
                    val firstUnreadMessageIndex = displayedMessages.indexOfFirst {
                        it.messageId == entryUnreadMessages.last().messageId
                    }.coerceAtLeast(0)
                    newMessageCount = loadedUnreadCount
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

        // messages 는 최신순이라 알려진 메시지보다 앞쪽에 추가된 것만 새 메시지다(이전 페이지는 뒤쪽에 붙는다).
        val firstKnownIndex = state.messages.indexOfFirst { it.messageId in knownMessageIds }
        val newMessages = (if (firstKnownIndex < 0) {
            state.messages.filter { it.messageId !in knownMessageIds }
        } else {
            state.messages.take(firstKnownIndex)
        }).filterNot { it.messageId in deletedMessageIds }
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
                    .lastOrNull { it.senderId != state.myMemberId && !it.type.isSystemNotice }
                    ?.messageId
            }
            newMessageCount += newMessages.count { it.senderId != state.myMemberId && !it.type.isSystemNotice }
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

    LaunchedEffect(isInputFocused, inputClickGeneration, imeBottom) {
        if (!isInputFocused || isViewOnly || state.editingMessageId != null) return@LaunchedEffect
        // Wait for the keyboard animation and the list's bottom padding to settle.
        delay(120)
        newMessageCount = 0
        unreadDividerMessageId = null
        followsLatestMessage = true
        withFrameNanos { }
        val lastItemIndex = latestListItemIndex(
            messageCount = displayedMessages.size,
            pendingMessageCount = state.pendingMessages.size,
            hasMore = state.hasMore,
        )
        if (lastItemIndex >= 0) listState.animateScrollToItem(lastItemIndex)
    }

    val showUnreadSummary = state.canSummarize && !isViewOnly && newMessageCount > 0 &&
        !state.isLoading && state.errorMessage == null

    Scaffold(
        modifier = Modifier
            .onGloballyPositioned { screenPosition = it.positionInRoot() }
            .pointerInput(focusManager, keyboardController) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                    val bounds = inputBounds
                    if (bounds != null && !bounds.contains(down.position + screenPosition)) {
                        var isTap = true
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            if (event.changes.size > 1 ||
                                (change.position - down.position).getDistance() > viewConfiguration.touchSlop
                            ) {
                                isTap = false
                            }
                            if (!change.pressed) {
                                if (isTap && change.uptimeMillis - down.uptimeMillis <
                                    viewConfiguration.longPressTimeoutMillis
                                ) {
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                }
                                break
                            }
                        }
                    }
                }
            },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = grey100(),
        snackbarHost = {
            if (!showThreadInfo) key(toastGeneration) { UToastHost(
                data = toastData,
                onDismiss = onToastDismiss,
                modifier = Modifier.padding(bottom = 8.dp),
            ) }
        },
        topBar = {
            Column {
                ChatTopBar(
                    title = state.thread?.title.orEmpty().ifBlank { AppStrings.CHAT_ROOM_DEFAULT },
                    subtitle = state.thread?.let { "${it.category.chatLabel} · ${it.memberCount}명" }.orEmpty(),
                    canSummarize = state.canSummarize,
                    onBack = onBack,
                    isMuted = state.thread?.isMuted == true,
                    isPinned = state.thread?.isPinned == true,
                    isOwner = state.thread?.myRole == CommunityThreadRole.OWNER,
                    canManage = state.thread?.myRole in setOf(CommunityThreadRole.OWNER, CommunityThreadRole.ADMIN),
                    showMoreMenu = state.thread != null && !isViewOnly,
                    onSummary = onUnreadSummary,
                    onToggleMuted = onToggleMuted,
                    onTogglePinned = onTogglePinned,
                    onThreadInfo = {
                        showThreadInfo = true
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
                if (showUnreadSummary) {
                    UnreadSummaryCard(
                        unreadCount = newMessageCount,
                        onClick = onUnreadSummary,
                        modifier = Modifier
                            .background(grey100())
                            .padding(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 16.dp),
                    )
                }
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
            } else if (displayedMessages.isEmpty() && !state.hasMore && state.pendingMessages.isEmpty()) {
                EmptyChatMessages(
                    isViewOnly = isViewOnly,
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
                        start = 16.dp,
                        end = 16.dp,
                        top = if (showUnreadSummary) 0.dp else 18.dp,
                        bottom = maxOf(
                            with(density) { inputOverlayHeightPx.toDp() },
                            (if (state.editingMessageId != null) 82.dp else 32.dp) +
                                if (isInputFocused && imeBottom > 0) {
                                    with(density) { imeBottom.toDp() }
                                } else {
                                    navigationBarInsets.asPaddingValues().calculateBottomPadding()
                                },
                        ),
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
            if (state.hasMore) {
                item(key = "community-load-previous") {
                    TextButton(
                        enabled = !state.isLoadingMore && !state.isRecovering && !state.recoveryIncomplete,
                        onClick = {
                            val layout = listState.layoutInfo
                            val visibleMessages = displayedMessages.mapTo(hashSetOf()) { it.messageId }
                            val message = layout.visibleItemsInfo.firstOrNull {
                                val key = it.key
                                key is String && key in visibleMessages
                            }
                            val firstItem = layout.visibleItemsInfo.firstOrNull {
                                it.index == listState.firstVisibleItemIndex
                            }
                            if (message != null && firstItem != null) {
                                historyAnchor = (message.key as String) to (
                                    listState.firstVisibleItemScrollOffset + firstItem.offset - message.offset
                                )
                            }
                            onLoadPrevious()
                        },
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
                    if (!isViewOnly && message.messageId == unreadDividerMessageId) {
                        NewMessagesDivider()
                        Spacer(Modifier.height(14.dp))
                    }
                    ChatMessageRow(
                        onReplySourceClick = {
                            message.replyTo?.messageId?.takeUnless { it in deletedMessageIds }?.let {
                                navigationTarget = it
                            }
                        },
                        message = message,
                        imageUris = state.localImageUrisByMessageId[message.messageId].orEmpty(),
                        member = message.senderId?.let(state.members::get),
                        isMine = isMine,
                        isViewOnly = isViewOnly,
                        canDelete = isMine || state.thread?.myRole in setOf(CommunityThreadRole.OWNER, CommunityThreadRole.ADMIN),
                        onEdit = {
                            onAction(CommunityChattingAction.OnStartEdit(message.messageId))
                            selectedMessage = null
                        },
                        menuExpanded = selectedMessage?.messageId == message.messageId,
                        onLongClick = {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            selectedMessage = message
                        },
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
                            messageDeleteConfirmationId = message.messageId
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
                    mentionNames = pending.mentionedMemberIds.mapNotNull { state.members[it.toString()]?.name },
                    onRetry = { onRetryPending(pending.clientMessageId) },
                    onDismiss = { pendingDeleteConfirmationId = pending.clientMessageId },
                )
                }
            }
            }

            if (!isViewOnly && state.editingMessageId == null && newMessageCount > 0 && !state.isLoading && state.errorMessage == null) {
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
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(bottom = 100.dp),
                )
            }
            Column(
                modifier = Modifier.align(Alignment.BottomCenter)
                    .onGloballyPositioned { inputOverlayHeightPx = it.size.height },
            ) {
                SourceNavigationStatus(
                    message = navigationMessage,
                    onDismiss = { navigationMessage = null },
                )
                if (!state.isLoading && state.errorMessage == null && !isViewOnly) {
                    val editingId = state.editingMessageId
                    if (editingId != null) {
                        val originalText = remember(editingId) {
                            state.messages.firstOrNull { it.messageId == editingId }?.content.orEmpty()
                        }
                        val tooLong = isCommunityChatMessageTooLong(state.editDraft)
                        CommunityChatEditInputBar(
                            messageId = editingId,
                            originalText = originalText,
                            value = state.editDraft,
                            saving = state.editCommandId != null,
                            canSave = state.editCommandId == null && state.editDraft.isNotBlank() && !tooLong,
                            error = if (tooLong) AppStrings.CHAT_MESSAGE_TOO_LONG else state.editError,
                            onValueChange = { onAction(CommunityChattingAction.OnEditDraftChanged(it)) },
                            onSave = { onAction(CommunityChattingAction.OnSaveEdit) },
                            onCancel = {
                                focusManager.clearFocus()
                                keyboardController?.hide()
                                onAction(CommunityChattingAction.OnDismissEdit)
                            },
                            onInputFocusChanged = { isInputFocused = it },
                            modifier = Modifier.onGloballyPositioned { inputBounds = it.boundsInRoot() },
                        )
                    } else {
                    CommunityChatInputBar(
                        onInputFocusChanged = { isInputFocused = it },
                        onInputClick = { inputClickGeneration++ },
                        modifier = Modifier.onGloballyPositioned { inputBounds = it.boundsInRoot() },
                        value = state.draft,
                        mentionMembers = mentionMembers,
                        draftMentions = draftMentions,
                        onMentionSelected = { memberId, start, end ->
                            onAction(CommunityChattingAction.OnMentionSelected(memberId, start, end))
                        },
                        enabled = true,
                        replyingMessage = replyingMessage,
                        onValueChange = onDraftChange,
                        onCamera = {
                            onCamera()
                            imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        onSend = {
                            onAction(CommunityChattingAction.OnSendClick(
                                replyingMessage?.messageId,
                            ))
                            if (!isCommunityChatMessageTooLong(state.draft)) replyingMessage = null
                        },
                        onCancelReply = { replyingMessage = null },
                    )
                    }
                }
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

    messageDeleteConfirmationId?.let { messageId ->
        MessageDeleteConfirmationDialog(
            onConfirm = {
                messageDeleteConfirmationId = null
                onDeleteMessage(messageId)
            },
            onDismiss = { messageDeleteConfirmationId = null },
        )
    }

    pendingDeleteConfirmationId?.let { clientMessageId ->
        MessageDeleteConfirmationDialog(
            onConfirm = {
                pendingDeleteConfirmationId = null
                onDismissPending(clientMessageId)
            },
            onDismiss = { pendingDeleteConfirmationId = null },
        )
    }

    if (showLeaveDialog) {
        val isLastOwner = state.isLastRemainingOwner
        UBasicDialog(
            title = if (isLastOwner) AppStrings.CHAT_DELETE_THREAD_TITLE else AppStrings.CHAT_LEAVE_THREAD_TITLE,
            useCompactStyle = true,
            content = if (isLastOwner) AppStrings.CHAT_LAST_OWNER_LEAVE_DESCRIPTION else AppStrings.CHAT_LEAVE_THREAD_DESCRIPTION,
            negativeText = AppStrings.CHAT_CANCEL,
            positiveText = if (isLastOwner) AppStrings.CHAT_DELETE_AND_LEAVE else AppStrings.CHAT_LEAVE,
            type = DialogType.ERROR,
            showCloseButton = false,
            negativeBackgroundColor = grey100(),
            negativeBorderColor = grey100(),
            negativeTextColor = grey700(),
            positiveBackgroundColor = red100(),
            positiveBorderColor = red100(),
            positiveTextColor = red500(),
            onPositive = {
                showLeaveDialog = false
                if (isLastOwner) {
                    onAction(CommunityChattingAction.OnConfirmDeleteThread)
                } else {
                    onLeave()
                }
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
            useCompactStyle = true,
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

    if (showThreadInfo) {
        CommunityThreadInfoScreen(
            members = remember(state.members) { state.members.values.toImmutableList() },
            memberProfiles = remember(state.memberProfiles) { state.memberProfiles.toImmutableMap() },
            memberCount = state.thread?.memberCount.orEmpty(),
            myMemberId = state.myMemberId,
            isOwner = state.thread?.myRole == CommunityThreadRole.OWNER,
            canManage = state.thread?.myRole in setOf(CommunityThreadRole.OWNER, CommunityThreadRole.ADMIN),
            onBack = { showThreadInfo = false },
            onKickMember = onKickMember,
            onTransferOwnership = onTransferOwnership,
            onToggleMemberAdmin = { onAction(CommunityChattingAction.OnToggleMemberAdmin(it)) },
            onViewProfile = { onAction(CommunityChattingAction.OnViewMemberProfile(it)) },
            onManageSelected = { ids, action -> onAction(CommunityChattingAction.OnManageParticipants(ids, action)) },
            isManagingParticipants = state.isManagingParticipants,
            managementRevision = state.participantManagementRevision,
            managedParticipantIds = remember(state.managedParticipantIds) { state.managedParticipantIds.toImmutableSet() },
            toastGeneration = toastGeneration,
            toastData = toastData,
            onToastDismiss = onToastDismiss,
        )
    }
}

@Composable
private fun EmptyChatMessages(
    isViewOnly: Boolean,
    modifier: Modifier = Modifier,
) {
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
        if (!isViewOnly) {
            Spacer(Modifier.height(3.dp))
            Text(
                text = AppStrings.CHAT_NO_MESSAGES_DESCRIPTION,
                color = grey400(),
                style = UmcTypographyTokens.Subheadline
            )
        }
    }
}

@Composable
private fun ThreadUnavailableScreen(
    onBack: () -> Unit,
    topBarTitle: String = AppStrings.CHAT_UNAVAILABLE_TOP_BAR,
    title: String = AppStrings.CHAT_UNAVAILABLE_TITLE,
    description: String = AppStrings.CHAT_UNAVAILABLE_DESCRIPTION,
    buttonText: String = AppStrings.CHAT_BACK_TO_COMMUNITY,
) {
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
                        text = topBarTitle,
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
                text = title,
                color = grey800(),
                style = UmcTypographyTokens.HeadlineBold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = description,
                color = grey400(),
                style = UmcTypographyTokens.Subheadline
            )
            Spacer(Modifier.height(34.dp))
            UButton(
                text = buttonText,
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                backgroundColor = main500(),
                pressedColor = main600(),
                textColor = white(),
                textStyle = UmcTypographyTokens.HeadlineBold,
                cornerRadius = 8.dp,
            )
            Spacer(Modifier.height(100.dp))
        }
    }
}

/**
 * 강퇴된 스레드에 진입했을 때의 참여 종료 안내 화면 (Figma `커뮤니티_스레드_강퇴`)
 *
 * '커뮤니티로 돌아가기'를 누르면 강퇴 확인을 서버에 저장하고, 뒤로가기는 확인 없이 돌아간다.
 */
@Composable
private fun KickedThreadScreen(
    onBack: () -> Unit,
    onAcknowledge: () -> Unit,
    isAcknowledging: Boolean,
    toastData: UToastData? = null,
    onToastDismiss: () -> Unit = {},
) {
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
            // Figma `TopBar/Community` 참여종료
            Surface(color = white()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_community_back),
                            contentDescription = AppStrings.CHAT_CD_BACK,
                            modifier = Modifier.size(24.dp),
                            tint = grey950(),
                        )
                    }
                    UText(
                        text = AppStrings.CHAT_UNAVAILABLE_TOP_BAR,
                        color = grey950(),
                        style = UmcTypographyTokens.Title2Bold,
                    )
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 64.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // 아이콘 + 제목
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_community_block),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = grey400(),
                )
            }
            Spacer(Modifier.height(4.dp))
            UText(
                text = AppStrings.CHAT_UNAVAILABLE_TITLE,
                color = grey600(),
                style = UmcTypographyTokens.HeadlineBold,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(4.dp))
            UText(
                text = AppStrings.CHAT_KICKED_DESCRIPTION,
                modifier = Modifier.fillMaxWidth(),
                color = grey500(),
                style = UmcTypographyTokens.Subheadline,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(40.dp))
            UButton(
                text = AppStrings.CHAT_BACK_TO_COMMUNITY,
                onClick = onAcknowledge,
                enabled = !isAcknowledging,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .fillMaxWidth()
                    .height(52.dp),
                backgroundColor = main500(),
                pressedColor = main600(),
                textColor = white(),
                textStyle = UmcTypographyTokens.HeadlineBold,
                cornerRadius = 8.dp,
            )
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
            backgroundColor = main500(),
            pressedColor = main600(),
            textColor = white(),
            textStyle = UmcTypographyTokens.HeadlineBold,
            cornerRadius = 12.dp,
        )
    }
}

@Composable
private fun ChatTopBar(
    title: String,
    subtitle: String,
    canSummarize: Boolean,
    onBack: () -> Unit,
    isMuted: Boolean,
    isPinned: Boolean,
    isOwner: Boolean,
    canManage: Boolean,
    showMoreMenu: Boolean,
    onSummary: () -> Unit,
    onToggleMuted: () -> Unit,
    onTogglePinned: () -> Unit,
    onThreadInfo: () -> Unit,
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
                .height(67.dp)
                .padding(start = 4.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                Icon(
                    painterResource(R.drawable.ic_back),
                    contentDescription = AppStrings.CHAT_CD_BACK,
                    tint = grey950(),
                )
            }
            Column(
                modifier = Modifier.weight(1f).padding(start = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
            Text(
                text = title,
                color = grey950(),
                style = UmcTypographyTokens.Title2Bold.copy(
                    fontSize = 20.sp,
                    lineHeight = 25.sp,
                    letterSpacing = 0.38.sp,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = subtitle,
                color = grey400(),
                style = UmcTypographyTokens.Callout,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            }
            if (showMoreMenu) {
                Box {
                    IconButton(onClick = { menuExpanded = true }, modifier = Modifier.size(48.dp)) {
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
                    ThreadMenuSectionTitle(if (canSummarize) AppStrings.CHAT_MENU_SUMMARY_SETTINGS else "설정")
                    if (canSummarize) {
                    ThreadMenuItem(AppStrings.CHAT_MENU_SUMMARY) {
                        menuExpanded = false
                        onSummary()
                    }
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
                    if (canManage) {
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
                    ThreadMenuSectionTitle(AppStrings.CHAT_MENU_THREAD)
                    ThreadMenuItem(AppStrings.CHAT_THREAD_INFO) {
                        menuExpanded = false
                        onThreadInfo()
                    }
                    if (isOwner) {
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
            .background(main100())
            .border(1.dp, main200(), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_community_ai),
            contentDescription = null,
            modifier = Modifier.size(32.dp),
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp, end = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                AppStrings.CHAT_UNREAD_COUNT_FORMAT.format(unreadCount),
                color = grey950(),
                style = UmcTypographyTokens.SubheadlineBold
            )
            Text(
                AppStrings.CHAT_UNREAD_SUMMARY_DESCRIPTION,
                color = grey600(),
                style = UmcTypographyTokens.Footnote
            )
        }
        Icon(painterResource(R.drawable.ic_next), contentDescription = null, tint = grey400(), modifier = Modifier.size(24.dp))
    }
}

private const val COMMUNITY_DEEP_LINK_HOST = "https://api.university.neordinary.com"
private const val COMMUNITY_DEEP_LINK_PATH = "/community/threads"
private val INITIAL_UNREAD_SCROLL_OFFSET = 96.dp
private val MESSAGE_TIME_MIN_WIDTH = 32.dp

private val CommunityThreadCategory.chatLabel: String
    get() = when (this) {
        CommunityThreadCategory.STUDY -> "스터디"
        CommunityThreadCategory.QNA -> "질문"
        CommunityThreadCategory.PROJECT -> "프로젝트"
        CommunityThreadCategory.FREE -> "자유"
        CommunityThreadCategory.UNKNOWN -> "기타"
    }

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
        color = main500(),
        contentColor = white(),
        shadowElevation = 4.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_dropdown),
                contentDescription = null,
                tint = white(),
                modifier = Modifier.size(24.dp),
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
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        HorizontalDivider(Modifier.weight(1f), color = main300())
        Text(
            text = AppStrings.CHAT_NEW_MESSAGE_DIVIDER,
            color = main500(),
            style = UmcTypographyTokens.FootnoteBold,
        )
        HorizontalDivider(Modifier.weight(1f), color = main300())
    }
}

@Composable
private fun ChatMessageRow(
    onReplySourceClick: () -> Unit,
    message: CommunityThreadMessage,
    imageUris: List<String>,
    member: CommunityThreadMember?,
    isMine: Boolean,
    isViewOnly: Boolean,
    canDelete: Boolean,
    onEdit: () -> Unit,
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
    if (message.type.isSystemNotice) {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(
                text = message.content.orEmpty(),
                style = UmcTypographyTokens.Footnote,
                color = grey500(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.background(grey200(), RoundedCornerShape(1000.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }
        return
    }
    val bubbleCoordinates = remember(message.messageId) { arrayOfNulls<LayoutCoordinates>(1) }
    var bubbleBounds by remember(message.messageId) { mutableStateOf<IntRect?>(null) }
    val bubbleModifier = Modifier.onGloballyPositioned { bubbleCoordinates[0] = it }
    val openMenu = {
        bubbleBounds = bubbleCoordinates[0]?.takeIf { it.isAttached }?.boundsInWindow()?.let { bounds ->
            IntRect(bounds.left.toInt(), bounds.top.toInt(), bounds.right.toInt(), bounds.bottom.toInt())
        }
        onLongClick()
    }
    Box(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (isMine) {
                MineMessage(message, imageUris, openMenu, selected = menuExpanded, bubbleModifier = bubbleModifier, onReplySourceClick = onReplySourceClick)
            } else {
                OtherMessage(message, imageUris, member, openMenu, selected = menuExpanded, bubbleModifier = bubbleModifier, onReplySourceClick = onReplySourceClick)
            }
            if (message.reactions.isNotEmpty()) {
                MessageReactions(
                    reactions = message.reactions,
                    isMine = isMine,
                    enabled = !isViewOnly,
                    onReactionClick = onReact,
                )
            }
        }
        val anchor = bubbleBounds
        if (menuExpanded && anchor != null && message.deletedAt == null && message.status != "DELETED") {
            MessageActionPopup(
                messageBounds = anchor,
                isMine = isMine,
                isViewOnly = isViewOnly,
                canDelete = canDelete,
                canEdit = isMine && message.type == CommunityMessageType.TEXT,
                onEdit = onEdit,
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
    enabled: Boolean = true,
    onReactionClick: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = if (isMine) 0.dp else 48.dp,
                end = 0.dp,
                top = 5.dp,
            ),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start,
    ) {
        reactions.forEach { reaction ->
            key(reaction.emoji) {
            val interactionSource = remember { MutableInteractionSource() }
            Surface(
                modifier = Modifier
                    .padding(end = 5.dp).clip(CircleShape)
                    .clickable(enabled = enabled, interactionSource = interactionSource,
                        indication = androidx.compose.material3.ripple()) { onReactionClick(reaction.emoji) },
                shape = CircleShape,
                color = white(),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    grey200(),
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
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun OtherMessage(
    message: CommunityThreadMessage,
    imageUris: List<String>,
    member: CommunityThreadMember?,
    onLongClick: () -> Unit,
    selected: Boolean = false,
    bubbleModifier: Modifier = Modifier,
    onReplySourceClick: () -> Unit = {},
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        CommunityChatProfileImage(member?.profileImageUrl, useThreadStyle = true)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp),
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
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Surface(
                    modifier = Modifier.weight(1f, fill = false).then(bubbleModifier),
                    color = when {
                        message.type == CommunityMessageType.IMAGE -> Color.Transparent
                        selected -> grey200()
                        else -> white()
                    },
                    shape = RoundedCornerShape(12.dp),
                ) {
                    MessageBubbleContent(
                        message = message,
                        imageUris = imageUris,
                        isMine = false,
                        onLongClick = onLongClick,
                        modifier = Modifier
                            .widthIn(max = 256.dp)
                            .combinedClickable(
                                onClick = {
                                    if (message.replyTo != null && message.deletedAt == null && message.status != "DELETED") {
                                        onReplySourceClick()
                                    }
                                },
                                onLongClick = onLongClick,
                            )
                            .then(
                                if (message.type == CommunityMessageType.IMAGE) Modifier
                                else Modifier.padding(12.dp)
                            ),
                    )
                }
                MessageTime(
                    message = message,
                    modifier = Modifier.padding(start = 8.dp, bottom = 2.dp),
                )
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
    selected: Boolean = false,
    bubbleModifier: Modifier = Modifier,
    onReplySourceClick: () -> Unit = {},
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.Bottom,
    ) {
        MessageTime(
            message = message,
            modifier = Modifier.padding(end = 8.dp, bottom = 2.dp),
            alignEnd = true,
        )
        Surface(
            modifier = bubbleModifier,
            color = when {
                message.type == CommunityMessageType.IMAGE -> Color.Transparent
                selected -> main600()
                else -> main500()
            },
            shape = RoundedCornerShape(12.dp),
        ) {
            MessageBubbleContent(
                message = message,
                imageUris = imageUris,
                isMine = true,
                onLongClick = onLongClick,
                modifier = Modifier
                    .widthIn(max = 256.dp)
                    .combinedClickable(
                        onClick = {
                            if (message.replyTo != null && message.deletedAt == null && message.status != "DELETED") {
                                onReplySourceClick()
                            }
                        },
                        onLongClick = onLongClick,
                    )
                    .then(
                        if (message.type == CommunityMessageType.IMAGE) Modifier
                        else Modifier.padding(12.dp)
                    ),
            )
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun MessageBubbleContent(
    message: CommunityThreadMessage,
    imageUris: List<String> = emptyList(),
    isMine: Boolean,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
) {
    if (message.deletedAt != null || message.status == "DELETED") {
        UText("삭제된 메시지예요.", modifier = modifier.padding(12.dp))
        return
    }
    var imageViewerIndex by remember(message.messageId) { mutableStateOf<Int?>(null) }
    val foreground = if (isMine) white() else grey950()
    val secondary = if (isMine) main100() else grey400()
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
            CommunityChatMessageImage(
                model = models.first(),
                modifier = modifier.width(256.dp).aspectRatio(1f)
                    .combinedClickable(onClick = { imageViewerIndex = 0 }, onLongClick = onLongClick),
            )
        } else if (models.isNotEmpty()) {
            ChatImageGrid(
                models = models,
                modifier = modifier,
                onImageClick = { imageViewerIndex = it },
                onLongClick = onLongClick,
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

    Column(modifier = modifier) {
        if (reply != null) {
            Column {
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
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                color = divider,
            )
        }
        Text(
            text = communityMentionText(
                message.content.orEmpty(),
                communityMentionNameRanges(message.content.orEmpty(), message.mentions.map { it.name }),
                if (isMine) main100() else main500(),
            ),
            color = foreground,
            style = UmcTypographyTokens.Subheadline
        )
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun ChatImageGrid(
    models: List<String>,
    modifier: Modifier = Modifier,
    onImageClick: (Int) -> Unit = {},
    onLongClick: (() -> Unit)? = null,
    showLoadingIndicator: Boolean = true,
) {
    @Composable
    fun Cell(model: String, modifier: Modifier, onLongClick: (() -> Unit)? = null, onClick: () -> Unit = {}) {
        ChatGridImage(model, modifier, onLongClick, showLoadingIndicator, onClick)
    }
    val images = models.take(COMMUNITY_CHAT_MAX_IMAGE_COUNT)
    val containerModifier = modifier.width(220.dp).clip(RoundedCornerShape(12.dp))

    when (images.size) {
        0 -> Unit
        1 -> Cell(images.first(), containerModifier.height(180.dp), onLongClick) { onImageClick(0) }
        2 -> Row(
            modifier = containerModifier.height(160.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            images.forEachIndexed { index, model ->
                Cell(model, Modifier.weight(1f).fillMaxHeight(), onLongClick) { onImageClick(index) }
            }
        }
        3 -> Row(
            modifier = containerModifier.height(220.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Cell(images.first(), Modifier.weight(1f).fillMaxHeight(), onLongClick) { onImageClick(0) }
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                images.drop(1).forEachIndexed { index, model ->
                    Cell(model, Modifier.weight(1f).fillMaxWidth(), onLongClick) { onImageClick(index + 1) }
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
                        Cell(model, Modifier.weight(1f).aspectRatio(1f), onLongClick) {
                            onImageClick(imageIndex)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun ChatGridImage(
    model: String,
    modifier: Modifier,
    onLongClick: (() -> Unit)? = null,
    showLoadingIndicator: Boolean = true,
    onClick: () -> Unit = {},
) {
    CommunityChatMessageImage(
        model = model,
        showLoadingIndicator = showLoadingIndicator,
        modifier = modifier.combinedClickable(onClick = onClick, onLongClick = onLongClick),
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
                .background(black()),
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
    messageBounds: IntRect,
    isMine: Boolean,
    isViewOnly: Boolean = false,
    canDelete: Boolean,
    canEdit: Boolean,
    onEdit: () -> Unit,
    onDismiss: () -> Unit,
    onReply: () -> Unit,
    onCopy: () -> Unit,
    onReport: () -> Unit,
    onDelete: () -> Unit,
    onReact: (String) -> Unit,
    onShowMoreEmojis: () -> Unit,
) {
    val density = LocalDensity.current
    val popupWidth = (LocalConfiguration.current.screenWidthDp.dp - 88.dp)
        .coerceIn(180.dp, 324.dp)
    val popupPositionProvider = remember(isMine, density, messageBounds) {
        val gap = with(density) { 16.dp.roundToPx() }
        val screenMargin = with(density) { 16.dp.roundToPx() }
        val endInset = with(density) { 8.dp.roundToPx() }

        object : PopupPositionProvider {
            override fun calculatePosition(
                anchorBounds: IntRect,
                windowSize: IntSize,
                layoutDirection: LayoutDirection,
                popupContentSize: IntSize,
            ): IntOffset {
                val desiredX = if (isMine) {
                    messageBounds.right - endInset - popupContentSize.width
                } else {
                    messageBounds.left
                }
                val maxX = (windowSize.width - popupContentSize.width - screenMargin)
                    .coerceAtLeast(screenMargin)
                val x = desiredX.coerceIn(screenMargin, maxX)

                val maxY = (windowSize.height - popupContentSize.height - screenMargin)
                    .coerceAtLeast(screenMargin)
                val belowY = messageBounds.bottom + gap
                val aboveY = messageBounds.top - gap - popupContentSize.height
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
            modifier = Modifier.width(popupWidth),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = if (isMine) Alignment.End else Alignment.Start,
        ) {
            Surface(
                modifier = Modifier.width(180.dp),
                shape = RoundedCornerShape(16.dp),
                color = white(),
                shadowElevation = 6.dp,
            ) {
                Column(modifier = Modifier.padding(vertical = 2.dp)) {
                    if (!isViewOnly) {
                        MessageActionItem(AppStrings.CHAT_ACTION_REPLY, R.drawable.ic_forward_circle, onClick = onReply)
                    }
                    if (!isViewOnly && canEdit) {
                        MessageActionItem("수정", R.drawable.ic_edit, onClick = onEdit)
                    }
                    MessageActionItem(AppStrings.CHAT_ACTION_COPY, R.drawable.ic_document, onClick = onCopy)
                    if (!isMine) MessageActionItem(AppStrings.CHAT_ACTION_REPORT, R.drawable.ic_warning, onClick = onReport)
                    if (!isViewOnly && canDelete) {
                        MessageActionItem(
                            text = AppStrings.CHAT_DELETE,
                            iconRes = R.drawable.ic_trash_can,
                            color = red500(),
                            onClick = onDelete,
                        )
                    }
                }
            }
            if (!isViewOnly) Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = CircleShape,
                color = white(),
                shadowElevation = 6.dp,
            ) {
                Row(
                    modifier = Modifier.height(64.dp).padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    listOf("❤️", "👍", "✅", "🥲", "😆").forEach { emoji ->
                        Box(
                            modifier = Modifier.weight(1f).height(48.dp)
                                .clip(CircleShape)
                                .clickable { onReact(emoji) },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = emoji,
                                fontSize = 24.sp,
                            )
                        }
                    }
                    IconButton(
                        onClick = onShowMoreEmojis,
                        modifier = Modifier.weight(1f).height(48.dp),
                    ) {
                        Icon(
                            painterResource(R.drawable.ic_plus),
                            contentDescription = AppStrings.CHAT_CD_OTHER_EMOJI,
                            tint = grey950(),
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageDeleteConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    UBasicDialog(
        title = AppStrings.CHAT_DELETE_MESSAGE_TITLE,
        negativeText = AppStrings.CHAT_CANCEL,
        positiveText = AppStrings.CHAT_DELETE_ACTION,
        type = DialogType.ERROR,
        showCloseButton = false,
        useCompactStyle = true,
        negativeBackgroundColor = grey100(),
        negativeBorderColor = grey100(),
        negativeTextColor = grey700(),
        positiveBackgroundColor = red100(),
        positiveBorderColor = red100(),
        positiveTextColor = red500(),
        onPositive = onConfirm,
        onNegative = onDismiss,
        onDismissRequest = onDismiss,
    )
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
    onReport: (Set<CommunityMessageReportReason>) -> Unit,
) {
    var selectedReasons by remember { mutableStateOf(emptySet<CommunityMessageReportReason>()) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss, sheetState = sheetState, containerColor = white(),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp), scrimColor = grey950().copy(alpha = 0.6f),
        dragHandle = {
            Box(Modifier.fillMaxWidth().height(52.dp)) {
                Box(Modifier.align(Alignment.TopCenter).padding(top = 16.dp).size(32.dp, 4.dp)
                    .background(grey600(), RoundedCornerShape(100.dp)))
            }
        },
    ) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(start = 16.dp, end = 16.dp, bottom = 24.dp)) {
            Text(AppStrings.CHAT_REPORT_REASON_TITLE, color = grey950(), style = UmcTypographyTokens.Title3Bold)
            Spacer(Modifier.height(8.dp))
            Text(AppStrings.CHAT_REPORT_GUIDE, color = grey600(), style = UmcTypographyTokens.Subheadline)
            Spacer(Modifier.height(16.dp))
            MessageReportReasons.forEach { (reason, label) ->
                Column(Modifier.fillMaxWidth().height(56.dp)) {
                    Row(Modifier.fillMaxWidth().weight(1f).toggleable(
                        value = reason in selectedReasons, role = Role.Checkbox,
                        onValueChange = { checked -> selectedReasons = if (checked) selectedReasons + reason else selectedReasons - reason },
                    ), verticalAlignment = Alignment.CenterVertically) {
                        Text(label, modifier = Modifier.weight(1f), color = grey950(), style = UmcTypographyTokens.Body)
                        Checkbox(checked = reason in selectedReasons, onCheckedChange = null, modifier = Modifier.size(24.dp),
                            colors = CheckboxDefaults.colors(checkedColor = main500(), uncheckedColor = grey400(), checkmarkColor = white()))
                    }
                    HorizontalDivider(color = grey200())
                }
            }
            Spacer(Modifier.height(32.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                UButton(text = AppStrings.CHAT_REPORT_CANCEL, onClick = onDismiss, modifier = Modifier.weight(1f).height(52.dp),
                    backgroundColor = grey100(), textColor = grey700(), textStyle = UmcTypographyTokens.HeadlineBold)
                UButton(text = AppStrings.CHAT_REPORT_ACTION, onClick = { onReport(selectedReasons.toSet()) },
                    enabled = selectedReasons.isNotEmpty(), modifier = Modifier.weight(1f).height(52.dp),
                    backgroundColor = red100(), textColor = red500(), textStyle = UmcTypographyTokens.HeadlineBold)
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
            .heightIn(min = 48.dp)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text, color = color, style = UmcTypographyTokens.Subheadline)
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp),
        )
    }
}

@Composable
private fun PendingMessageRow(
    message: PendingCommunityMessage,
    mentionNames: List<String>,
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
                color = if (failed) red500() else main500(),
                style = UmcTypographyTokens.Footnote,
            )
            Surface(
                color = when {
                    isImage -> Color.Transparent
                    failed -> red100()
                    else -> main500()
                },
                shape = RoundedCornerShape(if (isImage) 16.dp else 12.dp),
                border = if (failed) {
                    androidx.compose.foundation.BorderStroke(1.dp, red500())
                } else {
                    null
                },
            ) {
                if (message.type == CommunityMessageType.IMAGE && message.localUris.size == 1) {
                    CommunityChatMessageImage(
                        model = message.localUris.first(), modifier = Modifier.width(256.dp).aspectRatio(1f),
                        isSending = !failed, transferFailed = failed,
                    )
                } else if (message.type == CommunityMessageType.IMAGE) {
                    Box(contentAlignment = Alignment.Center) {
                        ChatImageGrid(models = message.localUris, showLoadingIndicator = false)
                        if (!failed) CommunityChatImageLoadingIndicator()
                    }
                } else {
                    Text(
                        communityMentionText(
                            message.content,
                            communityMentionNameRanges(message.content, mentionNames),
                            if (failed) main500() else main100(),
                        ),
                        modifier = Modifier
                            .widthIn(max = 256.dp)
                            .padding(12.dp),
                        color = if (failed) red500() else white(),
                        style = UmcTypographyTokens.Subheadline,
                    )
                }
            }
        }
        if (failed) {
            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                IconButton(
                    onClick = onRetry,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_refresh),
                        contentDescription = AppStrings.CHAT_CD_RETRY_MESSAGE,
                        tint = grey950(),
                        modifier = Modifier.size(20.dp),
                    )
                }
                IconButton(
                    onClick = onDismiss,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_close_big),
                        contentDescription = AppStrings.CHAT_CD_DELETE_FAILED_MESSAGE,
                        tint = red500(),
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageTime(
    message: CommunityThreadMessage,
    modifier: Modifier = Modifier,
    alignEnd: Boolean = false,
) {
    Column(
        modifier = modifier.widthIn(min = MESSAGE_TIME_MIN_WIDTH),
        horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        if (
            message.type == CommunityMessageType.TEXT && !message.editedAt.isNullOrBlank() &&
            message.deletedAt == null && message.status != "DELETED"
        ) {
            Text(
                text = AppStrings.CHAT_MESSAGE_EDITED,
                color = grey400(),
                style = UmcTypographyTokens.Footnote,
                maxLines = 1,
                softWrap = false,
            )
        }
        Text(
            formatCommunityCreatedAt(message.createdAt),
            color = grey400(),
            style = UmcTypographyTokens.Footnote,
            maxLines = 1,
            softWrap = false,
        )
    }
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

@PreviewScreenSizes
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

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun CommunityChattingScreenViewOnlyPreview() {
    CommunityChattingScreen(
        state = CommunityChattingState(
            threadId = "1",
            thread = CommunityThreadDetail(
                threadId = "1",
                title = "나간 스레드",
                myRole = CommunityThreadRole.UNKNOWN,
                unreadCount = "3",
            ),
            unreadCountAtEntry = 1,
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

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun CommunityChattingScreenViewOnlyEmptyPreview() {
    CommunityChattingScreen(
        state = CommunityChattingState(
            threadId = "1",
            thread = CommunityThreadDetail(
                threadId = "1",
                title = "나간 스레드",
                myRole = CommunityThreadRole.UNKNOWN,
            ),
            myMemberId = "10",
        ),
        onAction = {},
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun CommunityChattingScreenNoAccessPreview() {
    CommunityChattingScreen(
        state = CommunityChattingState(threadId = "1", isAccessDenied = true),
        onAction = {},
    )
}

/**
 * 강퇴된 스레드에 진입했을 때의 참여 종료 화면 Preview (Figma `커뮤니티_스레드_강퇴`)
 */
@Preview(showBackground = true, widthDp = 412, heightDp = 917)
@Composable
private fun CommunityChattingScreenKickedPreview() {
    CommunityChattingScreen(
        state = CommunityChattingState(threadId = "1", isKicked = true),
        onAction = {},
    )
}

@Composable
private fun SourceNavigationStatus(
    message: String?,
    onDismiss: () -> Unit,
) {
    message?.let { TextButton(onClick = onDismiss) { UText(it) } }
}

@Preview(showBackground = true, widthDp = 320)
@Composable
private fun SourceNavigationStatusPreview() {
    Column {
        SourceNavigationStatus("원문을 불러오지 못했어요. 다시 시도해주세요.", {})
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun CommunityChattingEditFailurePreview() {
    CommunityChattingScreen(
        state = CommunityChattingState(
            threadId = "1",
            thread = CommunityThreadDetail(threadId = "1", title = "스터디", myRole = CommunityThreadRole.OWNER),
            editingMessageId = "1",
            editDraft = "실패해도 유지되는 편집 내용",
            editError = "저장하지 못했어요. 다시 시도해주세요.",
        ),
        onAction = {},
    )
}
