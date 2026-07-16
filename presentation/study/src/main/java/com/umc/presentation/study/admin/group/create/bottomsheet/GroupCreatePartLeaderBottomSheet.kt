package com.umc.presentation.study.admin.group.create.bottomsheet

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.umc.component.R
import com.umc.component.component.UTextField
import com.umc.component.theme.*
import com.umc.presentation.study.admin.group.create.AdminStudyGroupCreateMemberUiModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupCreatePartLeaderBottomSheet(
    viewModel: GroupCreateMemberPickerViewModel = hiltViewModel(),
    preSelected: List<AdminStudyGroupCreateMemberUiModel>,
    onDismissRequest: () -> Unit,
    onConfirm: (List<AdminStudyGroupCreateMemberUiModel>) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    fun dismissWithApply() {
        onConfirm(state.selectedMembers)
        viewModel.resetAfterDismiss()
        onDismissRequest()
    }

    LaunchedEffect(Unit) {
        viewModel.setSelected(preSelected)
        viewModel.resetAfterDismiss()
    }

    ModalBottomSheet(
        onDismissRequest = { dismissWithApply() },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = grey000(),
        dragHandle = { BottomSheetDefaults.DragHandle(color = grey600()) },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(620.dp)
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
        ) {
            GroupCreatePickerTitle(
                title = if (state.isSearching) {
                    "담당 파트장을 검색하세요"
                } else {
                    "담당 파트장을 추가하세요"
                }
            )

            Spacer(modifier = Modifier.height(18.dp))

            UTextField(
                value = state.query,
                onValueChange = viewModel::searchMembers,
                placeholder = "이름을 입력하세요",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                prevIcon = painterResource(R.drawable.ic_search),
                prevIconTint = grey800(),
                prevIconSize = 18.dp
            )

            Spacer(modifier = Modifier.height(28.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (!state.isSearching) {
                    if (state.selectedMembers.isEmpty()) {
                        GroupCreateEmptyContent(text = "아직 추가한 파트장이 없어요")
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(state.selectedMembers, key = { it.id }) { item ->
                                GroupCreateAddedMemberRow(
                                    item = item,
                                    onRemoveClick = { viewModel.toggleMember(item) }
                                )
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            state.searchResults,
                            key = { it.id }
                        ) { item ->
                            GroupCreateSelectSearchRow(
                                item = item,
                                onSelectClick = {
                                    viewModel.addMember(item)
                                    viewModel.clearSearchOnly()
                                }
                            )
                        }
                    }
                }

                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = indigo500()
                    )
                }
            }
        }
    }
}