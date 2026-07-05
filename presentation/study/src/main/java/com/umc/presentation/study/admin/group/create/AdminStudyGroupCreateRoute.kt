package com.umc.presentation.study.admin.group.create

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AdminStudyGroupCreateRoute(
    viewModel: AdminStudyGroupCreateViewModel = hiltViewModel(),
    navigateBack: () -> Unit,
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value

    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                AdminStudyGroupCreateEvent.NavigateBack -> navigateBack()
                AdminStudyGroupCreateEvent.RegisterSuccess -> navigateBack()
            }
        }
    }

    AdminStudyGroupCreateScreen(
        state = state,
        onAction = viewModel::onAction,
        onDismissBottomSheet = viewModel::dismissBottomSheet,
        onPartSelected = viewModel::selectPart,
        onPartLeaderSelected = viewModel::selectPartLeaders,
        onMembersSelected = viewModel::selectMembers,
    )
}