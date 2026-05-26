package com.example.presentation.act.normal.challenger

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.umc.component.R
import com.umc.component.component.UButton
import com.umc.component.component.UText
import com.umc.component.theme.AppStrings
import com.umc.component.theme.UmcTheme
import com.umc.component.theme.UmcTypographyTokens.Callout
import com.umc.component.theme.UmcTypographyTokens.CalloutBold
import com.umc.component.theme.UmcTypographyTokens.Subheadline
import com.umc.component.theme.UmcTypographyTokens.SubheadlineBold
import com.umc.component.theme.UmcTypographyTokens.Title3Bold
import com.umc.component.theme.danger500
import com.umc.component.theme.indigo500
import com.umc.component.theme.neutral000
import com.umc.component.theme.neutral100
import com.umc.component.theme.neutral300
import com.umc.component.theme.neutral400
import com.umc.component.theme.neutral500
import com.umc.component.theme.neutral600
import com.umc.component.theme.neutral800
import com.umc.component.theme.success500

@Composable
fun OtherPointsRoute() {
    OtherPointsScreen()
}

@Composable
fun OtherPointsScreen(
    modifier: Modifier = Modifier,
    onSubmitClick: (reward: Int, punish: Int, reason: String) -> Unit = { _, _, _ -> }
) {
    var rewardScore by remember { mutableStateOf(0) }
    var punishScore by remember { mutableStateOf(0) }
    var reason by remember { mutableStateOf("안 했음") }
    val hasScore = rewardScore > 0 || punishScore > 0
    val hasReason = reason.isNotBlank()
    val isSubmitEnabled = hasScore && hasReason

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(neutral000())
            .padding(horizontal = 16.dp)
    ) {
        DragHeader()

        UText(
            text = AppStrings.REWARD_ETC_TITLE,
            style = Title3Bold,
            color = neutral800()
        )

        Spacer(modifier = Modifier.height(8.dp))

        UText(
            text = AppStrings.REWARD_ETC_CONTENT,
            style = Subheadline,
            color = neutral600()
        )

        Spacer(modifier = Modifier.height(24.dp))

        ScoreStepper(
            label = AppStrings.REWARD,
            value = rewardScore,
            valueColor = success500(),
            onMinusClick = { if (rewardScore > 0) rewardScore-- },
            onPlusClick = { rewardScore++ }
        )

        Spacer(modifier = Modifier.height(16.dp))

        ScoreStepper(
            label = AppStrings.PUNISH,
            value = punishScore,
            valueColor = danger500(),
            onMinusClick = { if (punishScore > 0) punishScore-- },
            onPlusClick = { punishScore++ }
        )

        Spacer(modifier = Modifier.height(16.dp))

        UText(
            text = AppStrings.REWARD_ETC_REASON,
            style = SubheadlineBold,
            color = neutral800()
        )

        Spacer(modifier = Modifier.height(8.dp))

        ReasonInput(
            value = reason,
            onValueChange = { reason = it }
        )

        Spacer(modifier = Modifier.height(128.dp))

        UButton(
            modifier = Modifier.fillMaxWidth(),
            text = AppStrings.REWARD_SUBMIT,
            enabled = isSubmitEnabled,
            textStyle = SubheadlineBold,
            textColor = if (isSubmitEnabled) neutral000() else neutral300(),
            backgroundColor = if (isSubmitEnabled) indigo500() else neutral100(),
            contentPadding = PaddingValues(vertical = 14.dp),
            cornerRadius = 8.dp,
            onClick = { onSubmitClick(rewardScore, punishScore, reason) }
        )

        Spacer(modifier = Modifier.height(48.dp))
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
private fun ScoreStepper(
    label: String,
    value: Int,
    valueColor: Color,
    onMinusClick: () -> Unit,
    onPlusClick: () -> Unit
) {
    val minusTint = if (value == 0) neutral300() else neutral500()

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        UText(
            text = label,
            style = CalloutBold,
            color = neutral800()
        )

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(neutral100())
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
                    tint = neutral500(),
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
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = Callout.copy(color = neutral800()),
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(neutral000())
            .border(1.dp, neutral300(), RoundedCornerShape(8.dp)),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 15.dp),
                contentAlignment = Alignment.TopStart
            ) {
                if (value.isBlank()) {
                    UText(
                        text = AppStrings.REWARD_ETC_PLACEHOLDER,
                        style = Callout,
                        color = neutral400()
                    )
                }
                innerTextField()
            }
        }
    )
}

@Preview(showBackground = false)
@Composable
private fun OtherPointsScreenPreview() {
    UmcTheme(darkTheme = false) {
        OtherPointsScreen()
    }
}
