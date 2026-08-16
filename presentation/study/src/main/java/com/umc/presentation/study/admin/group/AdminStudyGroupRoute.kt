package com.umc.presentation.study.admin.group

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AdminStudyGroupRoute(
    isActive: Boolean,
    viewModel: AdminStudyGroupViewModel = hiltViewModel(),
    onNavigateCreateGroup: () -> Unit = {},
    onNavigateAddSchedule: (
        groupId: Long,
        groupTitle: String,
        groupPart: String,
    ) -> Unit = { _, _, _ -> },
    onOpenEditMembers: (AdminStudyGroupItemUiModel) -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(isActive) {
        if (isActive) {
            viewModel.refreshGroups()
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is AdminStudyGroupEvent.NavigateCreateGroup -> {
                    onNavigateCreateGroup()
                }

                is AdminStudyGroupEvent.NavigateAddSchedule -> {
                    onNavigateAddSchedule(
                        event.groupId,
                        event.groupTitle,
                        event.groupPart,
                    )
                }

                is AdminStudyGroupEvent.OpenEditMembers -> {
                    onOpenEditMembers(event.item)
                }

                is AdminStudyGroupEvent.ShowToast -> {
                    Toast.makeText(
                        context,
                        event.message,
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
        }
    }

    AdminStudyGroupScreen(
        state = state,
        onAction = viewModel::onAction,
    )
}