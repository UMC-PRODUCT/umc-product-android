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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.umc.component.theme.UmcTypographyTokens.Callout
import com.umc.component.theme.UmcTypographyTokens.CalloutBold
import com.umc.component.theme.UmcTypographyTokens.HeadlineBold
import com.umc.component.theme.UmcTypographyTokens.Subheadline
import com.umc.component.theme.UmcTypographyTokens.Title3Bold
import com.umc.component.theme.red500
import com.umc.component.theme.indigo500
import com.umc.component.theme.grey000
import com.umc.component.theme.grey100
import com.umc.component.theme.grey300
import com.umc.component.theme.grey400
import com.umc.component.theme.grey500
import com.umc.component.theme.grey600
import com.umc.component.theme.grey800
import com.umc.component.theme.grey900
import com.umc.component.theme.green500

@Composable
fun OtherPointsRoute(
    challengerId: Long = 0L,
    viewModel: AdminChallengerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    OtherPointsScreen(
        uiState = uiState,
        onRewardMinusClick = viewModel::decreaseRewardScore,
        onRewardPlusClick = viewModel::increaseRewardScore,
        onPunishMinusClick = viewModel::decreasePunishScore,
        onPunishPlusClick = viewModel::increasePunishScore,
        onReasonChange = viewModel::onCustomReasonChanged,
        onSubmitClick = { viewModel.grantCustomPoint(challengerId) }
    )
}

@Composable
fun OtherPointsScreen(
    modifier: Modifier = Modifier,
    uiState: AdminChallengerUiState = AdminChallengerUiState(),
    onRewardMinusClick: () -> Unit = {},
    onRewardPlusClick: () -> Unit = {},
    onPunishMinusClick: () -> Unit = {},
    onPunishPlusClick: () -> Unit = {},
    onReasonChange: (String) -> Unit = {},
    onSubmitClick: () -> Unit = {}
) {
    val hasScore = uiState.customRewardScore > 0 || uiState.customPunishScore > 0
    val hasReason = uiState.customReason.isNotBlank()
    val isSubmitEnabled = hasScore && hasReason

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(700.dp)
            .imePadding()
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(grey000())
            .padding(horizontal = 16.dp)
    ) {
        DragHeader()

        Spacer(modifier = Modifier.height(16.dp))

        UText(
            text = AppStrings.REWARD_ETC_TITLE,
            style = Title3Bold,
            color = grey800()
        )

        Spacer(modifier = Modifier.height(8.dp))

        UText(
            text = AppStrings.REWARD_ETC_CONTENT,
            style = Subheadline,
            color = grey600()
        )

        Spacer(modifier = Modifier.height(24.dp))

        ScoreStepper(
            label = AppStrings.REWARD,
            value = uiState.customRewardScore,
            valueColor = green500(),
            onMinusClick = onRewardMinusClick,
            onPlusClick = onRewardPlusClick
        )

        Spacer(modifier = Modifier.height(16.dp))

        ScoreStepper(
            label = AppStrings.PUNISH,
            value = uiState.customPunishScore,
            valueColor = red500(),
            onMinusClick = onPunishMinusClick,
            onPlusClick = onPunishPlusClick
        )

        Spacer(modifier = Modifier.height(16.dp))

        UText(
            text = AppStrings.REWARD_ETC_REASON,
            style = CalloutBold,
            color = grey800()
        )

        Spacer(modifier = Modifier.height(8.dp))

        ReasonInput(
            value = uiState.customReason,
            onValueChange = onReasonChange
        )

        Spacer(modifier = Modifier.weight(1f))

        UButton(
            modifier = Modifier.fillMaxWidth(),
            text = AppStrings.REWARD_SUBMIT,
            enabled = isSubmitEnabled,
            textStyle = HeadlineBold,
            textColor = if (isSubmitEnabled) grey000() else grey300(),
            backgroundColor = if (isSubmitEnabled) indigo500() else grey100(),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 16.dp),
            cornerRadius = 8.dp,
            onClick = onSubmitClick
        )

        Spacer(modifier = Modifier.height(24.dp))
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
                .background(grey600())
        )
    }
}

@Composable
private fun ScoreStepper(
    label: String,
    value: Int,
    valueColor: Color,
    onMinusClick: () -> Unit,
    onPlusClick: () -> Unit
) {
    val minusTint = if (value == 0) grey300() else grey500()

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        UText(
            text = label,
            style = CalloutBold,
            color = grey800()
        )

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(grey100())
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clickable(
                        enabled = value > 0,
                        onClick = onMinusClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_minus_circle),
                    contentDescription = null,
                    tint = minusTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            UText(
                text = value.toString(),
                style = Title3Bold,
                color = valueColor
            )

            Spacer(modifier = Modifier.width(16.dp))

            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clickable(onClick = onPlusClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_plus_circle),
                    contentDescription = null,
                    tint = grey500(),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun ReasonInput(
    value: String,
    onValueChange: (String) -> Unit
) {
    UTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = AppStrings.REWARD_ETC_PLACEHOLDER,
        placeholderColor = grey400(),
        textColor = grey800(),
        textStyle = Callout,
        backgroundColor = grey000(),
        strokeColor = grey300(),
        focusStrokeColor = grey900(),
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
    )
}

@Preview(showBackground = false)
@Composable
private fun OtherPointsScreenPreview() {
    UmcTheme(darkTheme = false) {
        OtherPointsScreen()
    }
}
