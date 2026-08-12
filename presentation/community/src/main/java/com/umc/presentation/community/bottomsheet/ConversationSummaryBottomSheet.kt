package com.umc.presentation.community.bottomsheet

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.umc.component.R
import com.umc.component.theme.AppStrings
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.grey400
import com.umc.component.theme.grey500
import com.umc.component.theme.grey600
import com.umc.component.theme.grey800
import com.umc.component.theme.grey950
import com.umc.component.theme.green500
import com.umc.component.theme.indigo500
import com.umc.component.theme.white
import com.umc.presentation.community.chatting.CommunityChattingState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationSummaryBottomSheet(
    state: CommunityChattingState,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = white(),
        dragHandle = {
            Box(
                Modifier
                    .padding(top = 10.dp, bottom = 18.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(grey400()),
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 300.dp)
                .padding(horizontal = 20.dp)
                .padding(bottom = 40.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_ai),
                    contentDescription = null,
                    tint = indigo500(),
                    modifier = Modifier.size(22.dp),
                )
                Text(
                    text = AppStrings.CHAT_AI_SHEET_TITLE,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp),
                    color = grey950(),
                    style = UmcTypographyTokens.Title3Bold,
                )
                IconButton(onClick = onRetry, enabled = !state.isSummarizingUnread) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = AppStrings.CHAT_RETRY,
                        tint = grey500(),
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            when {
                state.isSummarizingUnread -> SummaryLoadingContent(state.aiDownloadPercent)
                state.unreadSummaryError != null -> SummaryErrorContent(state.unreadSummaryError)
                else -> SummarySuccessContent(
                    summary = state.unreadSummary.orEmpty(),
                    messageCount = state.summarizedMessageCount,
                )
            }
        }
    }
}

@Composable
private fun SummaryLoadingContent(downloadPercent: Int?) {
    Text(
        text = downloadPercent
            ?.let { AppStrings.AI_MODEL_DOWNLOADING.format(it) }
            ?: AppStrings.CHAT_AI_SUMMARIZING_DESCRIPTION,
        color = grey600(),
        style = UmcTypographyTokens.Footnote,
    )

    Spacer(Modifier.height(18.dp))

    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_community_ai),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(32.dp),
        )

        Spacer(Modifier.width(10.dp))

        SummaryProgressBar(
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun SummaryProgressBar(
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(
        label = "community_chat_summary_loading",
    )
    val firstProgress = transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "first_progress",
    )
    val secondProgress = transition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1_100),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "second_progress",
    )
    val thirdProgress = transition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1_000),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "third_progress",
    )
    val fourthProgress = transition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1_200),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "fourth_progress",
    )

    Column(
        modifier = modifier,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
    ) {
        SummaryProgressLine(firstProgress.value)
        SummaryProgressLine(secondProgress.value)
        SummaryProgressLine(thirdProgress.value)
        SummaryProgressLine(fourthProgress.value)
    }
}

@Composable
private fun SummaryProgressLine(progress: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(12.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress)
                .height(12.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(indigo500(), green500()),
                    ),
                    shape = RoundedCornerShape(100.dp),
                ),
        )
    }
}

@Composable
private fun SummaryErrorContent(message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = grey400(),
            modifier = Modifier.size(32.dp),
        )
        Spacer(Modifier.height(14.dp))
        Text(
            text = AppStrings.CHAT_AI_SUMMARY_FAILED,
            color = grey600(),
            style = UmcTypographyTokens.HeadlineBold,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = message.ifBlank { AppStrings.CHAT_AI_SUMMARY_RETRY_DESCRIPTION },
            color = grey400(),
            style = UmcTypographyTokens.Subheadline,
        )
    }
}

@Composable
private fun SummarySuccessContent(summary: String, messageCount: Int) {
    Text(
        text = AppStrings.CHAT_AI_SUMMARY_COUNT_FORMAT.format(messageCount),
        color = grey500(),
        style = UmcTypographyTokens.Subheadline,
    )
    Spacer(Modifier.height(14.dp))
    summary
        .lineSequence()
        .map { it.trim().removePrefix("-").removePrefix("•").trim() }
        .filter(String::isNotBlank)
        .forEach { line ->
            Row(
                modifier = Modifier.padding(vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_check),
                    contentDescription = null,
                    modifier = Modifier.size(10.dp),
                    colorFilter = ColorFilter.tint(indigo500()),
                )
                Text(
                    text = line,
                    modifier = Modifier.padding(start = 8.dp),
                    color = grey800(),
                    style = UmcTypographyTokens.Body,
                )
            }
        }
}
