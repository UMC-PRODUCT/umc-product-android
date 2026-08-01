package com.umc.presentation.community

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun CommunityRoute(
    onNavigateToThreadDetail: (String) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToCreateThread: () -> Unit,
    viewModel: CommunityViewModel = hiltViewModel(),
    onNavigateToEditThread: (String) -> Unit,
) {
    val context = LocalContext.current

    val state by viewModel.state.collectAsStateWithLifecycle()

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