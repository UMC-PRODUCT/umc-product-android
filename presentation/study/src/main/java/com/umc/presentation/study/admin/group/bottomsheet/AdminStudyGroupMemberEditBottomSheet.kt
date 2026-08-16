package com.umc.presentation.study.admin.group.bottomsheet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.umc.component.R
import com.umc.component.component.UButton
import com.umc.component.component.UText
import com.umc.component.component.UTextField
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.grey000
import com.umc.component.theme.grey100
import com.umc.component.theme.grey400
import com.umc.component.theme.grey500
import com.umc.component.theme.grey600
import com.umc.component.theme.grey800
import com.umc.component.theme.grey900
import com.umc.component.theme.indigo500
import com.umc.component.theme.red500
import com.umc.presentation.study.admin.group.create.AdminStudyGroupCreateMemberUiModel
import com.umc.presentation.study.admin.group.create.bottomsheet.GroupCreateMemberPickerState
import com.umc.presentation.study.admin.group.create.bottomsheet.GroupCreateMemberPickerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminStudyGroupMemberEditBottomSheet(
    viewModel: AdminStudyGroupMemberEditViewModel = hiltViewModel(),
    preSelected: List<AdminStudyGroupCreateMemberUiModel>,
    onDismissRequest: () -> Unit,
    onConfirm: (List<AdminStudyGroupCreateMemberUiModel>) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(preSelected) {
        viewModel.initialize(preSelected)
    }

    fun dismissBottomSheet() {
        if (state.hasChanges) {
            onConfirm(state.selectedMembers)
        }

        viewModel.reset()
        onDismissRequest()
    }

    ModalBottomSheet(
        onDismissRequest = ::dismissBottomSheet,
        sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
        ),
        containerColor = grey000(),
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = grey600(),
            )
        },
        shape = RoundedCornerShape(
            topStart = 28.dp,
            topEnd = 28.dp,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(620.dp)
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
        ) {
            EditMemberHeader(
                isSearching = state.isSearching,
                isConfirmEnabled = state.isConfirmEnabled,
                onConfirmClick = {
                    viewModel.confirmPendingMembers()
                },
            )

            Spacer(
                modifier = Modifier.height(18.dp)
            )

            UTextField(
                value = state.query,
                onValueChange = viewModel::searchMembers,
                placeholder = "이름을 입력하세요",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                prevIcon = painterResource(R.drawable.ic_search),
                prevIconTint = grey500(),
                prevIconSize = 24.dp,
                backgroundColor = grey100(),
                focusBackgroundColor = grey000(),
                strokeColor = grey100(),
                focusStrokeColor = grey900(),
                cornerRadius = 8.dp,
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                when {
                    state.isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(
                                Alignment.Center
                            ),
                            color = indigo500(),
                        )
                    }

                    state.isSearching -> {
                        EditMemberSearchContent(
                            state = state,
                            onToggleMember = {
                                viewModel.togglePendingMember(it)
                            },
                            onLoadMore = {
                                viewModel.loadMoreMembers()
                            },
                        )
                    }

                    else -> {
                        EditCurrentMemberContent(
                            members = state.selectedMembers,
                            onRemoveMember = {
                                viewModel.removeMember(it)
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EditMemberHeader(
    isSearching: Boolean,
    isConfirmEnabled: Boolean,
    onConfirmClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        UText(
            text = if (isSearching) {
                "스터디원을 검색하세요"
            } else {
                "스터디원을 추가하세요"
            },
            style = UmcTypographyTokens.Title3Bold,
            color = grey800(),
            modifier = Modifier.weight(1f),
        )

        if (isSearching) {
            UButton(
                text = "확인",
                onClick = onConfirmClick,
                enabled = isConfirmEnabled,
                modifier = Modifier
                    .width(52.dp)
                    .height(32.dp),
                backgroundColor = if (isConfirmEnabled) {
                    indigo500()
                } else {
                    grey100()
                },
                textColor = if (isConfirmEnabled) {
                    grey000()
                } else {
                    grey400()
                },
                textStyle = UmcTypographyTokens.SubheadlineBold,
                cornerRadius = 8.dp,
            )
        }
    }
}

@Composable
private fun EditCurrentMemberContent(
    members: List<AdminStudyGroupCreateMemberUiModel>,
    onRemoveMember: (AdminStudyGroupCreateMemberUiModel) -> Unit,
) {
    if (members.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            UText(
                text = "추가된 스터디원이 없어요",
                style = UmcTypographyTokens.Body,
                color = grey500(),
            )
        }

        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
    ) {
        items(
            items = members,
            key = { member ->
                member.id
            },
        ) { member ->
            EditMemberRow(
                member = member,
                onDeleteClick = {
                    onRemoveMember(member)
                },
            )
        }
    }
}

@Composable
private fun EditMemberRow(
    member: AdminStudyGroupCreateMemberUiModel,
    onDeleteClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(
                R.drawable.ic_profile_default
            ),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(32.dp),
        )

        Spacer(
            modifier = Modifier.width(8.dp)
        )

        Column(
            modifier = Modifier.weight(1f),
        ) {
            UText(
                text = member.displayName,
                style = UmcTypographyTokens.SubheadlineBold,
                color = grey800(),
            )

            if (member.school.isNotBlank()) {
                Spacer(
                    modifier = Modifier.height(2.dp)
                )

                UText(
                    text = member.school,
                    style = UmcTypographyTokens.Footnote,
                    color = grey600(),
                )
            }
        }

        UButton(
            text = "삭제",
            onClick = onDeleteClick,
            modifier = Modifier
                .width(50.dp)
                .height(32.dp),
            backgroundColor = red500().copy(
                alpha = 0.12f
            ),
            textColor = red500(),
            textStyle = UmcTypographyTokens.SubheadlineBold,
            cornerRadius = 6.dp,
        )
    }
}

@Composable
private fun EditMemberSearchContent(
    state: AdminStudyGroupMemberEditState,
    onToggleMember: (
        AdminStudyGroupCreateMemberUiModel
    ) -> Unit,
    onLoadMore: () -> Unit,
) {
    val groupedResults = state.searchResults
        .groupBy { member ->
            member.partLabel
        }
        .toList()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
    ) {
        groupedResults.forEach { (partLabel, members) ->

            if (partLabel.isNotBlank()) {
                item(
                    key = "header_$partLabel"
                ) {
                    UText(
                        text = partLabel,
                        style = UmcTypographyTokens.BodyBold,
                        color = grey900(),
                        modifier = Modifier.padding(
                            top = 4.dp,
                            bottom = 8.dp,
                        ),
                    )
                }
            }

            items(
                items = members,
                key = { member ->
                    member.id
                },
            ) { member ->

                val isAlreadyMember =
                    state.selectedMembers.any {
                        it.id == member.id
                    }

                val isPending =
                    state.pendingMembers.any {
                        it.id == member.id
                    }

                EditSearchMemberRow(
                    member = member,
                    isChecked =
                        isAlreadyMember || isPending,
                    enabled = !isAlreadyMember,
                    onClick = {
                        onToggleMember(member)
                    },
                )
            }

            item(
                key = "spacing_$partLabel"
            ) {
                Spacer(
                    modifier = Modifier.height(16.dp)
                )
            }
        }

        if (
            state.hasNext &&
            state.searchResults.isNotEmpty() &&
            !state.isLoading
        ) {
            item(
                key = "load_more"
            ) {
                LaunchedEffect(Unit) {
                    onLoadMore()
                }
            }
        }
    }
}

@Composable
private fun EditSearchMemberRow(
    member: AdminStudyGroupCreateMemberUiModel,
    isChecked: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = enabled,
                onClick = onClick,
            )
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(
                R.drawable.ic_profile_default
            ),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(32.dp),
        )

        Spacer(
            modifier = Modifier.width(8.dp)
        )

        Column(
            modifier = Modifier.weight(1f),
        ) {
            UText(
                text = member.displayName,
                style = UmcTypographyTokens.SubheadlineBold,
                color = if (enabled) {
                    grey800()
                } else {
                    grey500()
                },
            )

            if (member.school.isNotBlank()) {
                Spacer(
                    modifier = Modifier.height(2.dp)
                )

                UText(
                    text = member.school,
                    style = UmcTypographyTokens.Footnote,
                    color = grey600(),
                )
            }
        }

        Icon(
            painter = painterResource(
                if (isChecked) {
                    R.drawable.ic_check_box_primary
                } else {
                    R.drawable.ic_check_box_empty
                }
            ),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(24.dp),
        )
    }
}