package com.umc.presentation.study.admin.group.create.bottomsheet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.umc.component.R
import com.umc.component.component.UButton
import com.umc.component.component.UText
import com.umc.component.component.UTextField
import com.umc.component.theme.*
import com.umc.presentation.study.admin.group.create.AdminStudyGroupCreateMemberUiModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupCreateMemberBottomSheet(
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

    LaunchedEffect(preSelected) {
        viewModel.resetAfterDismiss()
        viewModel.setSelected(preSelected)
    }

    ModalBottomSheet(
        onDismissRequest = ::dismissWithApply,
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
            GroupCreatePickerHeader(
                title = if (state.isSearching) {
                    "스터디원을 검색하세요"
                } else {
                    "스터디원을 추가하세요"
                },
                showConfirmButton = state.isSearching,
                isConfirmEnabled = state.selectedMembers.isNotEmpty(),
                onConfirmClick = {
                    viewModel.clearSearchOnly()
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
                prevIconTint = grey500(),
                prevIconSize = 24.dp,
                backgroundColor = grey100(),
                focusBackgroundColor = grey000(),
                strokeColor = grey100(),
                focusStrokeColor = grey900(),
                cornerRadius = 8.dp,
            )

            Spacer(modifier = Modifier.height(28.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (!state.isSearching) {
                    if (state.selectedMembers.isEmpty()) {
                        GroupCreateEmptyContent(text = "아직 추가한 스터디원이 없어요")
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
                    val groupedResults = state.searchResults
                        .groupBy { member -> member.partLabel }
                        .toList()

                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        groupedResults.forEach { (partLabel, members) ->

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

                            items(
                                items = members,
                                key = { member -> member.id }
                            ) { item ->
                                val isChecked = state.selectedMembers.any {
                                        selectedMember -> selectedMember.id == item.id
                                }

                                GroupCreateMultiSearchRow(
                                    item = item,
                                    isChecked = isChecked,
                                    onToggleClick = {
                                        viewModel.toggleMember(item)
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
fun GroupCreateEmptyContent(
    text: String,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_people),
            contentDescription = null,
            modifier = Modifier.size(42.dp),
            tint = grey400()
        )

        Spacer(modifier = Modifier.height(12.dp))

        UText(
            text = text,
            style = UmcTypographyTokens.Body,
            color = grey600()
        )
    }
}

@Composable
fun GroupCreateAddedMemberRow(
    item: AdminStudyGroupCreateMemberUiModel,
    onRemoveClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GroupCreateMemberProfile()

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            UText(
                text = item.displayName,
                style = UmcTypographyTokens.SubheadlineBold,
                color = grey800()
            )

            Spacer(modifier = Modifier.height(2.dp))

            UText(
                text = item.school,
                style = UmcTypographyTokens.Footnote,
                color = grey800()
            )
        }

        UButton(
            text = "삭제",
            onClick = onRemoveClick,
            modifier = Modifier
                .width(50.dp)
                .height(32.dp),
            backgroundColor = red500().copy(alpha = 0.12f),
            textColor = red500(),
            textStyle = UmcTypographyTokens.SubheadlineBold,
            cornerRadius = 6.dp
        )
    }
}



@Composable
fun GroupCreatePickerHeader(
    title: String,
    showConfirmButton: Boolean,
    isConfirmEnabled: Boolean,
    onConfirmClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        UText(
            text = title,
            style = UmcTypographyTokens.Title3Bold,
            color = grey800(),
            modifier = Modifier.weight(1f)
        )

        if (showConfirmButton) {
            UButton(
                text = "확인",
                onClick = onConfirmClick,
                enabled = isConfirmEnabled,
                modifier = Modifier
                    .width(52.dp)
                    .height(32.dp),
                backgroundColor = if (isConfirmEnabled) indigo500() else grey100(),
                textColor = if (isConfirmEnabled) grey000() else grey400(),
                textStyle = UmcTypographyTokens.SubheadlineBold,
                cornerRadius = 8.dp
            )
        }
    }
}
@Composable
fun GroupCreateMultiSearchRow(
    item: AdminStudyGroupCreateMemberUiModel,
    isChecked: Boolean,
    onToggleClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleClick() }
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GroupCreateMemberProfile()

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            UText(
                text = item.displayName,
                style = UmcTypographyTokens.SubheadlineBold,
                color = grey800()
            )

            Spacer(modifier = Modifier.height(2.dp))

            UText(
                text = item.school,
                style = UmcTypographyTokens.Footnote,
                color = grey800()
            )
        }

        Icon(
            painter = painterResource(
                id = if (isChecked) {
                    R.drawable.ic_check_box_primary
                } else {
                    R.drawable.ic_check_box_empty
                }
            ),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun GroupCreateMemberProfile() {
        Icon(
            painter = painterResource(id = R.drawable.ic_profile_default),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(32.dp)
        )
}

@Composable
fun GroupCreatePickerTitle(
    title: String,
) {
    UText(
        text = title,
        style = UmcTypographyTokens.Title3Bold,
        color = grey800(),
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Composable
fun GroupCreateSelectSearchRow(
    item: AdminStudyGroupCreateMemberUiModel,
    onSelectClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        GroupCreateMemberProfile()

        Spacer(modifier = Modifier.width(8.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            UText(
                text = item.displayName,
                style = UmcTypographyTokens.SubheadlineBold,
                color = grey800()
            )

            Spacer(modifier = Modifier.height(2.dp))

            UText(
                text = item.school,
                style = UmcTypographyTokens.SubheadlineBold,
                color = grey800()
            )
        }

        UButton(
            text = "선택",
            onClick = onSelectClick,
            modifier = Modifier
                .width(42.dp)
                .height(32.dp),
            backgroundColor = grey100(),
            textColor = grey700(),
            textStyle = UmcTypographyTokens.SubheadlineBold,
            cornerRadius = 8.dp,
        )
    }
}