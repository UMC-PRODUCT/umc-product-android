package com.umc.presentation.community.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.umc.component.base.CollectUiEvents

/**
 * 커뮤니티 검색 화면의 Route
 *
 * ViewModel의 상태를 구독하고 화면 이동과 같은
 * 일회성 이벤트를 처리합니다.
 */
@Composable
fun CommunitySearchRoute(
    onNavigateBack: () -> Unit,
    onNavigateToThreadDetail: (threadId: String, isKicked: Boolean) -> Unit,
    acknowledgedKickThreadId: String? = null,
    onKickAcknowledgedHandled: () -> Unit = {},
    viewModel: CommunitySearchViewModel =
        hiltViewModel<CommunitySearchViewModel>(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    CollectUiEvents(viewModel.event) { event ->
        when (event) {
            CommunitySearchEvent.NavigateBack -> {
                onNavigateBack()
            }

            is CommunitySearchEvent.NavigateToThreadDetail -> {
                onNavigateToThreadDetail(
                    event.threadId,
                    event.isKicked,
                )
            }
        }
    }

    // 참여 종료 화면에서 강퇴 확인을 마친 스레드는 검색 결과에서도 뺀다
    LaunchedEffect(acknowledgedKickThreadId) {
        val threadId = acknowledgedKickThreadId ?: return@LaunchedEffect
        viewModel.removeThread(threadId)
        onKickAcknowledgedHandled()
    }

    CommunitySearchScreen(
        state = state,
        onAction = viewModel::onAction,
    )
}