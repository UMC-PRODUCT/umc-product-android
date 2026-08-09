package com.umc.presentation.community.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

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