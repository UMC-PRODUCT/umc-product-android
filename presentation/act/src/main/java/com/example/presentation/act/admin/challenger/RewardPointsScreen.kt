package com.example.presentation.act.admin.challenger

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.umc.component.component.UText
import com.umc.component.component.UTextField
import com.umc.component.theme.AppStrings
import com.umc.component.theme.UmcTheme
import com.umc.component.theme.UmcTypographyTokens.Body
import com.umc.component.theme.UmcTypographyTokens.Callout
import com.umc.component.theme.UmcTypographyTokens.CalloutBold
import com.umc.component.theme.UmcTypographyTokens.Caption1Bold
import com.umc.component.theme.UmcTypographyTokens.HeadlineBold
import com.umc.component.theme.UmcTypographyTokens.Subheadline
import com.umc.component.theme.UmcTypographyTokens.Title3Bold
import com.umc.component.theme.indigo500
import com.umc.component.theme.neutral000
import com.umc.component.theme.neutral100
import com.umc.component.theme.neutral200
import com.umc.component.theme.neutral300
import com.umc.component.theme.neutral400
import com.umc.component.theme.neutral600
import com.umc.component.theme.neutral800
import com.umc.component.theme.neutral900
import com.umc.component.theme.success100
import com.umc.component.theme.success500
import com.umc.domain.model.enums.RewardType

@Composable
fun RewardPointsRoute(
    challengerId: Long = 0L,
    viewModel: AdminChallengerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    RewardPointsScreen(
        uiState = uiState,
        onSelectReward = viewModel::selectReward,
        onMemoChange = viewModel::onMemoChanged,
        onSubmitClick = { viewModel.grantReward(challengerId) }
    )
}

@Composable
fun RewardPointsScreen(
    modifier: Modifier = Modifier,
    rewards: List<RewardPointItem> = defaultRewardItems(),
    uiState: AdminChallengerUiState = AdminChallengerUiState(),
    onSelectReward: (RewardType) -> Unit = {},
    onMemoChange: (String) -> Unit = {},
    onSubmitClick: () -> Unit = {}
) {
    val selectedReward = uiState.selectedRewardType
    val isSubmitEnabled = selectedReward != null

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(700.dp)
            .imePadding()
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(neutral000())
            .padding(horizontal = 16.dp)
    ) {
        DragHeader()

        Spacer(modifier = Modifier.height(16.dp))

        UText(
            text = AppStrings.REWARD_TITLE,
            style = Title3Bold,
            color = neutral800()
        )

        Spacer(modifier = Modifier.height(8.dp))

        UText(
            text = AppStrings.REWARD_CONTENT,
            style = Subheadline,
            color = neutral600()
        )

        Spacer(modifier = Modifier.height(16.dp))

        RewardListCard(
            rewards = rewards,
            selectedRewardType = uiState.selectedRewardType,
            onSelectReward = onSelectReward
        )

        Spacer(modifier = Modifier.height(16.dp))

        UText(
            text = AppStrings.MEMO,
            style = CalloutBold,
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
private fun RewardListCard(
    rewards: List<RewardPointItem>,
    selectedRewardType: RewardType?,
    onSelectReward: (RewardType) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(neutral000())
    ) {
        rewards.forEachIndexed { index, item ->
            RewardRow(
                item = item,
                selected = selectedRewardType == item.type,
                onClick = { onSelectReward(item.type) }
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
private fun RewardRow(
    item: RewardPointItem,
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
                    .background(success100())
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                UText(
                    text = "+${item.score}",
                    style = Caption1Bold,
                    color = success500(),
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
        textStyle = Callout,
        backgroundColor = neutral000(),
        strokeColor = neutral300(),
        focusStrokeColor = neutral900(),
        modifier = Modifier
            .fillMaxWidth()
            .height(92.dp)
    )
}

data class RewardPointItem(
    val id: Long,
    val title: String,
    val score: Int,
    val type: RewardType,
)

private fun defaultRewardItems(): List<RewardPointItem> =
    RewardType.getBonusList().mapIndexed { index, type ->
        RewardPointItem(id = index.toLong(), title = type.label, score = type.score, type = type)
    }

@Preview(showBackground = false)
@Composable
private fun RewardPointsScreenPreview() {
    UmcTheme(darkTheme = false) {
        RewardPointsScreen()
    }
}
