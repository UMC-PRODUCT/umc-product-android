package com.umc.presentation.community.create

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.umc.component.base.CollectUiEvents
import com.umc.component.component.LocalUToast
import com.umc.component.component.UToastData
import com.umc.component.component.UToastState
import com.umc.component.theme.AppStrings
import com.umc.presentation.community.bottomsheet.create.CommunityCreateMemberBottomSheet

/**
 * 스레드 생성 화면의 Route
 *
 * ViewModel의 상태를 수집하고,
 * 화면 이동 및 토스트와 같은 일회성 이벤트를 처리합니다.
 * 챌린저 선택 시트는 hiltViewModel()로 VM을 얻으므로 Screen이 아니라 Route에서 띄운다.
 */
@Composable
fun CommunityCreateRoute(
    onNavigateBack: () -> Unit,
    onNavigateToEmojiPicker: () -> Unit,
    onCreateSuccess: (String) -> Unit,
    viewModel: CommunityCreateViewModel =
        hiltViewModel<CommunityCreateViewModel>(),
) {
    // 생성 직후 화면을 떠나도 남아야 하는 토스트용 앱 공용 토스트
    val appToast = LocalUToast.current

    // 스레드 생성 화면의 UI 상태 구독
    val state by viewModel.state.collectAsStateWithLifecycle()

    // 화면 하단에 띄울 UToast (null 이면 숨김)
    var toastData by remember { mutableStateOf<UToastData?>(null) }

    // ViewModel에서 발생한 일회성 이벤트 처리
    CollectUiEvents(viewModel.event) { event ->
        when (event) {
            CommunityCreateEvent.NavigateBack -> {
                onNavigateBack()
            }

            // 생성 성공 뒤 바로 화면을 떠나므로 화면의 호스트 대신 앱 공용 토스트로 알린다
            is CommunityCreateEvent.CreateSuccess -> {
                appToast.show(AppStrings.COMMUNITY_CREATE_SUCCESS, UToastState.CHECK)

                onCreateSuccess(event.threadId)
            }

            // 스레드 생성 실패 시 에러 UToast 표시
            CommunityCreateEvent.CreateFailed -> {
                toastData = UToastData(
                    message = AppStrings.COMMUNITY_CREATE_FAILED,
                    state = UToastState.ERROR,
                )
            }

            // 아이콘 선택 화면으로 이동
            CommunityCreateEvent.NavigateToEmojiPicker -> {
                onNavigateToEmojiPicker()
            }

            // 필수값 누락 등 안내는 생성 실패와 같은 화면 호스트에 띄운다
            is CommunityCreateEvent.ShowToast -> {
                toastData = UToastData(
                    message = event.message,
                    state = UToastState.ERROR,
                )
            }
        }
    }

    // 실제 스레드 생성 UI
    CommunityCreateScreen(
        state = state,
        onAction = viewModel::onAction,
        toastData = toastData,
        onToastDismiss = { toastData = null },
    )

    // 챌린저 선택 BottomSheet
    if (state.showChallengerBottomSheet) {
        CommunityCreateMemberBottomSheet(
            preSelected = state.selectedChallengers,
            maxCount = state.maxChallengerCount,
            onDismissRequest = {
                viewModel.onAction(
                    CommunityCreateAction.OnDismissChallengerBottomSheet
                )
            },
            onConfirm = { challengers ->
                viewModel.onAction(
                    CommunityCreateAction.OnChallengersSelected(
                        challengers = challengers,
                    )
                )
            },
        )
    }
}