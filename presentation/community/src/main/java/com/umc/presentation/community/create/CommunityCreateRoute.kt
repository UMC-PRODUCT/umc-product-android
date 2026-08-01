package com.umc.presentation.community.create

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun CommunityCreateRoute(
    onNavigateBack: () -> Unit,
    onNavigateToEmojiPicker: () -> Unit,
    viewModel: CommunityCreateViewModel = viewModel(),
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.event.collect { event ->
            when (event) {
                CommunityCreateEvent.NavigateBack -> {
                    onNavigateBack()
                }

                CommunityCreateEvent.CreateSuccess -> {
                    Toast.makeText(
                        context,
                        "스레드가 생성되었습니다.",
                        Toast.LENGTH_SHORT,
                    ).show()

                    onNavigateBack()
                }

                CommunityCreateEvent.NavigateToEmojiPicker -> {
                    onNavigateToEmojiPicker()
                }

                is CommunityCreateEvent.ShowToast -> {
                    Toast.makeText(
                        context,
                        event.message,
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
        }
    }

    CommunityCreateScreen(
        state = state,
        onAction = viewModel::onAction,
    )
}