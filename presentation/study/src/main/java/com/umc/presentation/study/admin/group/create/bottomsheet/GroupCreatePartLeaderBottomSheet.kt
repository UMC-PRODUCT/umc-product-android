package com.umc.presentation.study.admin.group.create.bottomsheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.umc.component.component.UText
import com.umc.component.component.UTextField
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.grey000
import com.umc.component.theme.grey100
import com.umc.component.theme.grey500
import com.umc.component.theme.grey600
import com.umc.component.theme.grey900
import com.umc.component.theme.indigo500
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

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    fun dismissWithApply() {
        onConfirm(state.selectedMembers)
        viewModel.resetAfterDismiss()
        onDismissRequest()
    }

    LaunchedEffect(preSelected) {
        viewModel.resetAfterDismiss()
        viewModel.setSelected(preSelected)
    }

    ModalBottomSheet(
        onDismissRequest = ::dismissWithApply,
        sheetState = sheetState,
        containerColor = grey000(),
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = grey600()
            )
        },
        shape = RoundedCornerShape(
            topStart = 28.dp,
            topEnd = 28.dp
        ),
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
                prevIcon = painterResource(
                    id = R.drawable.ic_search
                ),
                prevIconTint = grey500(),
                prevIconSize = 24.dp,
                backgroundColor = grey100(),
                focusBackgroundColor = grey000(),
                strokeColor = grey100(),
                focusStrokeColor = grey900(),
                cornerRadius = 8.dp
            )

            Spacer(modifier = Modifier.height(28.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when {
                    state.isSearching -> {
                        GroupCreatePartLeaderSearchResults(
                            searchResults = state.searchResults,
                            selectedMembers = state.selectedMembers,
                            onSelectClick = { member ->
                                viewModel.setSelected(
                                    (state.selectedMembers + member)
                                        .distinctBy { it.id }
                                )
                                viewModel.clearSearchOnly()
                            }
                        )
                    }

                    state.selectedMembers.isEmpty() -> {
                        GroupCreateEmptyContent(
                            text = "아직 추가한 파트장이 없어요"
                        )
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(
                                items = state.selectedMembers,
                                key = { member -> member.id }
                            ) { item ->
                                GroupCreateAddedMemberRow(
                                    item = item,
                                    onRemoveClick = {
                                        viewModel.removeMember(item)
                                    }
                                )
                            }
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

@Composable
private fun GroupCreatePartLeaderSearchResults(
    searchResults: List<AdminStudyGroupCreateMemberUiModel>,
    selectedMembers: List<AdminStudyGroupCreateMemberUiModel>,
    onSelectClick: (AdminStudyGroupCreateMemberUiModel) -> Unit,
) {
    val groupedResults = searchResults
        .groupBy { member -> member.partLabel }
        .toList()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        groupedResults.forEach { (partLabel, members) ->
            if (partLabel.isNotBlank()) {
                item(
                    key = "part_header_$partLabel"
                ) {
                    UText(
                        text = partLabel,
                        style = UmcTypographyTokens.BodyBold,
                        color = grey900(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                top = 4.dp,
                                bottom = 8.dp
                            )
                    )
                }
            }

            items(
                items = members,
                key = { member -> member.id }
            ) { member ->
                val isAlreadySelected = selectedMembers.any {
                        selectedMember ->
                    selectedMember.id == member.id
                }

                GroupCreateMultiSearchRow(
                    item = member,
                    isChecked = isAlreadySelected,
                    enabled = !isAlreadySelected,
                    onToggleClick = {
                        onSelectClick(member)
                    }
                )
            }

            item(
                key = "part_spacing_$partLabel"
            ) {
                Spacer(
                    modifier = Modifier.height(16.dp)
                )
            }
        }
    }
}