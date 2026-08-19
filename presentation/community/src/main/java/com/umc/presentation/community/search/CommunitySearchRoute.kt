package com.umc.presentation.community.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * 커뮤니티 검색 화면의 Route
 *
 * ViewModel의 상태를 구독하고 화면 이동과 같은
 * 일회성 이벤트를 처리합니다.
 */
@Composable
fun CommunitySearchRoute(
    onNavigateBack: () -> Unit,
    onNavigateToThreadDetail: (String) -> Unit,
    viewModel: CommunitySearchViewModel =
        hiltViewModel<CommunitySearchViewModel>(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.event.collect { event ->
            when (event) {
                CommunitySearchEvent.NavigateBack -> {
                    onNavigateBack()
                }

                is CommunitySearchEvent.NavigateToThreadDetail -> {
                    onNavigateToThreadDetail(
                        event.threadId,
                    )
                }
            }
        }
    }

    CommunitySearchScreen(
        state = state,
        onAction = viewModel::onAction,
    )
}