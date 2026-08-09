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

    LaunchedEffect(Unit) {
        viewModel.setGisuId(5L)
    }


    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                AdminStudyGroupCreateEvent.NavigateBack -> {
                    navigateBack()
                }

                AdminStudyGroupCreateEvent.RegisterSuccess -> {
                    navigateBack()
                }

                is AdminStudyGroupCreateEvent.RegisterFailure -> {
                    // 추후 실패 메시지 표시
                }
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