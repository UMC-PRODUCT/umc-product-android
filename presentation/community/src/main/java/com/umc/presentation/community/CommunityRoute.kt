package com.umc.presentation.community

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect

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

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.startThreadPolling()
    }

    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) {
        viewModel.stopThreadPolling()
    }

    DisposableEffect(viewModel) {
        onDispose(viewModel::stopThreadPolling)
    }

    LaunchedEffect(viewModel) {
        viewModel.event.collect { event ->
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
    }

    CommunityScreen(
        state = state,
        onAction = viewModel::onAction,
    )
}
