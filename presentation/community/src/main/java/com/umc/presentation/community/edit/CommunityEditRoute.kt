package com.umc.presentation.community.edit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.umc.component.base.CollectUiEvents
import com.umc.component.component.LocalUToast
import com.umc.component.component.UToastState
import com.umc.component.theme.AppStrings
import com.umc.presentation.community.bottomsheet.edit.CommunityMemberBottomSheet

/**
 * 스레드 수정 화면의 Route
 *
 * 수정할 스레드 정보를 불러오고 ViewModel의 상태를 구독하며,
 * 화면 이동 및 토스트와 같은 일회성 이벤트를 처리합니다.
 * 저장·삭제 직후 화면을 떠나므로 토스트는 화면을 떠나도 남는 앱 공용 토스트로 띄운다.
 * 챌린저 관리 시트는 hiltViewModel()로 VM을 얻으므로 Screen이 아니라 Route에서 띄운다.
 */
@Composable
fun CommunityEditRoute(
    threadId: String,
    onNavigateBack: () -> Unit,
    onNavigateToEmojiPicker: () -> Unit,
    onEditSuccess: () -> Unit,
    onDeleteSuccess: () -> Unit,
    viewModel: CommunityEditViewModel =
        hiltViewModel<CommunityEditViewModel>(),
) {
    val toast = LocalUToast.current

    // 스레드 수정 화면의 UI 상태 구독
    val state by viewModel.state.collectAsStateWithLifecycle()

    // 수정 화면 진입 시 전달받은 threadId로 스레드 상세 조회. 회전 뒤 다시 실행되는 호출은 ViewModel이 거른다.
    LaunchedEffect(threadId) {
        viewModel.loadThread(
            threadId = threadId,
        )
    }

    // ViewModel에서 발생하는 일회성 이벤트 처리
    CollectUiEvents(viewModel.event) { event ->
        when (event) {
            // 이전 화면으로 이동
            CommunityEditEvent.NavigateBack -> {
                onNavigateBack()
            }

            // 스레드 수정 완료
            CommunityEditEvent.SaveSuccess -> {
                toast.show(AppStrings.COMMUNITY_EDIT_SUCCESS, UToastState.CHECK)

                onEditSuccess()
            }

            // 스레드 삭제 완료
            CommunityEditEvent.DeleteSuccess -> {
                toast.show(AppStrings.COMMUNITY_DELETE_SUCCESS, UToastState.CHECK)

                // 삭제된 스레드의 채팅방으로 돌아가지 않도록 목록까지 되돌아간다
                onDeleteSuccess()
            }

            // 아이콘 선택 화면으로 이동
            CommunityEditEvent.NavigateToEmojiPicker -> {
                onNavigateToEmojiPicker()
            }

            // 공통 에러 안내
            is CommunityEditEvent.ShowToast -> {
                toast.show(event.message, UToastState.ERROR)
            }
        }
    }

    // 실제 스레드 수정 화면
    CommunityEditScreen(
        state = state,
        onAction = viewModel::onAction,
    )

    // 챌린저 추가를 위한 멤버 관리 BottomSheet
    if (
        state.showChallengerBottomSheet &&
        state.threadId.isNotBlank()
    ) {
        CommunityMemberBottomSheet(
            threadId = state.threadId,
            onDismissRequest = {
                viewModel.onAction(
                    CommunityEditAction.OnDismissChallengerBottomSheet
                )
            },
            onInviteSuccess = {
                viewModel.onAction(
                    CommunityEditAction.OnMemberInviteSuccess
                )
            },
        )
    }
}