package com.umc.presentation.community.edit

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * 스레드 수정 화면의 Route
 *
 * 수정할 스레드 정보를 불러오고 ViewModel의 상태를 구독하며,
 * 화면 이동 및 Toast와 같은 일회성 이벤트를 처리합니다.
 */
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

    // 스레드 수정 화면의 UI 상태 구독
    val state by viewModel.state.collectAsStateWithLifecycle()

    // 수정 화면 진입 시 전달받은 threadId로 스레드 상세 조회
    LaunchedEffect(threadId) {
        viewModel.loadThread(
            threadId = threadId,
        )
    }

    // ViewModel에서 발생하는 일회성 이벤트 처리
    LaunchedEffect(viewModel) {
        viewModel.event.collect { event: CommunityEditEvent ->
            when (event) {
                // 이전 화면으로 이동
                CommunityEditEvent.NavigateBack -> {
                    onNavigateBack()
                }

                // 스레드 수정 완료
                CommunityEditEvent.SaveSuccess -> {
                    Toast.makeText(
                        context,
                        "스레드가 수정되었습니다.",
                        Toast.LENGTH_SHORT,
                    ).show()

                    onEditSuccess()
                }

                // 스레드 삭제 완료
                CommunityEditEvent.DeleteSuccess -> {
                    Toast.makeText(
                        context,
                        "스레드가 삭제되었습니다.",
                        Toast.LENGTH_SHORT,
                    ).show()

                    onEditSuccess()
                }

                // 아이콘 선택 화면으로 이동
                CommunityEditEvent.NavigateToEmojiPicker -> {
                    onNavigateToEmojiPicker()
                }

                // 챌린저 추가/삭제 완료
                CommunityEditEvent.MemberInviteSuccess -> {
                    Toast.makeText(
                        context,
                        "챌린저를 추가했습니다.",
                        Toast.LENGTH_SHORT,
                    ).show()
                }

                // 공통 안내 Toast 처리
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

    // 실제 스레드 수정 화면
    CommunityEditScreen(
        state = state,
        onAction = viewModel::onAction,
    )
}