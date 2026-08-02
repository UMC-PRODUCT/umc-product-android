package com.umc.presentation.community.edit

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun CommunityEditRoute(
    threadId: String,
    onNavigateBack: () -> Unit,
    onNavigateToEmojiPicker: () -> Unit,
    onEditSuccess: () -> Unit,
    viewModel: CommunityEditViewModel =
        hiltViewModel<CommunityEditViewModel>(),
) {
    val context = LocalContext.current

    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(threadId) {
        viewModel.loadThread(
            threadId = threadId,
        )
    }

    LaunchedEffect(viewModel) {
        viewModel.event.collect { event: CommunityEditEvent ->
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

                    onEditSuccess()
                }

                CommunityEditEvent.DeleteSuccess -> {
                    Toast.makeText(
                        context,
                        "스레드가 삭제되었습니다.",
                        Toast.LENGTH_SHORT,
                    ).show()

                    onEditSuccess()
                }

                CommunityEditEvent.NavigateToEmojiPicker -> {
                    onNavigateToEmojiPicker()
                }

                CommunityEditEvent.MemberInviteSuccess -> {
                    Toast.makeText(
                        context,
                        "챌린저를 추가했습니다.",
                        Toast.LENGTH_SHORT,
                    ).show()
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