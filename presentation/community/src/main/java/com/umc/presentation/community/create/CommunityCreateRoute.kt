package com.umc.presentation.community.create

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * 스레드 생성 화면의 Route
 *
 * ViewModel의 상태를 수집하고,
 * 화면 이동 및 Toast와 같은 일회성 이벤트를 처리합니다.
 */
@Composable
fun CommunityCreateRoute(
    onNavigateBack: () -> Unit,
    onNavigateToEmojiPicker: () -> Unit,
    onCreateSuccess: (String) -> Unit,
    viewModel: CommunityCreateViewModel =
        hiltViewModel<CommunityCreateViewModel>(),
) {
    val context = LocalContext.current

    // 스레드 생성 화면의 UI 상태 구독
    val state by viewModel.state.collectAsStateWithLifecycle()

    // ViewModel에서 발생한 일회성 이벤트 처리
    LaunchedEffect(viewModel) {
        viewModel.event.collect { event: CommunityCreateEvent ->
            when (event) {
                CommunityCreateEvent.NavigateBack -> {
                    onNavigateBack()
                }

                // 스레드 생성 성공 후 Toast 표시 및 생성 완료 처리
                is CommunityCreateEvent.CreateSuccess -> {
                    Toast.makeText(
                        context,
                        "스레드가 생성되었습니다.",
                        Toast.LENGTH_SHORT,
                    ).show()

                    onCreateSuccess(event.threadId)
                }

                // 아이콘 선택 화면으로 이동
                CommunityCreateEvent.NavigateToEmojiPicker -> {
                    onNavigateToEmojiPicker()
                }

                // 공통 안내 Toast 처리
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

    // 실제 스레드 생성 UI
    CommunityCreateScreen(
        state = state,
        onAction = viewModel::onAction,
    )
}