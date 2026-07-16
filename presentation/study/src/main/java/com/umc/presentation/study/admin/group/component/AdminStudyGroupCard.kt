package com.umc.presentation.study.admin.group.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.umc.component.R
import com.umc.component.component.UText
import com.umc.component.theme.*
import com.umc.component.theme.UmcTypographyTokens.Caption1
import com.umc.component.theme.UmcTypographyTokens.Caption1Bold
import com.umc.component.theme.UmcTypographyTokens.Footnote
import com.umc.component.theme.UmcTypographyTokens.FootnoteBold
import com.umc.component.theme.UmcTypographyTokens.SubheadlineBold
import com.umc.presentation.study.admin.group.AdminStudyGroupItemUiModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminStudyGroupCard(
    item: AdminStudyGroupItemUiModel,
    isSettingOpen: Boolean,
    onSettingClick: () -> Unit,
    onDismissSetting: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onAddScheduleClick: () -> Unit,
    onAddMemberClick: () -> Unit,
) {
    Box {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(grey000(), RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                UText(
                    text = item.title,
                    style = SubheadlineBold,
                    color = grey900()
                )

                Spacer(Modifier.width(6.dp))

                Box(
                    modifier = Modifier
                        .background(indigo100(), RoundedCornerShape(4.dp))
                        .border(1.dp, indigo200(), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    UText(
                        text = item.partLabel,
                        style = Caption1Bold,
                        color = indigo600()
                    )
                }

                Spacer(Modifier.weight(1f))

                Icon(
                    painter = painterResource(R.drawable.ic_setting_outline),
                    contentDescription = null,
                    tint = grey500(),
                    modifier = Modifier
                        .size(22.dp)
                        .clickable { onSettingClick() }
                )
            }

            Spacer(Modifier.height(4.dp))

            UText(
                text = "${item.createdAtText}  |  멤버 ${item.memberCount}명",
                style = Footnote,
                color = grey500()
            )

            Spacer(Modifier.height(14.dp))

            UText(
                text = "담당 파트장",
                style = FootnoteBold,
                color = grey800()
            )

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(grey100(), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(grey000()),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_profile_default),
                        contentDescription = null,
                        tint = grey400(),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(Modifier.width(8.dp))

                UText(
                    text = item.leaderName,
                    style = Caption1Bold,
                    color = grey800()
                )

                Spacer(Modifier.width(5.dp))

                UText(
                    text = item.leaderUniv.ifBlank { "중앙대" },
                    style = Caption1,
                    color = grey500()
                )

                Spacer(Modifier.weight(1f))

                Box(
                    modifier = Modifier
                        .background(grey000(), RoundedCornerShape(4.dp))
                        .border(1.dp, grey200(), RoundedCornerShape(4.dp))
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    UText(
                        text = "Leader",
                        style = Caption1Bold,
                        color = grey600()
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            UText(
                text = "스터디원",
                style = FootnoteBold,
                color = grey800()
            )

            Spacer(Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                FlowRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    item.members.forEach { member ->
                        AdminStudyGroupMemberChip(member = member)
                    }
                }

                Spacer(Modifier.width(8.dp))

                Icon(
                    painter = painterResource(R.drawable.ic_add_filled),
                    contentDescription = null,
                    tint = indigo500(),
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onAddMemberClick() }
                )
            }

            Spacer(Modifier.height(12.dp))

            AdminStudyGroupScheduleButton(
                onClick = onAddScheduleClick
            )
        }

        if (isSettingOpen) {
            AdminStudyGroupSettingPopup(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 42.dp, end = 10.dp),
                onEditClick = onEditClick,
                onDeleteClick = onDeleteClick,
                onDismiss = onDismissSetting
            )
        }
    }
}