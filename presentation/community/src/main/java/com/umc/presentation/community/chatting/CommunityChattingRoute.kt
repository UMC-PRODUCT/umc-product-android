package com.umc.presentation.community.chatting

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.umc.component.component.UToastData
import com.umc.component.component.UToastState
import com.umc.presentation.community.bottomsheet.edit.CommunityMemberBottomSheet
import com.umc.component.theme.AppStrings
import com.umc.component.base.CollectUiEvents

@Composable
fun CommunityChattingRoute(
    onBack: () -> Unit,
    onThreadDeleted: () -> Unit = onBack,
    onMore: () -> Unit = {},
    onUnreadSummary: () -> Unit = {},
    onCamera: () -> Unit = {},
    onEditThread: () -> Unit = {},
    onViewParticipantProfile: (String) -> Unit = {},
    shouldRefresh: Boolean = false,
    onRefreshHandled: () -> Unit = {},
    viewModel: CommunityChattingViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var toastData by remember { mutableStateOf<UToastData?>(null) }
    var showInviteMemberSheet by remember { mutableStateOf(false) }

    LaunchedEffect(shouldRefresh) {
        if (shouldRefresh) {
            viewModel.refreshThread()
            onRefreshHandled()
        }
    }

    CollectUiEvents(viewModel.event) { event ->
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
            }
            CommunityChattingEvent.OpenEditThread -> onEditThread()
            CommunityChattingEvent.MessageReported -> toastData = UToastData(
                message = AppStrings.CHAT_REPORT_SUCCESS,
                state = UToastState.CHECK,
            )
            is CommunityChattingEvent.ShowError -> toastData = UToastData(
                message = event.message,
                state = UToastState.ERROR,
            )
        }
    }

    CommunityChattingScreen(
        state = state,
        toastData = toastData,
        onToastDismiss = { toastData = null },
        onViewParticipantProfile = onViewParticipantProfile,
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
