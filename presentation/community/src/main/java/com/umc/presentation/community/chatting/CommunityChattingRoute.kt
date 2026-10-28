package com.umc.presentation.community.chatting

import android.widget.Toast
import android.net.ConnectivityManager
import android.net.Network
import androidx.core.content.getSystemService
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.umc.component.fcm.ActiveCommunityThread
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
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
    shouldRefresh: Boolean = false,
    onRefreshHandled: () -> Unit = {},
    viewModel: CommunityChattingViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    var toastData by remember { mutableStateOf<UToastData?>(null) }
    var showInviteMemberSheet by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, state.threadId) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                ActiveCommunityThread.threadId = state.threadId
                viewModel.onForegroundChanged(true)
            } else if (event == Lifecycle.Event.ON_PAUSE) {
                ActiveCommunityThread.threadId = null
                viewModel.onForegroundChanged(false)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            ActiveCommunityThread.threadId = state.threadId
            viewModel.onForegroundChanged(true)
        }
        val connectivity = context.getSystemService<ConnectivityManager>()
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) { viewModel.onNetworkAvailable() }
        }
        connectivity?.registerDefaultNetworkCallback(callback)
        onDispose {
            connectivity?.unregisterNetworkCallback(callback)
            lifecycleOwner.lifecycle.removeObserver(observer)
            ActiveCommunityThread.threadId = null
            viewModel.onForegroundChanged(false)
        }
    }

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
            CommunityChattingEvent.ThreadDeleted -> {
                // 채팅 화면의 토스트는 화면과 함께 사라지므로 시스템 토스트로 알린다(목록에서 나갈 때와 같은 방식).
                Toast.makeText(context, AppStrings.CHAT_LEAVE_SUCCESS, Toast.LENGTH_SHORT).show()
                onThreadDeleted()
            }
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
        onAction = viewModel::onAction,
    )

    // 접근 권한이 없어진 뒤에는 열려 있던 초대 시트를 남기지 않는다.
    if (showInviteMemberSheet && !state.isAccessDenied) {
        CommunityMemberBottomSheet(
            threadId = state.threadId,
            onDismissRequest = { showInviteMemberSheet = false },
            onInviteSuccess = {
                viewModel.refreshThread()
            },
        )
    }
}
