package com.umc.presentation.community

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.umc.component.base.CollectUiEvents

/**
 * 커뮤니티 메인 화면의 Route
 *
 * ViewModel 상태를 구독하고 화면 이동 및 Toast 이벤트를 처리하며,
 * 화면 Lifecycle에 맞춰 스레드 목록 Polling을 시작/중지합니다.
 */
@Composable
fun CommunityRoute(
    onNavigateToThreadDetail: (String) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToCreateThread: () -> Unit,
    onNavigateToEditThread: (String) -> Unit,
    viewModel: CommunityViewModel = hiltViewModel(),
) {
    val context = LocalContext.current

    val state by viewModel.state.collectAsStateWithLifecycle()

    // 화면 Lifecycle에 맞춰 스레드 목록 Polling 관리
    ThreadPollingLifecycleEffect(
        pollingKey = viewModel,
        onStartPolling = viewModel::startThreadPolling,
        onStopPolling = viewModel::stopThreadPolling,
    )

    // ViewModel에서 발생하는 일회성 이벤트 처리
    CollectUiEvents(viewModel.event) { event ->
        when (event) {
            is CommunityEvent.NavigateToThreadDetail -> {
                onNavigateToThreadDetail(event.threadId)
            }

            CommunityEvent.NavigateToSearch -> {
                onNavigateToSearch()
            }

            is CommunityEvent.NavigateToEditThread -> {
                onNavigateToEditThread(event.threadId)
            }

            CommunityEvent.NavigateToCreateThread -> {
                onNavigateToCreateThread()
            }

            is CommunityEvent.ShowToast -> {
                Toast.makeText(
                    context,
                    event.message,
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }
    }

    CommunityScreen(
        state = state,
        onAction = viewModel::onAction,
    )
}

/**
 * 커뮤니티 화면의 Lifecycle에 따라
 * 스레드 목록 Polling을 시작하거나 중지합니다.
 */
@Composable
internal fun ThreadPollingLifecycleEffect(
    pollingKey: Any,
    onStartPolling: () -> Unit,
    onStopPolling: () -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    // 이미 RESUMED 상태로 진입한 경우 Polling 시작
    LaunchedEffect(pollingKey, lifecycleOwner) {
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            onStartPolling()
        }
    }

    // 화면이 Pause 상태가 되면 Polling 중지
    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) {
        onStopPolling()
    }

    // 화면이 다시 Resume되면 Polling 재시작
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        onStartPolling()
    }

    // Composable이 제거될 때 Polling 정리
    DisposableEffect(pollingKey) {
        onDispose(onStopPolling)
    }
}