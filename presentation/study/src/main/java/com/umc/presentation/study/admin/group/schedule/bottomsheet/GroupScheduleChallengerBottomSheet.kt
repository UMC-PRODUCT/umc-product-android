package com.umc.presentation.study.admin.group.schedule.bottomsheet

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.umc.component.R
import com.umc.component.component.UButton
import com.umc.component.component.UText
import com.umc.component.component.UTextField
import com.umc.component.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupScheduleChallengerBottomSheet(
    viewModel: GroupScheduleChallengerViewModel = hiltViewModel(),
    preSelected: List<GroupScheduleChallengerUiModel>,
    onDismissRequest: () -> Unit,
    onConfirm: (List<GroupScheduleChallengerUiModel>, String) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(Unit) {
        viewModel.resetAfterConfirm()
        viewModel.setSelected(preSelected)
    }

    ModalBottomSheet(
        onDismissRequest = {
            viewModel.resetAfterConfirm()
            onDismissRequest()
        },
        sheetState = sheetState,
        containerColor = grey000(),
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = grey600())
        },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(620.dp)
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
        ) {
            GroupScheduleChallengerHeader(
                showConfirmButton = state.hasConfirmButton,
                isConfirmEnabled = if (state.isSearching) {
                    state.selectedChallengers.isNotEmpty()
                } else {
                    true
                },
                onConfirmClick = {
                    onConfirm(state.selectedChallengers, state.selectedSummaryText)
                    viewModel.resetAfterConfirm()
                    onDismissRequest()
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
                        onToggleClick = viewModel::toggleChallenger
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
            text = "초대할 챌린저를 추가하세요",
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
                textStyle = UmcTypographyTokens.Caption1Bold,
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
            painter = painterResource(id = R.drawable.ic_people),
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
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(challengers, key = { it.id }) { item ->
            GroupScheduleAddedChallengerRow(
                item = item,
                onRemoveClick = { onRemoveClick(item) }
            )
        }
    }
}

@Composable
fun GroupScheduleSearchChallengerList(
    searchResults: List<GroupScheduleChallengerUiModel>,
    selectedChallengers: List<GroupScheduleChallengerUiModel>,
    onToggleClick: (GroupScheduleChallengerUiModel) -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(searchResults, key = { it.id }) { item ->
            val isChecked = selectedChallengers.any { it.id == item.id }

            GroupScheduleSearchChallengerRow(
                item = item,
                isChecked = isChecked,
                onToggleClick = { onToggleClick(item) }
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

        Column(modifier = Modifier.weight(1f)) {
            UText(
                text = item.displayName,
                style = UmcTypographyTokens.Caption1Bold,
                color = grey800()
            )

            Spacer(modifier = Modifier.height(2.dp))

            UText(
                text = item.school,
                style = UmcTypographyTokens.Caption2,
                color = grey800()
            )
        }

        UButton(
            text = "삭제",
            onClick = onRemoveClick,
            modifier = Modifier
                .width(44.dp)
                .height(28.dp),
            backgroundColor = red500().copy(alpha = 0.12f),
            textColor = red500(),
            textStyle = UmcTypographyTokens.Caption2Bold,
            cornerRadius = 6.dp
        )
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
            .clickable { onToggleClick() }
            .padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GroupScheduleChallengerProfile()

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            UText(
                text = item.displayName,
                style = UmcTypographyTokens.Caption1Bold,
                color = grey800()
            )

            Spacer(modifier = Modifier.height(2.dp))

            UText(
                text = item.school,
                style = UmcTypographyTokens.Caption2,
                color = grey800()
            )
        }

        Checkbox(
            checked = isChecked,
            onCheckedChange = { onToggleClick() },
            modifier = Modifier.size(22.dp),
            colors = CheckboxDefaults.colors(
                checkedColor = indigo500(),
                uncheckedColor = grey400()
            )
        )
    }
}

@Composable
fun GroupScheduleChallengerProfile() {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(grey100())
            .border(1.dp, grey200(), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_profile_default),
            contentDescription = null,
            tint = grey400(),
            modifier = Modifier.size(16.dp)
        )
    }
}