package com.umc.presentation.study.normal.component

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.umc.component.R
import com.umc.component.component.UText
import com.umc.component.theme.AppStrings
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.grey000
import com.umc.component.theme.grey200
import com.umc.component.theme.grey500
import com.umc.component.theme.grey800
import com.umc.presentation.study.normal.NormalStudyItemUiModel

@Composable
fun StudyItemRow(
    item: NormalStudyItemUiModel,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val alpha = if (item.isLocked) 0.35f else 1f
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier
                .padding(start = 16.dp)
                .width(32.dp)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            StudyTimelineColumn(
                week = item.week,
                status = item.status,
                isLocked = item.isLocked,
                modifier = Modifier.alpha(alpha),
            )

            Spacer(modifier = Modifier.height(4.dp))

            Box(
                modifier = Modifier
                    .width(2.dp)
                    .weight(1f)
                    .background(grey200()),
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Surface(
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp)
                .clickable(
                    enabled = !item.isLocked,
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onToggle,
                )
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow,
                    )
                ),
            shape = RoundedCornerShape(8.dp),
            color = grey000(),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            Column(
                modifier = Modifier
                    .alpha(alpha)
                    .padding(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            StudyTagChip(
                                text = AppStrings.STUDY_WEEK_FORMAT.format(item.week),
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            StudyTagChip(
                                text = item.platform,
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        UText(
                            text = item.title,
                            style = UmcTypographyTokens.HeadlineBold,
                            color = grey800(),
                            maxLines = 2,
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (item.isBest) {
                            StudyBestBadge()
                            Spacer(modifier = Modifier.width(6.dp))
                        }

                        if (!item.isLocked) {
                            StudyStatusBadge(item = item)

                            Spacer(modifier = Modifier.width(8.dp))

                            Icon(
                                painter = painterResource(
                                    if (item.isExpanded) {
                                        R.drawable.ic_dropdown_up
                                    } else {
                                        R.drawable.ic_dropdown_down
                                    }
                                ),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = grey500(),
                            )
                        }
                    }
                }

                if (item.isExpanded && !item.isLocked) {
                    Spacer(modifier = Modifier.height(12.dp))

                    if (item.description.isNotBlank()) {
                        UText(
                            text = item.description,
                            style = UmcTypographyTokens.Footnote,
                            color = grey500(),
                        )
                    }

                    if (
                        item.status == com.umc.domain.model.enums.StudyStatus.PASS ||
                        item.status == com.umc.domain.model.enums.StudyStatus.FAIL
                    ) {
                        Spacer(modifier = Modifier.height(12.dp))

                        StudyExpandedContent(
                            item = item,
                        )
                    }
                }
            }
        }
    }
}