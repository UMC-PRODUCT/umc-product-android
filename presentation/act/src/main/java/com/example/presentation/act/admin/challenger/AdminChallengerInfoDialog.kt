package com.example.presentation.act.admin.challenger

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.umc.component.R
import com.umc.component.component.UText
import com.umc.component.theme.UmcTheme
import com.umc.component.theme.UmcTypographyTokens.Callout
import com.umc.component.theme.UmcTypographyTokens.Caption1Bold
import com.umc.component.theme.UmcTypographyTokens.HeadlineBold
import com.umc.component.theme.UmcTypographyTokens.Subheadline
import com.umc.component.theme.UmcTypographyTokens.Title3Bold
import com.umc.component.theme.danger100
import com.umc.component.theme.danger500
import com.umc.component.theme.neutral000
import com.umc.component.theme.neutral050
import com.umc.component.theme.neutral200
import com.umc.component.theme.neutral400
import com.umc.component.theme.neutral600
import com.umc.component.theme.neutral800
import com.umc.component.theme.neutral900
import com.umc.component.theme.primary100
import com.umc.component.theme.primary500
import com.umc.component.theme.success100
import com.umc.component.theme.success500
import com.umc.component.theme.warning100
import com.umc.component.theme.warning500
import com.umc.domain.model.act.challenger.ChallengerInfoDialogModel
import com.umc.domain.model.act.challenger.ChallengerInfoHistory
import com.umc.domain.model.enums.CheckHistoryStatus

@Composable
fun ChallengerInfoDialog(
    model: ChallengerInfoDialogModel,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(neutral000())
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .border(1.dp, neutral200(), CircleShape)
                        .background(neutral000()),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_person),
                        contentDescription = null,
                        tint = neutral400(),
                        modifier = Modifier.size(39.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_close_big),
                        contentDescription = null,
                        tint = neutral600(),
                        modifier = Modifier
                            .size(28.dp)
                            .clickable(onClick = onDismissRequest)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                UText(
                    text = model.name,
                    style = Title3Bold,
                    color = neutral800()
                )

                InfoChip(
                    text = model.university,
                    background = neutral050(),
                    textColor = neutral600()
                )
                InfoChip(
                    text = model.part,
                    background = primary100(),
                    textColor = primary500()
                )
            }

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                InfoStatCard(
                    modifier = Modifier.weight(1f),
                    title = "활동 기수",
                    value = if (model.generation > 0) "${model.generation}기" else "-"
                )

                InfoStatCard(
                    modifier = Modifier.weight(1f),
                    title = "총점수",
                    value = formatPoint(model.totalPoints)
                )
            }

            Spacer(Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_history),
                    contentDescription = null,
                    tint = neutral600(),
                    modifier = Modifier.size(24.dp)
                )
                UText(
                    text = "출석/활동 기록",
                    style = HeadlineBold,
                    color = neutral900()
                )
            }

            Spacer(Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, neutral200(), RoundedCornerShape(12.dp))
                    .background(neutral000())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                model.history.forEach { history ->
                    HistoryItemRow(history = history)
                }
            }
        }
    }
}

@Composable
private fun InfoChip(
    text: String,
    background: Color,
    textColor: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(background)
            .height(24.dp)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        UText(
            text = text,
            style = Caption1Bold,
            color = textColor
        )
    }
}

@Composable
private fun InfoStatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, neutral200(), RoundedCornerShape(12.dp))
            .background(neutral000())
            .padding(vertical = 18.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        UText(
            text = title,
            style = Callout,
            color = neutral600()
        )
        UText(
            text = value,
            style = HeadlineBold,
            color = neutral800()
        )
    }
}

@Composable
private fun HistoryItemRow(history: ChallengerInfoHistory) {
    val bgColor = when (history.status) {
        CheckHistoryStatus.PRESENT -> success100()
        CheckHistoryStatus.LATE -> warning100()
        CheckHistoryStatus.ABSENT -> danger100()
    }
    val textColor = when (history.status) {
        CheckHistoryStatus.PRESENT -> success500()
        CheckHistoryStatus.LATE -> warning500()
        CheckHistoryStatus.ABSENT -> danger500()
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(bgColor)
                .height(24.dp)
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            UText(
                text = history.status.text,
                style = Caption1Bold,
                color = textColor
            )
        }

        UText(
            text = history.title,
            style = Subheadline,
            color = neutral800()
        )
    }
}

private fun formatPoint(point: Double): String {
    return if (point % 1.0 == 0.0) {
        point.toInt().toString()
    } else {
        point.toString()
    }
}

@Preview(showBackground = false)
@Composable
private fun ChallengerInfoDialogPreview() {
    UmcTheme(darkTheme = false) {
        ChallengerInfoDialog(
            model = ChallengerInfoDialogModel(
                name = "김디자",
                university = "중앙대학교",
                part = "Web",
                generation = 12,
                totalPoints = 1.0,
                history = listOf(
                    ChallengerInfoHistory(title = "3주차 정기 세션", status = CheckHistoryStatus.PRESENT),
                    ChallengerInfoHistory(title = "2주차 정기 세션", status = CheckHistoryStatus.LATE),
                    ChallengerInfoHistory(title = "1주차 정기 세션", status = CheckHistoryStatus.ABSENT)
                )
            ),
            onDismissRequest = {}
        )
    }
}
