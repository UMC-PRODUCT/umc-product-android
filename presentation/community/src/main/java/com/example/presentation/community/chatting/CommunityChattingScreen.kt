package com.example.presentation.community.chatting

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
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.umc.component.theme.black
import com.umc.component.R
import com.umc.component.component.DialogType
import com.umc.component.component.UBasicDialog
import com.umc.component.component.UDialog
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
import com.umc.component.theme.grey900
import com.umc.component.theme.indigo600
import com.umc.component.theme.red600
import com.umc.component.theme.yellow100
import com.umc.component.theme.yellow500
import com.umc.component.theme.yellow600
import com.umc.domain.model.community.thread.*
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Composable
fun CommunityChattingRoute(
    onBack: () -> Unit,
    onMore: () -> Unit = {},
    onUnreadSummary: () -> Unit = {},
    onCamera: () -> Unit = {},
    viewModel: CommunityChattingViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                CommunityChattingEvent.ThreadUnavailable -> onBack()
                CommunityChattingEvent.MessageReported -> snackbarHostState.showSnackbar(
                    message = "신고가 정상적으로 접수되었습니다.",
                    duration = SnackbarDuration.Short,
                )
                is CommunityChattingEvent.ShowError -> Unit
            }
        }
    }

    CommunityChattingScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onMore = onMore,
        onUnreadSummary = onUnreadSummary,
        onCamera = onCamera,
        onSendImage = viewModel::sendImage,
        onDraftChange = viewModel::updateDraft,
        onSend = { replyToId -> viewModel.sendText(state.draft, replyToId = replyToId) },
        onLoadPrevious = viewModel::loadPreviousMessages,
        onDeleteMessage = viewModel::deleteMessage,
        onReact = viewModel::toggleReaction,
        onToggleMuted = viewModel::toggleMuted,
        onTogglePinned = viewModel::togglePinned,
        onLeave = viewModel::leaveThread,
        onRetryPending = viewModel::retryPendingMessage,
        onDismissPending = viewModel::dismissPendingMessage,
        onRetryLoad = viewModel::bootstrap,
        onKickMember = viewModel::kickMember,
        onReportMessage = viewModel::reportMessage,
    )
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun CommunityChattingScreen(
    state: CommunityChattingUiState,
    snackbarHostState: SnackbarHostState? = null,
    onBack: () -> Unit,
    onMore: () -> Unit,
    onUnreadSummary: () -> Unit,
    onCamera: () -> Unit,
    onSendImage: (String) -> Unit = {},
    onDraftChange: (String) -> Unit,
    onSend: (Long?) -> Unit,
    onLoadPrevious: () -> Unit,
    onDeleteMessage: (String) -> Unit = {},
    onReact: (CommunityThreadMessage, String) -> Unit = { _, _ -> },
    onReportMessage: (String, CommunityMessageReportReason) -> Unit = { _, _ -> },
    onToggleMuted: () -> Unit = {},
    onTogglePinned: () -> Unit = {},
    onLeave: () -> Unit = {},
    onRetryPending: (String) -> Unit = {},
    onDismissPending: (String) -> Unit = {},
    onRetryLoad: () -> Unit = {},
    onInviteParticipants: () -> Unit = {},
    onEditThread: () -> Unit = {},
    onDeleteThread: () -> Unit = {},
    onKickMember: (String) -> Unit = {},
) {
    if (state.isThreadUnavailable) {
        ThreadUnavailableScreen(onBack = onBack)
        return
    }

    val listState = rememberLazyListState()
    val displayedMessages = remember(state.messages) { state.messages.asReversed() }
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
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        uri?.let { onSendImage(it.toString()) }
    }

    if (showParticipants) {
        ParticipantManagementScreen(
            members = state.members.values.toList(),
            memberCount = state.thread?.memberCount.orEmpty(),
            myMemberId = state.myMemberId,
            isOwner = state.thread?.myRole == CommunityThreadRole.OWNER,
            onBack = { showParticipants = false },
            onKickMember = onKickMember,
        )
        return
    }

    LaunchedEffect(displayedMessages.size, state.pendingMessages.size) {
        val count = displayedMessages.size + state.pendingMessages.size + 1
        if (count > 1) listState.animateScrollToItem(count - 1)
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
                title = state.thread?.title.orEmpty().ifBlank { "채팅방" },
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
                        context.startActivity(
                            Intent.createChooser(
                                Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, shareUrl)
                                },
                                "스레드 링크 공유",
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
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 20.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                val unread = state.thread?.unreadCount?.toIntOrNull() ?: 0
                if (unread > 0) {
                    UnreadSummaryCard(unreadCount = unread, onClick = onUnreadSummary)
                    Spacer(Modifier.height(20.dp))
                }
            }

            if (state.hasMore) {
                item {
                    TextButton(
                        onClick = onLoadPrevious,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(if (state.isLoadingMore) "불러오는 중..." else "이전 메시지 보기")
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
            title = "메시지를 삭제하시겠습니까?",
            negativeText = "취소",
            positiveText = "삭제하기",
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
            title = "스레드에서 나가시겠습니까?",
            content = "대화 목록에서 사라지고 알림도 꺼져요.",
            negativeText = "취소",
            positiveText = "나가기",
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
            title = "신고하시겠습니까?",
            subtitle = "신고하신 내용은 관리자가 검토 후\n운영 정책에 따라 처리됩니다.",
            isTwoButton = true,
            negativeText = "취소",
            positiveText = "신고하기",
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
            text = "아직 메시지가 없어요",
            color = grey800(),
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = "첫 메시지를 남겨 대화를 시작해보세요.",
            color = grey400(),
            fontSize = 12.sp,
            lineHeight = 17.sp,
        )
    }
}

@Composable
private fun ParticipantManagementScreen(
    members: List<CommunityThreadMember>,
    memberCount: String,
    myMemberId: String,
    isOwner: Boolean,
    onBack: () -> Unit,
    onKickMember: (String) -> Unit,
) {
    BackHandler(onBack = onBack)
    var pendingKickMember by remember { mutableStateOf<CommunityThreadMember?>(null) }
    val sortedMembers = remember(members) {
        members.sortedWith(
            compareBy<CommunityThreadMember> { it.role != CommunityThreadRole.OWNER }
                .thenBy { it.name },
        )
    }

    Scaffold(
        containerColor = white(),
        topBar = {
            Surface(color = white()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(64.dp)
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_back),
                            contentDescription = "뒤로가기",
                            tint = grey950(),
                        )
                    }
                    Text(
                        text = "참여자 관리",
                        modifier = Modifier.padding(start = 2.dp),
                        color = grey950(),
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        ) {
            item {
                Text(
                    text = "총 ${memberCount.ifBlank { members.size.toString() }}명",
                    color = grey950(),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 10.dp),
                )
            }
            items(sortedMembers, key = { it.memberId }) { member ->
                ParticipantRow(
                    member = member,
                    isMe = member.memberId == myMemberId,
                    showManagement = isOwner,
                    onKick = { pendingKickMember = member },
                )
            }
            if (isOwner) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(yellow100())
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text("!", color = yellow500(), fontWeight = FontWeight.Bold)
                        Text(
                            text = "개설자만 참여자를 내보낼 수 있어요",
                            color = yellow600(),
                            fontSize = 12.sp,
                        )
                    }
                }
            }
        }
    }

    pendingKickMember?.let { member ->
        UBasicDialog(
            title = "${member.name.ifBlank { "해당 참여자" }}님을 내보낼까요?",
            content = "이 스레드에서 나가지며,\n다시 초대해야 참여할 수 있어요",
            negativeText = "취소",
            positiveText = "내보내기",
            type = DialogType.WARNING,
            showCloseButton = false,
            negativeBackgroundColor = grey100(),
            negativeBorderColor = grey100(),
            positiveBackgroundColor = red100(),
            positiveBorderColor = red100(),
            positiveTextColor = red500(),
            onPositive = {
                onKickMember(member.memberId)
                pendingKickMember = null
            },
            onNegative = { pendingKickMember = null },
            onDismissRequest = { pendingKickMember = null },
        )
    }
}

@Composable
private fun ParticipantRow(
    member: CommunityThreadMember,
    isMe: Boolean,
    showManagement: Boolean,
    onKick: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 62.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ProfileImage(member?.profileImageUrl)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 10.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = member.name.ifBlank { "알 수 없음" },
                    color = grey950(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                if (member.role == CommunityThreadRole.OWNER) {
                    ParticipantBadge("개설자", grey800(), white())
                }
                if (isMe) {
                    ParticipantBadge("나", indigo100(), indigo500())
                }
            }
            val part = member.part?.let { partTag(it).first }.orEmpty()
            val detail = listOfNotNull(
                part.takeIf(String::isNotBlank),
                member.generation?.takeIf { it.isNotBlank() }?.let { "${it}기" },
            ).joinToString(" · ")
            if (detail.isNotBlank()) {
                Text(detail, color = grey400(), fontSize = 11.sp)
            }
        }
        if (showManagement) {
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_menu_kebab),
                        contentDescription = "참여자 관리 메뉴",
                        tint = grey400(),
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier.width(160.dp),
                    shape = RoundedCornerShape(16.dp),
                    containerColor = white(),
                    shadowElevation = 8.dp,
                ) {
                    ParticipantMenuItem(
                        text = "프로필 보기",
                        iconRes = R.drawable.ic_person,
                        onClick = { menuExpanded = false },
                    )
                    if (!isMe && member.role != CommunityThreadRole.OWNER) {
                        ParticipantMenuItem(
                            text = "내보내기",
                            iconRes = R.drawable.ic_block,
                            color = red400(),
                            onClick = {
                                menuExpanded = false
                                onKick()
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ParticipantMenuItem(
    text: String,
    @androidx.annotation.DrawableRes iconRes: Int,
    color: Color = grey950(),
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = text, color = color, fontSize = 14.sp)
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(21.dp),
        )
    }
}

@Composable
private fun ParticipantBadge(
    text: String,
    background: Color,
    foreground: Color,
) {
    Text(
        text = text,
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(background)
            .padding(horizontal = 6.dp, vertical = 3.dp),
        color = foreground,
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold,
    )
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
                            contentDescription = "뒤로가기",
                            tint = grey950(),
                        )
                    }
                    Text(
                        text = "참여 종료",
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
                text = "더 이상 참여할 수 없는 스레드예요",
                color = grey800(),
                fontSize = 15.sp,
                lineHeight = 21.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "운영진에 의해 참여가 종료되었어요.",
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
                    text = "커뮤니티로 돌아가기",
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
            text = "메시지를 불러오지 못했어요",
            color = grey800(),
            fontSize = 22.sp,
            lineHeight = 28.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = "네트워크 연결을 확인하고 다시 시도해주세요.",
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
                text = "다시 시도하기",
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
                Icon(painterResource(R.drawable.ic_back), contentDescription = "뒤로가기", tint = grey950())
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
                    Icon(painterResource(R.drawable.ic_menu_kebab), contentDescription = "더보기", tint = grey950())
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
                    ThreadMenuSectionTitle("요약 및 설정")
                    ThreadMenuItem("대화 요약") {
                        menuExpanded = false
                        onSummary()
                    }
                    ThreadMenuItem(if (isMuted) "알림 켜기" else "알림 끄기") {
                        menuExpanded = false
                        onToggleMuted()
                    }
                    ThreadMenuItem(if (isPinned) "고정 해제" else "고정") {
                        menuExpanded = false
                        onTogglePinned()
                    }
                    ThreadMenuDivider()
                    ThreadMenuSectionTitle("참여자")
                    ThreadMenuItem("참여자 보기") {
                        menuExpanded = false
                        onParticipants()
                    }
                    if (isOwner) {
                        ThreadMenuItem("참여자 초대") {
                            menuExpanded = false
                            onInviteParticipants()
                        }
                    }
                    ThreadMenuItem("링크 공유") {
                        menuExpanded = false
                        onShareLink()
                    }
                    ThreadMenuDivider()
                    if (isOwner) {
                        ThreadMenuSectionTitle("스레드")
                        ThreadMenuItem("스레드 편집") {
                            menuExpanded = false
                            onEditThread()
                        }
                        ThreadMenuItem("스레드 삭제", color = red400()) {
                            menuExpanded = false
                            onDeleteThread()
                        }
                    }
                    ThreadMenuItem("나가기", color = red400()) {
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
) {
    Row(
        modifier = Modifier
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
                "읽지 않은 메시지 ${unreadCount}개",
                color = grey950(),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                "Galaxy AI로 핵심만 요약해 드려요",
                color = grey800(),
                fontSize = 12.sp,
            )
        }
        Icon(painterResource(R.drawable.ic_next_small), contentDescription = null, tint = grey400())
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
                MineMessage(message, onLongClick)
            } else {
                OtherMessage(message, member, onLongClick)
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
                    message.senderName.orEmpty().ifBlank { "알 수 없음" },
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
                    color = if (message.type == CommunityMessageType.IMAGE) Color.Transparent else white(),
                    shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 5.dp),
                ) {
                    MessageBubbleContent(
                        message = message,
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
    isMine: Boolean,
    modifier: Modifier = Modifier,
) {
    val foreground = if (isMine) white() else grey950()
    val secondary = if (isMine) indigo100() else grey400()
    val divider = if (isMine) white().copy(alpha = 0.22f) else grey200()
    val reply = message.replyTo

    if (message.type == CommunityMessageType.IMAGE) {
        if (!message.content.isNullOrBlank()) {
            AsyncImage(
                model = message.content,
                contentDescription = "채팅 이미지",
                modifier = modifier
                    .size(width = 220.dp, height = 180.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop,
            )
        } else {
            Box(
                modifier = modifier.size(width = 220.dp, height = 160.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = painterResource(R.drawable.ic_image),
                        contentDescription = null,
                        tint = secondary,
                        modifier = Modifier.size(32.dp),
                    )
                    Text(
                        text = "이미지",
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
            text = "${reply.senderName.ifBlank { "알 수 없음" }}님에게 답장",
            color = foreground,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = reply.snippet.ifBlank { "메시지 내용" },
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
                    MessageActionItem("답장", R.drawable.ic_forward_circle, onClick = onReply)
                    MessageActionItem("복사", R.drawable.ic_document, onClick = onCopy)
                    MessageActionItem("신고", R.drawable.ic_warning, onClick = onReport)
                    MessageActionItem(
                        text = "삭제",
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
                        Icon(painterResource(R.drawable.ic_add), contentDescription = "다른 이모지", modifier = Modifier.size(17.dp))
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
    CommunityMessageReportReason.SPAM to "스팸 · 광고",
    CommunityMessageReportReason.ABUSE to "욕설 · 비방 · 혐오 표현",
    CommunityMessageReportReason.INAPPROPRIATE to "부적절하거나 불쾌한 내용",
    CommunityMessageReportReason.PRIVACY to "개인정보 노출",
    CommunityMessageReportReason.ETC to "기타",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MessageReportSheet(
    onDismiss: () -> Unit,
    onReport: (CommunityMessageReportReason) -> Unit,
) {
    var selectedReason by remember { mutableStateOf<CommunityMessageReportReason?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = white(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
        ) {
            Text(
                text = "신고 사유",
                color = grey950(),
                fontSize = 20.sp,
                lineHeight = 26.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "신고 내용은 운영진만 확인하며, 익명으로 처리돼요.",
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
            Spacer(Modifier.height(36.dp))
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
                    Text("취소하기", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
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
                    Text("신고하기", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
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
            text = "이모지 선택",
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
    val isImage = message.type == CommunityMessageType.IMAGE && message.localUri != null

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.End,
    ) {
        Row(
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                if (failed) "전송 실패" else "전송 중",
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
                if (message.type == CommunityMessageType.IMAGE && message.localUri != null) {
                    AsyncImage(
                        model = message.localUri,
                        contentDescription = "전송할 이미지",
                        modifier = Modifier
                            .size(width = 220.dp, height = 180.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop,
                    )
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
                        contentDescription = "메시지 재전송",
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
                        contentDescription = "실패 메시지 삭제",
                        tint = red400(),
                        modifier = Modifier.size(28.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileImage(imageUrl: String?) {
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
                contentDescription = "사용자 프로필 이미지",
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
private fun partTag(part: String): Triple<String, Color, Color> = when (part) {
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
        modifier = modifier,
        color = grey400(),
        fontSize = 11.sp,
    )
}

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
    val date = communityCreatedAtDate(createdAt) ?: return "날짜 정보 없음"
    return when (date) {
        today -> "오늘"
        today.minusDays(1) -> "어제"
        else -> if (date.year == today.year) {
            "${date.monthValue}월 ${date.dayOfMonth}일"
        } else {
            "${date.year}년 ${date.monthValue}월 ${date.dayOfMonth}일"
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
                                text = "${replyingMessage.senderName.orEmpty().ifBlank { "나" }}님에게 답장",
                                color = grey950(),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = replyingMessage.content.orEmpty().ifBlank { "메시지 내용" },
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
                                        "답장 메시지를 입력해주세요",
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
                                contentDescription = "답장 전송",
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
                        Icon(painterResource(R.drawable.ic_image), contentDescription = "사진 첨부", tint = black())
                    }
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        if (value.isEmpty()) {
                            Text("메시지를 입력해주세요", color = grey400(), fontSize = 14.sp)
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
                            contentDescription = "전송",
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
        state = CommunityChattingUiState(
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
        onBack = {},
        onMore = {},
        onUnreadSummary = {},
        onCamera = {},
        onDraftChange = {},
        onSend = {},
        onLoadPrevious = {},
    )
}
