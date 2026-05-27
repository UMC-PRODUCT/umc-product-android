package com.example.presentation.act.admin.challenger

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.umc.component.R
import com.umc.component.component.UButton
import com.umc.component.component.UChip
import com.umc.component.component.UText
import com.umc.component.component.UTextField
import com.umc.component.theme.AppStrings
import com.umc.component.theme.UmcTheme
import com.umc.component.theme.UmcTypographyTokens.Body
import com.umc.component.theme.UmcTypographyTokens.Callout
import com.umc.component.theme.UmcTypographyTokens.Caption1Bold
import com.umc.component.theme.UmcTypographyTokens.HeadlineBold
import com.umc.component.theme.UmcTypographyTokens.Subheadline
import com.umc.component.theme.UmcTypographyTokens.SubheadlineBold
import com.umc.component.theme.UmcTypographyTokens.Title3Bold
import com.umc.component.theme.danger100
import com.umc.component.theme.danger500
import com.umc.component.theme.indigo500
import com.umc.component.theme.neutral000
import com.umc.component.theme.neutral100
import com.umc.component.theme.neutral200
import com.umc.component.theme.neutral300
import com.umc.component.theme.neutral400
import com.umc.component.theme.neutral500
import com.umc.component.theme.neutral600
import com.umc.component.theme.neutral800
import com.umc.component.theme.neutral900
import com.umc.domain.model.enums.PunishCategory
import com.umc.domain.model.enums.RewardType

@Composable
fun PenaltyPointsRoute(
    challengerId: Long = 0L,
    viewModel: AdminChallengerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    PenaltyPointsScreen(
        uiState = uiState,
        onFilterSelected = viewModel::selectPenaltyFilter,
        onSelectPenalty = viewModel::selectPenalty,
        onMemoChange = viewModel::onMemoChanged,
        onSubmitClick = { viewModel.grantPenalty(challengerId) }
    )
}

@Composable
fun PenaltyPointsScreen(
    modifier: Modifier = Modifier,
    penalties: List<PenaltyPointItem> = defaultPenaltyItems(),
    uiState: AdminChallengerUiState = AdminChallengerUiState(),
    onFilterSelected: (PunishCategory) -> Unit = {},
    onSelectPenalty: (RewardType) -> Unit = {},
    onMemoChange: (String) -> Unit = {},
    onSubmitClick: () -> Unit = {}
) {
    val filteredPenalties = penalties.filter {
        uiState.selectedPenaltyFilter == PunishCategory.ALL || it.filter == uiState.selectedPenaltyFilter
    }
    val selectedPenalty = uiState.selectedPenaltyType
    val hasMemo = uiState.pointMemo.isNotBlank()
    val isSubmitEnabled = selectedPenalty != null && hasMemo

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(848.dp)
            .imePadding()
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(neutral000())
            .padding(horizontal = 16.dp)
    ) {
        DragHeader()

        Spacer(modifier = Modifier.height(16.dp))

        UText(
            text = AppStrings.PUNISH_TITLE,
            style = Title3Bold,
            color = neutral800()
        )

        Spacer(modifier = Modifier.height(8.dp))

        UText(
            text = AppStrings.PUNISH_CONTENT,
            style = Subheadline,
            color = neutral600()
        )

        Spacer(modifier = Modifier.height(16.dp))

        FilterTabs(
            selectedFilter = uiState.selectedPenaltyFilter,
            onFilterSelected = onFilterSelected
        )

        Spacer(modifier = Modifier.height(16.dp))

        PenaltyList(
            penalties = filteredPenalties,
            selectedPenaltyType = uiState.selectedPenaltyType,
            onSelectPenalty = onSelectPenalty
        )

        Spacer(modifier = Modifier.height(16.dp))

        UText(
            text = AppStrings.MEMO,
            style = SubheadlineBold,
            color = neutral800()
        )

        Spacer(modifier = Modifier.height(8.dp))

        MemoInput(
            value = uiState.pointMemo,
            onValueChange = onMemoChange
        )

        Spacer(modifier = Modifier.weight(1f))

        UButton(
            modifier = Modifier.fillMaxWidth(),
            text = AppStrings.REWARD_SUBMIT,
            enabled = isSubmitEnabled,
            textStyle = HeadlineBold,
            textColor = if (isSubmitEnabled) neutral000() else neutral300(),
            backgroundColor = if (isSubmitEnabled) indigo500() else neutral100(),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 16.dp),
            cornerRadius = 8.dp,
            onClick = onSubmitClick
        )

        Spacer(modifier = Modifier.height(72.dp))
    }
}

@Composable
private fun DragHeader(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(36.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(100.dp))
                .background(neutral600())
        )
    }
}

@Composable
private fun FilterTabs(
    selectedFilter: PunishCategory,
    onFilterSelected: (PunishCategory) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        PunishCategory.entries.forEach { filter ->
            val selected = selectedFilter == filter

            UChip(
                text = filter.label,
                backgroundColor = if (selected) neutral900() else neutral100(),
                textColor = if (selected) neutral000() else neutral500(),
                textStyle = SubheadlineBold,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                onClick = { onFilterSelected(filter) }
            )
        }
    }
}

@Composable
private fun PenaltyList(
    penalties: List<PenaltyPointItem>,
    selectedPenaltyType: RewardType?,
    onSelectPenalty: (RewardType) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        penalties.forEachIndexed { index, item ->
            PenaltyRow(
                item = item,
                selected = selectedPenaltyType == item.type,
                onClick = { onSelectPenalty(item.type) }
            )

            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                color = neutral200()
            )

        }
    }
}

@Composable
private fun PenaltyRow(
    item: PenaltyPointItem,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UText(
                text = item.title,
                style = Body,
                color = neutral800()
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .wrapContentWidth()
                    .height(24.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(danger100())
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                UText(
                    text = "-${item.score}",
                    style = Caption1Bold,
                    color = danger500()
                )
            }
        }

        Icon(
            painter = painterResource(
                if (selected) R.drawable.ic_radio_button_checked else R.drawable.ic_radio_button_unchecked
            ),
            contentDescription = null,
            tint = if (selected) indigo500() else neutral400()
        )
    }
}

@Composable
private fun MemoInput(
    value: String,
    onValueChange: (String) -> Unit
) {
    UTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = AppStrings.MEMO_PLACEHOLDER,
        placeholderColor = neutral400(),
        textColor = neutral800(),
        textStyle = Callout.copy(color = neutral800()),
        backgroundColor = neutral000(),
        strokeColor = neutral300(),
        focusStrokeColor = neutral900(),
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
    )
}

data class PenaltyPointItem(
    val id: Long,
    val title: String,
    val score: Int,
    val filter: PunishCategory,
    val type: RewardType,
)

private fun defaultPenaltyItems(): List<PenaltyPointItem> =
    RewardType.getPenaltyList().mapIndexed { index, type ->
        PenaltyPointItem(
            id = index.toLong(),
            title = type.label,
            score = -type.score,
            filter = type.category,
            type = type
        )
    }

@Preview(showBackground = false)
@Composable
private fun PenaltyPointsScreenPreview() {
    UmcTheme(darkTheme = false) {
        PenaltyPointsScreen()
    }
}
