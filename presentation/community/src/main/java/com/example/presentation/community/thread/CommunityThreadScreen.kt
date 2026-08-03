package com.example.presentation.community.thread

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.umc.component.R
import com.umc.component.theme.*
import com.umc.domain.model.community.thread.CommunityThreadCategory
import com.umc.domain.model.community.thread.CommunityThreadSummary
import com.umc.domain.model.community.thread.CreateCommunityThread

@Composable
fun CommunityThreadRoute(
    onOpenChat: (String) -> Unit,
    viewModel: CommunityThreadViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is CommunityThreadEvent.OpenChat -> onOpenChat(event.threadId)
                is CommunityThreadEvent.ShowError ->
                    snackbarHostState.showSnackbar(event.message.ifBlank { "Thread 생성에 실패했습니다." })
            }
        }
    }

    CommunityThreadScreen(
        state = state,
        onSearch = viewModel::search,
        onRefresh = viewModel::refresh,
        onLoadNext = viewModel::loadNext,
        onOpenChat = viewModel::openChat,
        onTogglePin = viewModel::togglePin,
        onCreate = viewModel::createThread,
        snackbarHostState = snackbarHostState,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityThreadScreen(
    state: CommunityThreadUiState,
    onSearch: (String) -> Unit,
    onRefresh: () -> Unit,
    onLoadNext: () -> Unit,
    onOpenChat: (String) -> Unit,
    onTogglePin: (CommunityThreadSummary) -> Unit,
    onCreate: (CreateCommunityThread) -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    var showCreateDialog by rememberSaveable { mutableStateOf(false) }

    if (showCreateDialog) {
        CreateThreadDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = {
                showCreateDialog = false
                onCreate(it)
            },
        )
    }

    Scaffold(
        containerColor = grey100(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text("Community", fontWeight = FontWeight.Bold)
                },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(painterResource(R.drawable.ic_refresh), contentDescription = "새로고침")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = white()),
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = indigo500(),
                contentColor = white(),
            ) {
                Icon(painterResource(R.drawable.ic_add), contentDescription = "Thread 만들기")
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                OutlinedTextField(
                    value = state.query,
                    onValueChange = onSearch,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Thread 검색") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = indigo500(),
                        unfocusedContainerColor = white(),
                        focusedContainerColor = white(),
                    ),
                )
            }

            if (state.pinned.isNotEmpty()) {
                item {
                    Text(
                        "고정된 Thread",
                        modifier = Modifier.padding(top = 8.dp),
                        color = grey600(),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                items(state.pinned, key = { "pinned-${it.threadId}" }) { thread ->
                    CommunityThreadCard(thread, onOpenChat, onTogglePin)
                }
            }

            item {
                Text(
                    "전체 Thread",
                    modifier = Modifier.padding(top = 8.dp),
                    color = grey600(),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            if (state.isRefreshing && state.threads.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = indigo500())
                    }
                }
            } else if (state.threads.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 56.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_comment),
                            contentDescription = null,
                            tint = grey400(),
                            modifier = Modifier.size(40.dp),
                        )
                        Text(
                            state.errorMessage ?: "참여 중인 Thread가 없습니다.",
                            color = grey500(),
                        )
                    }
                }
            } else {
                items(state.threads, key = { it.threadId }) { thread ->
                    CommunityThreadCard(thread, onOpenChat, onTogglePin)
                }
            }

            if (state.nextOffset != null) {
                item {
                    TextButton(
                        onClick = onLoadNext,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(if (state.isLoadingMore) "불러오는 중..." else "더 보기")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateThreadDialog(
    onDismiss: () -> Unit,
    onCreate: (CreateCommunityThread) -> Unit,
) {
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var icon by rememberSaveable { mutableStateOf("💬") }
    var category by rememberSaveable { mutableStateOf(CommunityThreadCategory.FREE) }
    val canCreate = title.isNotBlank() && icon.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("새 Thread 만들기", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("제목") },
                    placeholder = { Text("예: Android 스터디") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("설명 (선택)") },
                    minLines = 2,
                    maxLines = 3,
                )
                OutlinedTextField(
                    value = icon,
                    onValueChange = { value ->
                        if (value.codePointCount(0, value.length) <= 4) icon = value
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("아이콘") },
                    supportingText = { Text("이모지 한 개를 입력해주세요.") },
                    singleLine = true,
                )
                Text(
                    "카테고리",
                    color = grey600(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    CommunityThreadCategory.entries.forEach { item ->
                        FilterChip(
                            selected = category == item,
                            onClick = { category = item },
                            label = {
                                Text(
                                    when (item) {
                                        CommunityThreadCategory.STUDY -> "스터디"
                                        CommunityThreadCategory.QNA -> "Q&A"
                                        CommunityThreadCategory.PROJECT -> "프로젝트"
                                        CommunityThreadCategory.FREE -> "자유"
                                    }
                                )
                            },
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onCreate(
                        CreateCommunityThread(
                            title = title.trim(),
                            description = description.trim().takeIf(String::isNotEmpty),
                            category = category,
                            icon = icon.trim(),
                        )
                    )
                },
                enabled = canCreate,
                colors = ButtonDefaults.buttonColors(containerColor = indigo500()),
            ) {
                Text("만들기")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        },
    )
}

@Composable
private fun CommunityThreadCard(
    thread: CommunityThreadSummary,
    onOpenChat: (String) -> Unit,
    onTogglePin: (CommunityThreadSummary) -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenChat(thread.threadId) },
        color = white(),
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(indigo100(), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(thread.icon.ifBlank { "💬" }, fontSize = 22.sp)
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        thread.title,
                        modifier = Modifier.weight(1f),
                        color = grey900(),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (thread.unreadCount != "0") {
                        Badge(containerColor = indigo500()) {
                            Text(thread.unreadCount, color = white())
                        }
                    }
                }
                Text(
                    thread.lastMessage?.preview ?: thread.description.orEmpty(),
                    color = grey500(),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    "${thread.category.name} · ${thread.memberCount}명",
                    color = grey400(),
                    fontSize = 11.sp,
                )
            }
            IconButton(onClick = { onTogglePin(thread) }) {
                Icon(
                    painter = painterResource(R.drawable.ic_push_pin),
                    contentDescription = if (thread.isPinned) "고정 해제" else "고정",
                    tint = if (thread.isPinned) indigo500() else grey300(),
                )
            }
        }
    }
}
