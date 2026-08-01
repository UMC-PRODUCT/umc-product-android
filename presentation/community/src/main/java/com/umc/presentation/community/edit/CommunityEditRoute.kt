package com.umc.presentation.community.edit

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun CommunityEditRoute(
    threadId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToEmojiPicker: () -> Unit,
    viewModel: CommunityEditViewModel = viewModel(),
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(threadId) {
        viewModel.loadThread(threadId)
    }

    LaunchedEffect(viewModel) {
        viewModel.event.collect { event ->
            when (event) {
                CommunityEditEvent.NavigateBack -> {
                    onNavigateBack()
                }

                CommunityEditEvent.SaveSuccess -> {
                    Toast.makeText(
                        context,
                        "스레드가 수정되었습니다.",
                        Toast.LENGTH_SHORT,
                    ).show()

                    onNavigateBack()
                }

                CommunityEditEvent.DeleteSuccess -> {
                    Toast.makeText(
                        context,
                        "스레드가 삭제되었습니다.",
                        Toast.LENGTH_SHORT,
                    ).show()

                    onNavigateBack()
                }

                CommunityEditEvent.NavigateToEmojiPicker -> {
                    onNavigateToEmojiPicker()
                }

                is CommunityEditEvent.ShowToast -> {
                    Toast.makeText(
                        context,
                        event.message,
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
        }
    }

    CommunityEditScreen(
        state = state,
        onAction = viewModel::onAction,
    )
}