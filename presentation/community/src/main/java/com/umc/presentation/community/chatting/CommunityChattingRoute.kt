package com.umc.presentation.community.chatting

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.umc.presentation.community.bottomsheet.CommunityMemberBottomSheet

@Composable
fun CommunityChattingRoute(
    onBack: () -> Unit,
    onThreadDeleted: () -> Unit = onBack,
    onMore: () -> Unit = {},
    onUnreadSummary: () -> Unit = {},
    onCamera: () -> Unit = {},
    onInviteParticipants: () -> Unit = {},
    onEditThread: () -> Unit = {},
    shouldRefresh: Boolean = false,
    onRefreshHandled: () -> Unit = {},
    viewModel: CommunityChattingViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showInviteMemberSheet by remember { mutableStateOf(false) }

    LaunchedEffect(shouldRefresh) {
        if (shouldRefresh) {
            viewModel.refreshThread()
            onRefreshHandled()
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.event.collect { event ->
            when (event) {
                CommunityChattingEvent.NavigateBack,
                CommunityChattingEvent.ThreadUnavailable,
                    -> onBack()
                CommunityChattingEvent.ThreadDeleted -> onThreadDeleted()
                CommunityChattingEvent.OpenMore -> onMore()
                CommunityChattingEvent.OpenUnreadSummary -> onUnreadSummary()
                CommunityChattingEvent.OpenCamera -> onCamera()
                CommunityChattingEvent.OpenInviteParticipants -> {
                    showInviteMemberSheet = true
                    onInviteParticipants()
                }
                CommunityChattingEvent.OpenEditThread -> onEditThread()
                CommunityChattingEvent.MessageReported -> snackbarHostState.showSnackbar(
                    message = "신고가 정상적으로 접수되었습니다.",
                    duration = SnackbarDuration.Short,
                )
                is CommunityChattingEvent.ShowError -> snackbarHostState.showSnackbar(
                    message = event.message,
                    duration = SnackbarDuration.Short,
                )
            }
        }
    }

    CommunityChattingScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onAction = viewModel::onAction,
    )

    if (showInviteMemberSheet) {
        CommunityMemberBottomSheet(
            threadId = state.threadId,
            onDismissRequest = { showInviteMemberSheet = false },
            onInviteSuccess = {
                showInviteMemberSheet = false
                viewModel.refreshThread()
            },
        )
    }
}
