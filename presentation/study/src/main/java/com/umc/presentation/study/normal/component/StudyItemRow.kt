package com.umc.presentation.study.normal.component

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.umc.component.R
import com.umc.component.component.UText
import com.umc.component.theme.*
import com.umc.presentation.study.normal.NormalStudyItemUiModel

@Composable
fun StudyItemRow(
    item: NormalStudyItemUiModel,
    onToggle: () -> Unit,
    onSubmitClick: (Long, String) -> Unit,
    onConfirmClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val alpha = if (item.isLocked) 0.35f else 1f
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        // 왼쪽 타임라인
        Column(
            modifier = Modifier
                .padding(start = 16.dp)
                .width(32.dp)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))
            StudyTimelineColumn(
                week = item.week,
                status = item.status,
                isLocked = item.isLocked,
                modifier = Modifier.alpha(alpha)
            )
            Spacer(Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .weight(1f)
                    .background(neutral200())
            )
        }

        Spacer(Modifier.width(12.dp))

        // 오른쪽 카드
        Surface(
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                ) { if (!item.isLocked) onToggle() }
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ),
            shape = RoundedCornerShape(16.dp),
            color = neutral000(),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            Column(
                modifier = Modifier
                    .alpha(alpha)
                    .padding(16.dp)
            ) {
                // 태그 + 뱃지 + 드롭다운
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            StudyTagChip(text = AppStrings.STUDY_WEEK_FORMAT.format(item.week))
                            Spacer(Modifier.width(6.dp))
                            StudyTagChip(text = item.platform)
                        }
                        Spacer(Modifier.height(10.dp))
                        UText(
                            text = item.title,
                            style = UmcTypographyTokens.HeadlineBold,
                            color = neutral800(),
                            maxLines = 2,
                        )
                    }

                    Spacer(Modifier.width(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (item.isBest) {
                            StudyBestBadge()
                            Spacer(Modifier.width(6.dp))
                        }
                        if (!item.isLocked) {
                            StudyStatusBadge(item = item)
                            Spacer(Modifier.width(8.dp))
                            Icon(
                                painter = painterResource(
                                    if (item.isExpanded) R.drawable.ic_dropdown_up
                                    else R.drawable.ic_dropdown_down
                                ),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = neutral500()
                            )
                        }
                    }
                }

                // 펼쳐지는 영역
                if (item.isExpanded && !item.isLocked) {
                    Column {
                        Spacer(Modifier.height(12.dp))
                        if (item.description.isNotBlank()) {
                            UText(
                                text = item.description,
                                style = UmcTypographyTokens.Footnote,
                                color = neutral500(),
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        StudyExpandedContent(
                            item = item,
                            onSubmitClick = onSubmitClick,
                            onConfirmClick = onConfirmClick,
                        )
                    }
                }
            }
        }
    }
}