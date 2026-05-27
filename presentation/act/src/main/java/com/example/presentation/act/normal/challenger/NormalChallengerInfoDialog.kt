package com.example.presentation.act.normal.challenger

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.umc.component.R
import com.umc.component.component.UInfoChip
import com.umc.component.component.UInfoChipType
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
import com.umc.component.theme.neutral200
import com.umc.component.theme.neutral400
import com.umc.component.theme.neutral600
import com.umc.component.theme.neutral800
import com.umc.component.theme.neutral900
import com.umc.component.theme.success100
import com.umc.component.theme.success500
import com.umc.component.theme.warning100
import com.umc.component.theme.warning500
import com.umc.domain.model.act.challenger.ChallengerManageDialogModel
import com.umc.domain.model.act.challenger.ChallengerPoint
import com.umc.domain.model.enums.CheckHistoryStatus
import com.umc.domain.model.enums.PointType

@Composable
fun NormalChallengerInfoDialog(
    model: ChallengerManageDialogModel,
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

                UInfoChip(
                    text = model.university,
                    type = UInfoChipType.SCHOOL
                )
                UInfoChip(
                    text = model.part,
                    type = UInfoChipType.PART
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
                    value = if (model.gisu > 0) "${model.gisu}기" else "-"
                )

                InfoStatCard(
                    modifier = Modifier.weight(1f),
                    title = "총점수",
                    value = formatPoint(model.totalScore)
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
                    text = "상벌점 기록",
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
private fun HistoryItemRow(history: ChallengerPoint) {
    val status = history.toStatus()
    val bgColor = when (status) {
        CheckHistoryStatus.PRESENT -> success100()
        CheckHistoryStatus.LATE -> warning100()
        CheckHistoryStatus.ABSENT -> danger100()
    }
    val textColor = when (status) {
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
                text = status.text,
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

private fun ChallengerPoint.toStatus(): CheckHistoryStatus {
    return when {
        value > 0 -> CheckHistoryStatus.PRESENT
        value < 0 -> CheckHistoryStatus.LATE
        else -> CheckHistoryStatus.ABSENT
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
        NormalChallengerInfoDialog(
            model = ChallengerManageDialogModel(
                name = "김디자",
                university = "중앙대학교",
                part = "Web",
                gisu = 12,
                totalScore = 1.0,
                history = listOf(
                    ChallengerPoint(id = 1L, title = "우수 워크북", pointType = PointType.BEST_WORKBOOK, value = 1.0),
                    ChallengerPoint(id = 2L, title = "지각", pointType = PointType.STUDY_LATE, value = -0.5),
                    ChallengerPoint(id = 3L, title = "결석", pointType = PointType.STUDY_ABSENT, value = -1.0)
                )
            ),
            onDismissRequest = {}
        )
    }
}
