package com.umc.presentation.study.admin.group.schedule.bottomsheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import com.umc.component.theme.grey700
import com.umc.component.theme.grey800
import com.umc.component.theme.grey900
import com.umc.component.theme.indigo500
import com.umc.component.theme.red500

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupScheduleChallengerBottomSheet(
    viewModel: GroupScheduleChallengerViewModel = hiltViewModel(),
    preSelected: List<GroupScheduleChallengerUiModel>,
    onDismissRequest: () -> Unit,
    onConfirm: (List<GroupScheduleChallengerUiModel>, String) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    fun dismissWithApply() {
        onConfirm(
            state.selectedChallengers,
            state.selectedSummaryText
        )
        viewModel.resetAfterConfirm()
        onDismissRequest()
    }

    LaunchedEffect(preSelected) {
        viewModel.resetAfterConfirm()
        viewModel.setSelected(preSelected)
    }

    ModalBottomSheet(
        onDismissRequest = ::dismissWithApply,
        sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        ),
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
            GroupScheduleChallengerHeader(
                title = if (state.isSearching) {
                    "초대할 챌린저를 검색하세요"
                } else {
                    "초대할 챌린저를 추가하세요"
                },
                showConfirmButton = state.isSearching,
                isConfirmEnabled = state.selectedChallengers.isNotEmpty(),
                onConfirmClick = {
                    viewModel.clearSearchOnly()
                }
            )

            Spacer(modifier = Modifier.height(18.dp))

            UTextField(
                value = state.query,
                onValueChange = viewModel::searchChallengers,
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
                if (!state.isSearching) {
                    if (state.selectedChallengers.isEmpty()) {
                        GroupScheduleEmptyChallengerContent()
                    } else {
                        GroupScheduleSelectedChallengerList(
                            challengers = state.selectedChallengers,
                            onRemoveClick = viewModel::toggleChallenger
                        )
                    }
                } else {
                    GroupScheduleSearchChallengerList(
                        searchResults = state.searchResults,
                        selectedChallengers = state.selectedChallengers,
                        isLoading = state.isLoading,
                        hasNext = state.hasNext,
                        onToggleClick = viewModel::toggleChallenger,
                        onLoadMore = viewModel::loadMoreChallengers,
                    )
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
fun GroupScheduleChallengerHeader(
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
                cornerRadius = 8.dp
            )
        }
    }
}

@Composable
fun GroupScheduleEmptyChallengerContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(
                id = R.drawable.ic_people
            ),
            contentDescription = null,
            modifier = Modifier.size(42.dp),
            tint = grey400()
        )

        Spacer(modifier = Modifier.height(12.dp))

        UText(
            text = "아직 초대한 챌린저가 없어요",
            style = UmcTypographyTokens.Body,
            color = grey600()
        )
    }
}

@Composable
fun GroupScheduleSelectedChallengerList(
    challengers: List<GroupScheduleChallengerUiModel>,
    onRemoveClick: (GroupScheduleChallengerUiModel) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(
            items = challengers,
            key = { challenger ->
                challenger.id
            }
        ) { item ->
            GroupScheduleAddedChallengerRow(
                item = item,
                onRemoveClick = {
                    onRemoveClick(item)
                }
            )
        }
    }
}

@Composable
fun GroupScheduleAddedChallengerRow(
    item: GroupScheduleChallengerUiModel,
    onRemoveClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GroupScheduleChallengerProfile()

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
fun GroupScheduleSearchChallengerList(
    searchResults: List<GroupScheduleChallengerUiModel>,
    selectedChallengers: List<GroupScheduleChallengerUiModel>,
    isLoading: Boolean,
    hasNext: Boolean,
    onToggleClick: (GroupScheduleChallengerUiModel) -> Unit,
    onLoadMore: () -> Unit,
) {
    val groupedResults = searchResults
        .groupBy { challenger ->
            challenger.partLabel
        }
        .toList()

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        groupedResults.forEach { (partLabel, challengers) ->
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
                                bottom = 8.dp,
                            )
                    )
                }
            }

            items(
                items = challengers,
                key = { challenger ->
                    challenger.id
                }
            ) { item ->
                val isChecked = selectedChallengers.any { selected ->
                    selected.id == item.id
                }

                GroupScheduleSearchChallengerRow(
                    item = item,
                    isChecked = isChecked,
                    onToggleClick = {
                        onToggleClick(item)
                    },
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

        if (
            hasNext &&
            searchResults.isNotEmpty() &&
            !isLoading
        ) {
            item(key = "load_more") {
                LaunchedEffect(
                    searchResults.size,
                    hasNext,
                ) {
                    onLoadMore()
                }
            }
        }
    }
}




@Composable
fun GroupScheduleSearchChallengerRow(
    item: GroupScheduleChallengerUiModel,
    isChecked: Boolean,
    onToggleClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onToggleClick()
            }
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GroupScheduleChallengerProfile()

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
fun GroupScheduleChallengerProfile() {
    Icon(
        painter = painterResource(
            id = R.drawable.ic_profile_default
        ),
        contentDescription = null,
        tint = Color.Unspecified,
        modifier = Modifier.size(32.dp)
    )
}